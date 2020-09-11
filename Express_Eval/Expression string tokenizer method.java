package app;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import javafx.scene.chart.StackedBarChart;
import structures.Stack;

public class Expression {

    enum MATCH_TYPE {
        EQUALS_OPENING_BRACKET, EQUALS_CLOSING_BRACKET, EQUALS_OPENING_PARENTHESIS, EQUALS_CLOSING_PARENTHESIS,
        IS_LETTER, IS_DIGIT, IS_OPERAND, NO_MATCH
    }

    public static String delims = " \t*+-/()[]";

    /**
     * Populates the vars list with simple variables, and arrays lists with arrays
     * in the expression. For every variable (simple or array), a SINGLE instance is
     * created and stored, even if it appears more than once in the expression. At
     * this time, values for all variables and all array items are set to zero -
     * they will be loaded from a file in the loadVariableValues method.
     * 
     * @param expr   The expression
     * @param vars   The variables array list - already created by the caller
     * @param arrays The arrays array list - already created by the caller
     */
    public static void makeVariableLists(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
        String noSpace = expr.replaceAll("\\s+", "");
        String[] asArray = noSpace.split("(?<=[-+*/()\\[\\]])|(?=[-+*/()\\]])");

        for (int i = 0; i < asArray.length; i++) {
            String temp = asArray[i];

            if (!Character.isLetter(temp.charAt(0))) {
                continue;
            } else {
                boolean noDuplicate = false;

                if (temp.contains("[")) {
                    temp = temp.replace("[", "");

                    for (int j = 0; j < arrays.size(); j++) {
                        if (temp.equals(arrays.get(j).name)) {
                            noDuplicate = true;
                            break;
                        }
                    }

                    if (noDuplicate == false) {
                        arrays.add(new Array(temp));
                    }
                } else {
                    for (int j = 0; j < vars.size(); j++) {
                        if (temp.equals(vars.get(j).name)) {
                            noDuplicate = true;
                            break;
                        }
                    }

                    if (noDuplicate == false) {
                        vars.add(new Variable(temp));
                    }
                }
            }
        }
    }

    /**
     * Loads values for variables and arrays in the expression
     * 
     * @param sc Scanner for values input
     * @throws IOException If there is a problem with the input
     * @param vars   The variables array list, previously populated by
     *               makeVariableLists
     * @param arrays The arrays array list - previously populated by
     *               makeVariableLists
     */
    public static void loadVariableValues(Scanner sc, ArrayList<Variable> vars, ArrayList<Array> arrays)
            throws IOException {
        while (sc.hasNextLine()) {
            StringTokenizer st = new StringTokenizer(sc.nextLine().trim());
            int numTokens = st.countTokens();
            String tok = st.nextToken();
            Variable var = new Variable(tok);
            Array arr = new Array(tok);
            int vari = vars.indexOf(var);
            int arri = arrays.indexOf(arr);
            if (vari == -1 && arri == -1) {
                continue;
            }
            int num = Integer.parseInt(st.nextToken());
            if (numTokens == 2) { // scalar symbol
                vars.get(vari).value = num;
            } else { // array symbol
                arr = arrays.get(arri);
                arr.values = new int[num];
                // following are (index,val) pairs
                while (st.hasMoreTokens()) {
                    tok = st.nextToken();
                    StringTokenizer stt = new StringTokenizer(tok, " (,)");
                    int index = Integer.parseInt(stt.nextToken());
                    int val = Integer.parseInt(stt.nextToken());
                    arr.values[index] = val;
                }
            }
        }
    }

    /**
     * Evaluates the expression.
     * 
     * @param vars   The variables array list, with values for all variables in the
     *               expression
     * @param arrays The arrays array list, with values for all array items
     * @return Result of evaluation
     */
    public static float evaluate(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) { // String Tokenizer
                                                                                                   // method
        String noSpace = expr.replaceAll("\\s+", "");
        StringTokenizer st = new StringTokenizer(noSpace, delims, true);

        String answer = recurse(st, vars, arrays);

        return Float.parseFloat(answer);
    }

    private static String recurse(StringTokenizer st, ArrayList<Variable> vars, ArrayList<Array> arrays) {
        Stack<String> varStack = new Stack<String>();
        Stack<String> operands = new Stack<String>();

        while (st.hasMoreTokens()) {
            String crnt = st.nextToken();

            switch (checkMatch(crnt)) {
                case EQUALS_CLOSING_BRACKET:
                    return reverseAndCalculate(varStack, operands);
                case EQUALS_OPENING_BRACKET:
                    String arrayName = varStack.pop();
                    String index = recurse(st, vars, arrays);

                    for (int i = 0; i < arrays.size(); i++) {
                        if (arrays.get(i).name.equals(arrayName)) {
                            varStack.push(arrays.get(i).values[Integer.parseInt(index)] + "");
                            break;
                        }
                    }
                    break;
                case EQUALS_OPENING_PARENTHESIS:
                    varStack.push(recurse(st, vars, arrays));
                    break;
                case EQUALS_CLOSING_PARENTHESIS:
                    return reverseAndCalculate(varStack, operands);
                case IS_DIGIT:
                    varStack.push(crnt);
                    break;
                case IS_LETTER:
                    boolean isVar = false;

                    for (int i = 0; i < vars.size(); i++) {
                        if (vars.get(i).name.equals(crnt)) {
                            isVar = true;
                            varStack.push(vars.get(i).value + "");
                            break;
                        }
                    }

                    if (!isVar) {
                        varStack.push(crnt);
                    }

                    break;
                case IS_OPERAND:
                    operands.push(crnt);
                    break;
                default:
                    break;
            }

            if (!operands.isEmpty() && operands.size() != varStack.size()
                    && (operands.peek().equals("*") || operands.peek().equals("/"))) {
                calculate(varStack, operands);
            }
        }

        return reverseAndCalculate(varStack, operands);
    }

    private static Expression.MATCH_TYPE checkMatch(String crnt) {
        if (crnt.equals("["))
            return Expression.MATCH_TYPE.EQUALS_OPENING_BRACKET;
        else if (crnt.equals("]"))
            return Expression.MATCH_TYPE.EQUALS_CLOSING_BRACKET;
        else if (crnt.equals("("))
            return Expression.MATCH_TYPE.EQUALS_OPENING_PARENTHESIS;
        else if (crnt.equals(")"))
            return Expression.MATCH_TYPE.EQUALS_CLOSING_PARENTHESIS;
        else if (Character.isDigit(crnt.charAt(0)))
            return Expression.MATCH_TYPE.IS_DIGIT;
        else if (Character.isLetter(crnt.charAt(0)))
            return Expression.MATCH_TYPE.IS_LETTER;
        else
            return Expression.MATCH_TYPE.IS_OPERAND;
    }

    private static String reverseAndCalculate(Stack<String> varStack, Stack<String> operands) {
        Stack<String> reversedVarStack = new Stack<String>();
        Stack<String> reversedOperands = new Stack<String>();

        while (!varStack.isEmpty()) {
            reversedVarStack.push(varStack.pop());
        }

        while (!operands.isEmpty()) {
            reversedOperands.push(operands.pop());
        }

        while (!reversedOperands.isEmpty()) {
            calculate(reversedVarStack, reversedOperands);
        }

        return reversedVarStack.peek();
    }

    private static void calculate(Stack<String> varStack, Stack<String> operands) {
        String newNum = "";
        float a, b;

        switch (operands.pop()) {
            case "+":
                a = Float.parseFloat(varStack.pop());
                b = Float.parseFloat(varStack.pop());
                newNum = a + b + "";
                break;
            case "-":
                a = Float.parseFloat(varStack.pop());
                b = Float.parseFloat(varStack.pop());
                newNum = a - b + "";
                break;
            case "*":
                a = Float.parseFloat(varStack.pop());
                b = Float.parseFloat(varStack.pop());
                newNum = a * b + "";
                break;
            case "/":
                b = Float.parseFloat(varStack.pop());
                a = Float.parseFloat(varStack.pop());
                newNum = a / b + "";
                break;
            default:
                break;
        }

        varStack.push(newNum);
    }
}