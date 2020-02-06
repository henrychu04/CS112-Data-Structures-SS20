package poly;

import java.io.IOException;
import java.util.Scanner;

/**
 * This class implements evaluate, add and multiply for polynomials.
 * 
 * @author runb-cs112
 *
 */
public class Polynomial {
	
	/**
	 * Reads a polynomial from an input stream (file or keyboard). The storage format
	 * of the polynomial is:
	 * <pre>
	 *     <coeff> <degree>
	 *     <coeff> <degree>
	 *     ...
	 *     <coeff> <degree>
	 * </pre>
	 * with the guarantee that degrees will be in descending order. For example:
	 * <pre>
	 *      4 5
	 *     -2 3
	 *      2 1
	 *      3 0
	 * </pre>
	 * which represents the polynomial:
	 * <pre>
	 *      4*x^5 - 2*x^3 + 2*x + 3 
	 * </pre>
	 * 
	 * @param sc Scanner from which a polynomial is to be read
	 * @throws IOException If there is any input error in reading the polynomial
	 * @return The polynomial linked list (front node) constructed from coefficients and
	 *         degrees read from scanner
	 */
	public static Node read(Scanner sc) 
	throws IOException {
		Node poly = null;
		while (sc.hasNextLine()) {
			Scanner scLine = new Scanner(sc.nextLine());
			poly = new Node(scLine.nextFloat(), scLine.nextInt(), poly);
			scLine.close();
		}
		return poly;
	}
	
	/**
	 * Returns the sum of two polynomials - DOES NOT change either of the input polynomials.
	 * The returned polynomial MUST have all new nodes. In other words, none of the nodes
	 * of the input polynomials can be in the result.
	 * 
	 * @param poly1 First input polynomial (front of polynomial linked list)
	 * @param poly2 Second input polynomial (front of polynomial linked list
	 * @return A new polynomial which is the sum of the input polynomials - the returned node
	 *         is the front of the result polynomial
	 */
	public static Node add(Node poly1, Node poly2) {
		
		Node node1 = poly1;
		Node node2 = poly2;
		Node newNode = new Node(0, -1, null);
		Node returningNode = newNode;
		
		int newDegree = 0;
		float newCoeff = 0;

		while(node1 != null || node2 != null) {
			if(node1 == null || node2 == null) {
				while(node2 == null) {
					newNode.next = node1;
					if(node1.next == null) {
						break;
					} else {
						node1 = node1.next;
					}
				}
				while(node1 == null) {
					newNode.next = node2;
					if(node2.next == null) {
						break;
					} else {
						node2 = node2.next;
					}
				}
				break;
			}
			if(node1.term.degree == node2.term.degree) {
				newDegree = node1.term.degree;
				newCoeff = node1.term.coeff + node2.term.coeff;
				node1 = node1.next;
				node2 = node2.next;
			} else if(node1.term.degree < node2.term.degree) {
				newDegree = node1.term.degree;
				newCoeff = node1.term.coeff;
				node1 = node1.next;
			} else if(node1.term.degree > node2.term.degree) {
				newDegree = node2.term.degree;
				newCoeff = node2.term.coeff;
				node2 = node2.next;
			}

			if(newNode.term.degree == -1) {
				newNode.term.coeff = newCoeff;
				newNode.term.degree = newDegree;
			} else {
				Node createdNode = new Node(newCoeff, newDegree, null);
				newNode.next = createdNode;
				newNode = newNode.next;
			}
		}

		return returningNode;
	}
	
	/**
	 * Returns the product of two polynomials - DOES NOT change either of the input polynomials.
	 * The returned polynomial MUST have all new nodes. In other words, none of the nodes
	 * of the input polynomials can be in the result.
	 * 
	 * @param poly1 First input polynomial (front of polynomial linked list)
	 * @param poly2 Second input polynomial (front of polynomial linked list)
	 * @return A new polynomial which is the product of the input polynomials - the returned node
	 *         is the front of the result polynomial
	 */
	public static Node multiply(Node poly1, Node poly2) {

		Node node1 = poly1;
		Node node2 = poly2;
		Node newNode = new Node(0, -1, null);
		Node returningNode = newNode;

		int newDegree = 0;
		float newCoeff = 0;

		while(node1 != null) {
			while(node2 != null) {
				newDegree = node1.term.degree + node2.term.degree;
				newCoeff = node1.term.coeff * node2.term.coeff;

				if(newNode.term.degree == -1) {
					newNode.term.coeff = newCoeff;
					newNode.term.degree = newDegree;
				} else {
					Node crnt = returningNode;
					boolean matchDegree = false;
					while(crnt != null) {
						if(crnt.term.degree == newDegree) {
							crnt.term.coeff += newCoeff;
							matchDegree = true;
							break;
						}
						crnt = crnt.next;
					}
					if(!matchDegree) {
						Node createdNode = new Node(newCoeff, newDegree, null);
						newNode.next = createdNode;
						newNode = newNode.next;
					}
				}
				node2 = node2.next;
			}
			node2 = poly2;
			node1 = node1.next;
		}

		return mergeSort(returningNode);
	}

	private static Node sortedMerge(Node a, Node b) {
		Node result = null;

		if(a == null) {
			return b;
		}
		if(b == null) {
			return a;
		}

		if(a.term.degree <= b.term.degree) {
			result = a;
			result.next = sortedMerge(a.next, b);
		} else {
			result = b;
			result.next = sortedMerge(a, b.next);
		}
		
		return result;
	}

	private static Node mergeSort(Node a) {
		if(a == null || a.next == null) {
			return a;
		}

		Node middle = getMiddle(a);
		Node nextOfMiddle = middle.next;

		middle.next = null;

		Node left = mergeSort(a);
		Node right = mergeSort(nextOfMiddle);

		Node sortedList = sortedMerge(left, right);

		return sortedList;
	}

	private static Node getMiddle(Node a) {
		if(a == null) {
			return a;
		}

		Node slow = a;
		Node fast = a;

		while(fast.next != null && fast.next.next != null) {
			slow = slow.next;
			fast = fast.next.next;
		}
		return slow;
	}
		
	/**
	 * Evaluates a polynomial at a given value.
	 * 
	 * @param poly Polynomial (front of linked list) to be evaluated
	 * @param x Value at which evaluation is to be done
	 * @return Value of polynomial p at x
	 */
	public static float evaluate(Node poly, float x) {
		
		Node crnt = poly;
		float answer = 0;
		float crntEval = x;

		while(crnt != null) {
			if(crnt.term.degree != 0) {
				crntEval = (float)Math.pow((double)x, (double)crnt.term.degree);
			}
			if(crnt.term.coeff != 1 && crnt.term.degree != 0) {
				crntEval = crntEval * crnt.term.coeff;
			}
			if(crnt.term.degree == 0) {
				crntEval = crnt.term.coeff;
			}
			answer += crntEval;
			crnt = crnt.next;
		}

		return answer;
	}
	
	/**
	 * Returns string representation of a polynomial
	 * 
	 * @param poly Polynomial (front of linked list)
	 * @return String representation, in descending order of degrees
	 */
	public static String toString(Node poly) {
		if (poly == null) {
			return "0";
		} 
		
		String retval = poly.term.toString();
		for (Node current = poly.next ; current != null ;
		current = current.next) {
			retval = current.term.toString() + " + " + retval;
		}
		return retval;
	}	
}
