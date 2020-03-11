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
        String noSpace = expr.replaceAll("\\s+","");
        String[] asArray = noSpace.split("(?<=[-+*/()\\[\\]])|(?=[-+*/()\\]])");

        for(int i = 0; i < asArray.length; i++) {
            String temp = asArray[i];

            if(!Character.isLetter(temp.charAt(0))) {
                continue;
            } else {
                boolean noDuplicate = false;

                if(temp.contains("[")) {
                    temp = temp.replace("[", "");

                    for(int j = 0; j < arrays.size(); j++) {
                        if(temp.equals(arrays.get(j).name)) {
                            noDuplicate = true;
                            break;
                        }
                    }

                    if(noDuplicate == false) {
                        arrays.add(new Array(temp));
                    }
                } else {
                    for(int j = 0; j < vars.size(); j++) {
                        if(temp.equals(vars.get(j).name)) {
                            noDuplicate = true;
                            break;
                        }
                    }

                    if(noDuplicate == false) {
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
        String noSpace = expr.replaceAll("\\s+","");
        String[] asArray = noSpace.split("(?<=[-+*/()\\[\\]])|(?=[-+*/()\\]])");
        Stack<String> operands = new Stack<String>();
        Stack<String> output = new Stack<String>();

        for(int i = 0; i < asArray.length; i++) {
            String crnt = asArray[i];

            if(Character.isDigit(crnt.charAt(0))) {
                output.push(crnt);
            } else if(crnt.equals("+") || crnt.equals("-") || crnt.equals("*") || crnt.equals("/")) {
                if(!operands.isEmpty() && givePrecedence(operands.peek()) == givePrecedence(crnt)) {
                    output.push(operands.pop());
                }
                
                while(!operands.isEmpty() && 
                    ((givePrecedence(operands.peek()) > givePrecedence(crnt)) || 
                    (givePrecedence(operands.peek()) == givePrecedence(crnt) && 
                    (crnt.equals("-") || crnt.equals("/"))) && 
                    !operands.peek().equals("("))) {
                    
                    output.push(operands.pop());
                }

                operands.push(crnt);
            } else if(crnt.equals("(")) {
                operands.push(crnt);
            } else if(crnt.equals(")")) {
                while(!operands.peek().equals("(")) {
                    output.push(operands.pop());
                }

                operands.pop();
            }
        }

        while(!operands.isEmpty()) {
            output.push(operands.pop());
        }

        Stack<String> answer = new Stack<String>();
        Stack<String> reverseOutput = new Stack<String>();

        while(!output.isEmpty()) {
            reverseOutput.push(output.pop());
        }

        while(!reverseOutput.isEmpty()) {
            String crnt = reverseOutput.pop();

            if(crnt.equals("+") || crnt.equals("-") || crnt.equals("*") || crnt.equals("/")) {
                Float newNum = 0.f, a, b;

                switch(crnt) {
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
            } else {
                answer.push(crnt);
            }
        }
        
        return Float.parseFloat(answer.peek());
    }

    private static int givePrecedence(String operand) {
        switch(operand) {
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