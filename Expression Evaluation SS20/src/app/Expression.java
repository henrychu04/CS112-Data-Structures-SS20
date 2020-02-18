package app;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import structures.Stack;

public class Expression {

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
        String exprNoSpace = expr.replaceAll("\\s+","");
    	StringTokenizer varis = new StringTokenizer(exprNoSpace, delims);
        while(varis.hasMoreTokens()) {
            String temp = varis.nextToken();
            boolean noDuplicate = false;
            for(int i = 0; i < vars.size(); i++) {
                if(temp == vars.get(i).name) {
                    noDuplicate = true;
                    break;
                }
            }
            if(noDuplicate == false) {
                vars.add(new Variable(temp));
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
    evaluate(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {

        String exprNoSpace = expr.replaceAll("\\s+","");
        String[] newString = exprNoSpace.split("(?<=[-+*/()])|(?=[-+*/()])");
        Stack<String> allStack = new Stack<String>();

        for(int i = newString.length; i > 0; i--) {
            allStack.push(newString[i-1]);
        }

        String numString = recurse(allStack, vars);
        
        return Float.parseFloat(numString);
    }

    private static String recurse(Stack<String> allStack, ArrayList<Variable> vars) {

        Stack<String> varStack = new Stack<String>();
        Stack<String> operands = new Stack<String>();
        String crnt;

        while(!allStack.isEmpty()) {
            crnt = allStack.pop();

            if(crnt.equals("(")) {
                varStack.push(recurse(allStack, vars));
            } else if(crnt.equals(")")) {
                varStack = reverse(varStack);
                operands = reverse(operands);

                while(!operands.isEmpty()) {
                    calculate(varStack, operands);
                }
                
                return varStack.peek();
            }

            if(Character.isDigit(crnt.charAt(0))) {
                varStack.push(crnt);
            } else if(Character.isLetter(crnt.charAt(0)) && !crnt.equals("(") && !crnt.equals(")")) {
                for(int i = 0; i < vars.size(); i++) {
                    if(crnt.equals(vars.get(i).name)) {
                        varStack.push(vars.get(i).value + "");
                        break;
                    }
                }
            } else if(!crnt.equals("(") && !crnt.equals(")")){
                operands.push(crnt);
            }

            if(varStack.size() != 1 && !operands.isEmpty() && operands.size() != varStack.size() && (operands.peek().equals("*") || operands.peek().equals("/"))) {
                calculate(varStack, operands);
            }
        }

        varStack = reverse(varStack);
        operands = reverse(operands);

        while(!operands.isEmpty()) {
            calculate(varStack, operands);
        }

        return varStack.peek();
    }

    private static Stack<String> reverse(Stack<String> stack) {

        Stack<String> finalStack = new Stack<String>();

        while(!stack.isEmpty()) {
            finalStack.push(stack.pop());
        }

        return finalStack;
    }

    private static void calculate(Stack<String> varStack, Stack<String> operands) {

        String newNum;
        float a, b;

        switch(operands.pop()) {
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
                b = Float.parseFloat(varStack.pop());
                a = Float.parseFloat(varStack.pop());
                newNum = a * b + "";
                break;
            case "/":
                b = Float.parseFloat(varStack.pop());
                a = Float.parseFloat(varStack.pop());
                newNum = a / b + "";
                break;
            default:
                newNum = "";
                break;
        }
        
    	varStack.push(newNum);
    }
}
