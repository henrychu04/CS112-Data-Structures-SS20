package app;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import structures.Stack;

public class Expression {

    enum MATCH_TYPE {
        IS_DIGIT,
        IS_LETTER,
        IS_OPERAND,
        EQUALS_OPENING_PARENTHESIS,
        EQUALS_CLOSING_PARENTHESIS,
        NO_MATCH
    }

	public static String delims = " \t*+-/()[]";
			
    /**
     * Populates the vars list with simple variables, and arrays lists with arrays
     * in the expression. For every variable (simple or array), a SINGLE instance is created 
     * and stored, even if it appears more than once in the expression.
     * At this time, values for all variables and all array items are set to
     * zero - they will be loaded from a file in the loadVariableValues method.
     * 
     * @param expr The expression
     * @param vars The variables array list - already created by the caller
     * @param arrays The arrays array list - already created by the caller
     */
    public static void 
    makeVariableLists(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
        String noSpace = expr.replaceAll("\\s+","");
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

                    if(noDuplicate == false) {
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
     * @param vars The variables array list, previously populated by makeVariableLists
     * @param arrays The arrays array list - previously populated by makeVariableLists
     */
    public static void 
    loadVariableValues(Scanner sc, ArrayList<Variable> vars, ArrayList<Array> arrays) 
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
                    StringTokenizer stt = new StringTokenizer(tok," (,)");
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
     * @param vars The variables array list, with values for all variables in the expression
     * @param arrays The arrays array list, with values for all array items
     * @return Result of evaluation
     */
    public static float 
    evaluate(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) { // Shunting Yard Algorithm
        // Does not work currently

        String noSpace = expr.replaceAll("\\s+","");
        StringTokenizer st = new StringTokenizer(noSpace, delims, true);
        Stack<String> operands = new Stack<String>();
        Stack<String> output = new Stack<String>();

        while (st.hasMoreTokens()) {
            String crnt = st.nextToken();

            switch (checkMatch(crnt)) {
                case IS_DIGIT:
                    output.push(crnt);
                    break;
                case IS_LETTER:
                    for (int j = 0; j < vars.size(); j++) {
                        if (vars.get(j).name.equals(crnt)) {
                            output.push(vars.get(j).value + "");
                            break;
                        }
                    }
                    break;
                case IS_OPERAND:                    
                    while (!operands.isEmpty() &&
                        ((givePrecedence(operands.peek()) > givePrecedence(crnt)) || 
                        (givePrecedence(operands.peek()) == givePrecedence(crnt) && (crnt.equals("-") || crnt.equals("/"))) && 
                        !operands.peek().equals("("))) {
                        
                        output.push(operands.pop());
                    }

                    operands.push(crnt);
                    break;
                case EQUALS_OPENING_PARENTHESIS:
                    operands.push(crnt);
                    break;
                case EQUALS_CLOSING_PARENTHESIS:
                    while (!operands.peek().equals("(")) {
                        output.push(operands.pop());
                    }

                    operands.pop();
                    break;
                default:
                    break;
            }
        }

        while (!operands.isEmpty()) {
            output.push(operands.pop());
        }

        Stack<String> answer = new Stack<String>();
        Stack<String> reverseOutput = reverse(output);        

        while (!reverseOutput.isEmpty()) {
            String crnt = reverseOutput.pop();

            switch (checkMatch(crnt)) {
                case IS_OPERAND:
                    calculate(crnt, answer);
                    break;
                default:
                    answer.push(crnt);
                    break;
            }
        }
        
        return Float.parseFloat(answer.peek());
    }

    private static Expression.MATCH_TYPE checkMatch(String crnt) {
        if (Character.isDigit(crnt.charAt(0)))                                                  return Expression.MATCH_TYPE.IS_DIGIT;
        else if (Character.isLetter(crnt.charAt(0)))                                            return Expression.MATCH_TYPE.IS_LETTER;
        else if (crnt.equals("+") || crnt.equals("-") || crnt.equals("*") || crnt.equals("/"))  return Expression.MATCH_TYPE.IS_OPERAND;
        else if (crnt.equals("("))                                                              return Expression.MATCH_TYPE.EQUALS_OPENING_PARENTHESIS;
        else if (crnt.equals(")"))                                                              return Expression.MATCH_TYPE.EQUALS_CLOSING_PARENTHESIS;
        else                                                                                    return Expression.MATCH_TYPE.NO_MATCH;
    }

    private static Stack<String> reverse(Stack<String> toBeReversed) {
        Stack<String> reversed = new Stack<String>();

        while (!toBeReversed.isEmpty()) {
            reversed.push(toBeReversed.pop());
        }

        return reversed;
    }

    private static void calculate(String operand, Stack<String> answer) {
        Float newNum = 0.f, a, b;

        switch (operand) {
            case "+":
                a = Float.parseFloat(answer.pop());
                b = Float.parseFloat(answer.pop());
                newNum = a + b;
                break;
            case "-":
                a = Float.parseFloat(answer.pop());
                b = Float.parseFloat(answer.pop());
                newNum = b - a;
                break;
            case "*":
                a = Float.parseFloat(answer.pop());
                b = Float.parseFloat(answer.pop());
                newNum = a * b;
                break;
            case "/":
                a = Float.parseFloat(answer.pop());
                b = Float.parseFloat(answer.pop());
                newNum = b / a;
                break;
            default:
                break;
        }

        answer.push(newNum + "");
    }

    private static int givePrecedence(String operand) {
        switch (operand) {
            case "+":
                return 1;
            case "-":
                return 1;
            case "*":
                return 5;
            case "/":
                return 5;
            default:
                return 0;
        }
    }
}