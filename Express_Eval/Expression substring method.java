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
    evaluate(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) { // Substring method
        String noSpace = expr.replaceAll("\\s", ""); // remove the spaces in the expression
        Stack<String> varStack = new Stack<String>();
        Stack<String> operands = new Stack<String>();
        
        for(int i = 0; i < noSpace.length(); i++) { // iterate through the expression
            String temp = "";

            if(noSpace.charAt(i) == '(') { // checks if current char is an opening parenthesis
                int closed = findClosing(noSpace, '(', ')', i); // finds closing parenthesis and returns index of it

                varStack.push(evaluate(noSpace.substring(i + 1, closed), vars, arrays) + ""); // recurses inside the opening and closing parenthesis

                i = closed; // sets i to the closed parethesis to skip what is inside since it is already evaluated
            } else if(noSpace.charAt(i) == ')') {
                return Float.parseFloat(reverseAndCalculate(varStack, operands)); // if current char is a closing parenthesis calculate and return
            } else if(noSpace.charAt(i) == ']') {
                return Float.parseFloat(reverseAndCalculate(varStack, operands)); // if current char is a closing bracket calculate and return
            } else if(Character.isLetter(noSpace.charAt(i))) {
                while(Character.isLetter(noSpace.charAt(i))) { // sets temp to variable name
                    temp += noSpace.charAt(i) + "";
                    i++;

                    if(i == noSpace.length()) {
                        break;
                    }
                }

                i--;
                
                if(i + 1 < noSpace.length() && noSpace.charAt(i + 1) == '[') { // checks if the variable name is an array
                    i++;

                    int closed = findClosing(noSpace, '[', ']', i); // finds closing bracket

                    int index = (int)evaluate(noSpace.substring(i + 1, closed), vars, arrays); // recurses inside the opening and closing bracket

                    for(int k = 0; k < arrays.size(); k++) { // finds the value of the index of the array
                        if(temp.equals(arrays.get(k).name)) {
                            varStack.push(arrays.get(k).values[index] + "");
                            break;
                        }
                    }

                    i = closed; // sets i to the closed bracket to skip what is inside since it is already evaluated
                } else {
                    for(int j = 0; j < vars.size(); j++) { // gets variable value
                        if(temp.equals(vars.get(j).name)) {
                            varStack.push(vars.get(j).value + "");
                            break;
                        }
                    }
                }
            } else if(Character.isDigit(noSpace.charAt(i))) { 
                while(Character.isDigit(noSpace.charAt(i))) { // sets temp to value of the current number
                    temp += noSpace.charAt(i) + "";
                    i++;
                    
                    if(i == noSpace.length()) {
                        break;
                    }
                }

                i--;
                
                varStack.push(temp);
            } else if(noSpace.charAt(i) == '+' || noSpace.charAt(i) == '-' || noSpace.charAt(i) == '*' || noSpace.charAt(i) == '/') { // if current char is an operand push it to the operand stack
                operands.push(noSpace.charAt(i) + "");
            }

            if(!operands.isEmpty() && operands.size() != varStack.size() && (operands.peek().equals("*") || operands.peek().equals("/"))) { // if the top of the operand stack is a multiply or divide evaulate it immediately and return the value
                calculate(varStack, operands);
            }
        }

        return Float.parseFloat(reverseAndCalculate(varStack, operands)); // evaluates whats left inside the varStack and operands stack
    }

    private static int findClosing(String expr, Character open, Character close, int i) { // finds closing parenthesis or bracket
        int closing = 0, count = 0;

        for(closing = i; closing < expr.length(); closing++) {
            if(expr.charAt(closing) == close) {
                count--;
            } else if(expr.charAt(closing)== open) {
                count++;
            }

            if(count == 0) {
                break;
            }
        }

        return closing;
    }

    private static String reverseAndCalculate(Stack<String> varStack, Stack<String> operands) { // reverses the stacks and calculates
        Stack<String> reversedVarStack = new Stack<String>();
        Stack<String> reversedOperands = new Stack<String>();

        while(!varStack.isEmpty()) {
            reversedVarStack.push(varStack.pop());
        }
        while(!operands.isEmpty()) {
            reversedOperands.push(operands.pop());
        }

        while(!reversedOperands.isEmpty()) {
            calculate(reversedVarStack, reversedOperands);
        }

        return reversedVarStack.peek();
    }

    private static void calculate(Stack<String> varStack, Stack<String> operands) { // calculates
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
                newNum = "";
                break;
        }
        
    	varStack.push(newNum);
    }
}