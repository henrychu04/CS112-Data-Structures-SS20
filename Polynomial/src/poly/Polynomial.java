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

		Node temp = null;
		Node a = null;
		Node returningNode = a;
		Node node1 = poly1;
		Node node2 = poly2;

		while(node1 != null || node2 != null) {
			if(node1 == null) {
				temp = new Node(node2.term.coeff, node2.term.degree, null);				
				node2 = node2.next;
			} else if(node2 == null) {
				temp = new Node(node1.term.coeff, node1.term.degree, null);
				node1 = node1.next;
			} else if(node1.term.degree == node2.term.degree) {
				temp = new Node(node1.term.coeff + node2.term.coeff, node1.term.degree, null);
				node1 = node1.next;
				node2 = node2.next;
			} else if(node1.term.degree < node2.term.degree) {
				temp = new Node(node1.term.coeff, node1.term.degree, null);
				node1 = node1.next;
			} else if(node1.term.degree > node2.term.degree) {
				temp = new Node(node2.term.coeff, node2.term.degree, null);
				node2 = node2.next;
			}

			if(temp.term.coeff == 0) {
				continue;
			} else if(a == null) {
				a = temp;
				returningNode = a;
			} else {
				a.next = temp;
				a = a.next;
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

		Node nullInput = new Node(0, 0, null);
		if(poly1 == null || poly2 == null) {
			return nullInput;
		}

		Node node1 = poly1;
		Node node2 = poly2;
		Node newNode = null;

		while(node1 != null) {
			while(node2 != null) {
				Node createdNode = new Node(node1.term.coeff * node2.term.coeff, node1.term.degree + node2.term.degree, null);

				if(newNode == null) {
					newNode = createdNode;
				} else {
					newNode = add(createdNode, newNode);
				}
				node2 = node2.next;
			}
			node2 = poly2;
			node1 = node1.next;
		}

		return newNode;
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

		while(crnt != null) {
			answer += crnt.term.coeff * Math.pow(x, crnt.term.degree);
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
