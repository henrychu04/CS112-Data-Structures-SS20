package bigint;

/**
 * This class encapsulates a BigInteger, i.e. a positive or negative integer with 
 * any number of digits, which overcomes the computer storage length limitation of 
 * an integer.
 * 
 */
public class BigInteger {

    enum COMPARISON {
        FIRST_BIGGER,
        SECOND_BIGGER,
        EQUAL
    }

    /**
     * True if this is a negative integer
     */
    boolean negative;
    
    /**
     * Number of digits in this integer
     */
    int numDigits;
    
    /**
     * Reference to the first node of this integer's linked list representation
     * NOTE: The linked list stores the Least Significant Digit in the FIRST node.
     * For instance, the integer 235 would be stored as:
     *    5 --> 3  --> 2
     *    
     * Insignificant digits are not stored. So the integer 00235 will be stored as:
     *    5 --> 3 --> 2  (No zeros after the last 2)        
     */
    DigitNode front;
    
    /**
     * Initializes this integer to a positive number with zero digits, in other
     * words this is the 0 (zero) valued integer.
     */
    public BigInteger() {
        negative = false;
        numDigits = 0;
        front = null;
    }
    
    /**
     * Parses an input integer string into a corresponding BigInteger instance.
     * A correctly formatted integer would have an optional sign as the first 
     * character (no sign means positive), and at least one digit character
     * (including zero). 
     * Examples of correct format, with corresponding values
     *      Format     Value
     *       +0            0
     *       -0            0
     *       +123        123
     *       1023       1023
     *       0012         12  
     *       0             0
     *       -123       -123
     *       -001         -1
     *       +000          0
     *       
     * Leading and trailing spaces are ignored. So "  +123  " will still parse 
     * correctly, as +123, after ignoring leading and trailing spaces in the input
     * string.
     * 
     * Spaces between digits are not ignored. So "12  345" will not parse as
     * an integer - the input is incorrectly formatted.
     * 
     * An integer with value 0 will correspond to a null (empty) list - see the BigInteger
     * constructor
     * 
     * @param integer Integer string that is to be parsed
     * @return BigInteger instance that stores the input integer.
     * @throws IllegalArgumentException If input is incorrectly formatted
     */
    public static BigInteger parse(String integer) 
    throws IllegalArgumentException {
        BigInteger BI = new BigInteger();
        boolean nonZeroDigitFound = false;
        
        integer = integer.trim();
        
        if (integer.length() == 0) {
            throw new IllegalArgumentException();
        }
        
        for (int i = 0; i < integer.length(); i++) {
            char place = integer.charAt(i);
            
            if (i == 0 && place == '-') {
                BI.negative = true;
                continue;
            } else if (i == 0 && place == '+') {
                BI.negative = false;
                continue;
            } else if (!nonZeroDigitFound && place == '0') {
                continue;
            }
            
            nonZeroDigitFound = true;
            
            if (Character.isDigit(place)) {
                addNewNode(BI, Character.getNumericValue(place));
            } else {
                throw new IllegalArgumentException();
            }
        }
        
        return BI;
    }
    
    /**
     * Adds the first and second big integers, and returns the result in a NEW BigInteger object. 
     * DOES NOT MODIFY the input big integers.
     * 
     * NOTE that either or both of the input big integers could be negative.
     * (Which means this method can effectively subtract as well.)
     * 
     * @param first First big integer
     * @param second Second big integer
     * @return Result big integer
     */
    public static BigInteger add(BigInteger first, BigInteger second) {
        BigInteger BI = new BigInteger();
        DigitNode a = first.front;
        DigitNode b = second.front;
        int sum = 0, carry = 0;

        if (first.negative == second.negative) { // addition
            while (a != null || b != null) {
                int aDigit = 0;
                int bDigit = 0;

                if (a == null) {
                    aDigit = 0;
                } else {
                    aDigit = a.digit;
                }

                if (b == null) {
                    bDigit = 0;
                } else {
                    bDigit = b.digit;
                }

                sum = aDigit + bDigit + carry;
                carry = 0;

                if (sum >= 10) {
                    carry++;
                    sum -= 10;
                }

                addNewNode(BI, sum);

                if (a != null) {
                    a = a.next;
                }

                if (b != null) {
                    b = b.next;
                }
            }

            if (carry != 0) {
                addNewNode(BI, carry);
            }
        } else { // subtraction
            DigitNode bigger = null;
            DigitNode smaller = null;

            switch (whichIsBigger(first, second)) {
                case FIRST_BIGGER:
                    bigger = first.front;
                    smaller = second.front;
                    break;
                case SECOND_BIGGER:
                    bigger = second.front;
                    smaller = first.front;
                    break;
                case EQUAL:
                    DigitNode newDigit = new DigitNode(0, null);
                    BI.front = newDigit;
                    return BI;
                default:
                    break;
            }

            while (bigger != null || smaller != null) {
                int smallerDigit;

                if (smaller == null) {
                    smallerDigit = 0;
                } else {
                    smallerDigit = smaller.digit;
                }

                int biggerDigit = bigger.digit;

                if (biggerDigit - carry < 0) {
                    biggerDigit = 0;
                    carry = 1;
                } else {
                    biggerDigit -= carry;
                    carry = 0;
                }

                if (biggerDigit < smallerDigit) {
                    biggerDigit += 10;
                    sum = biggerDigit - smallerDigit;
                    carry = 1;
                } else {
                    sum = biggerDigit - smallerDigit;
                    carry = 0;
                }

                addNewNode(BI, sum);

                bigger = bigger.next;

                if (smaller != null) {
                    smaller = smaller.next;
                }
            }
        }

        BigInteger finalBI = reverse(BI);

        if (first.negative == second.negative) {
            finalBI.negative = first.negative;
        } else {
            switch (whichIsBigger(first, second)) {
                case FIRST_BIGGER:
                    finalBI.negative = first.negative;
                    break;
                case SECOND_BIGGER:
                    finalBI.negative = second.negative;
                    break;
                case EQUAL:
                    break;
                default:
                    break;
            } 
        }

        String finalBIString = finalBI.toString();

        return parse(finalBIString);
    }

    private static BigInteger.COMPARISON whichIsBigger(BigInteger first, BigInteger second) {
        Float firstNum = Float.parseFloat(first.toString());
        Float secondNum = Float.parseFloat(second.toString());

        if (firstNum < 0) {
            firstNum *= -1;
        }

        if (secondNum < 0) {
            secondNum *= -1;
        }

        if (firstNum > secondNum) {
            return COMPARISON.FIRST_BIGGER;
        } else if (firstNum < secondNum) {
            return COMPARISON.SECOND_BIGGER;
        } else {
            return COMPARISON.EQUAL;
        }
    }
    
    /**
     * Returns the BigInteger obtained by multiplying the first big integer
     * with the second big integer
     * 
     * This method DOES NOT MODIFY either of the input big integers
     * 
     * @param first First big integer
     * @param second Second big integer
     * @return A new BigInteger which is the product of the first and second big integers
     */
    public static BigInteger multiply(BigInteger first, BigInteger second) {
        BigInteger BI = new BigInteger();
        BigInteger tempBI = new BigInteger();
        DigitNode a = first.front;
        DigitNode b = second.front;
        int product = 0, carry = 0, countTens = 0, countTens2 = 0;

        while (a != null) {
            while (countTens != 0) {
                addNewNode(tempBI, 0);
                countTens--;
            }
            countTens = countTens2;

            while (b != null) {
                product = (a.digit * b.digit) + carry;

                carry = product / 10;
                product = product % 10;

                addNewNode(tempBI, product);

                b = b.next;
            }

            if (carry != 0) {
                addNewNode(tempBI, carry);
                carry = 0;
            }

            BI = add(reverse(tempBI), BI);
            tempBI = new BigInteger();
            
            countTens++;
            countTens2 = countTens;

            a = a.next;
            b = second.front;
        }

        if (first.negative == second.negative) {
            BI.negative = first.negative;
        } else {
            BI.negative = true;
        }

        return BI;
    }

    private static void addNewNode(BigInteger BI, int num) {
        DigitNode newDigit = new DigitNode(num, null);

        if (BI.front != null) {
            newDigit.next = BI.front;
        }

        BI.front = newDigit;
    }

    private static BigInteger reverse(BigInteger integer) {
        BigInteger reversedBI = new BigInteger();
        DigitNode reverse = integer.front;

        while (reverse != null) {
            DigitNode newDigit = new DigitNode(reverse.digit, reversedBI.front);
            reversedBI.front = newDigit;
            reverse = reverse.next;
        }

        return reversedBI;
    }
    
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        if (front == null) {
            return "0";
        }
        String retval = front.digit + "";
        for (DigitNode curr = front.next; curr != null; curr = curr.next) {
                retval = curr.digit + retval;
        }
        
        if (negative) {
            retval = '-' + retval;
        }
        return retval;
    }
}