package friends;

import java.util.ArrayList;

import structures.Queue;
import structures.Stack;

public class Friends {

	/**
	 * Finds the shortest chain of people from p1 to p2.
	 * Chain is returned as a sequence of names starting with p1,
	 * and ending with p2. Each pair (n1,n2) of consecutive names in
	 * the returned chain is an edge in the graph.
	 * 
	 * @param g Graph for which shortest chain is to be found.
	 * @param p1 Person with whom the chain originates
	 * @param p2 Person at whom the chain terminates
	 * @return The shortest chain from p1 to p2. Null or empty array list if there is no
	 *         path from p1 to p2
	 */
	public static ArrayList<String> shortestChain(Graph g, String p1, String p2) {		
		ArrayList<String> chain = new ArrayList<String>();

		if (p1.equals(p2)) {
			System.out.println("Person one and Person two cannot match. Enter different names");
			return chain;
		}

		boolean p1exist = false, p2exist = false;

		for (int i = 0; i < g.members.length; i++) {
			if (g.members[i].name.equals(p1)) {
				p1exist = true;
			}

			if (g.members[i].name.equals(p2)) {
				p2exist = true;
			}
		}

		if (!p1exist) {
			System.out.println("Person one does not exist");
			return chain;
		}

		if (!p2exist) {
			System.out.println("Person two does not exist");
			return chain;
		}

		boolean visited[] = new boolean[g.members.length];
		int prev[] = new int[g.members.length];
		Queue<Integer> visitQueue = new Queue<Integer>();
		int p1num = -1, p2num = -1;

		for (int i = 0; i < g.members.length; i++) {
			if (g.members[i].name.equals(p1)) {
				p1num = i;
				prev[i] = -1;
				visitQueue.enqueue(i);
				visited[i] = true;
				break;
			}
		}

		while (!visitQueue.isEmpty()) {
			int crnt = visitQueue.dequeue();

			for (Friend frd = g.members[crnt].first; frd != null; frd = frd.next) {
				if (!visited[frd.fnum]) {
					prev[frd.fnum] = crnt;
					visited[frd.fnum] = true;

					if (g.members[frd.fnum].name.equals(p2)) {
						p2num = frd.fnum;
						chain.add(g.members[p2num].name);
						break;
					}

					visitQueue.enqueue(frd.fnum);
				}
			}

			if (p2num != -1) {
				break;
			}
		}

		if (p2num == -1) {
			System.out.println("No path between " + p1 + " and " + p2);
			return chain;
		}

		int placeholder = p2num;

		while (placeholder != p1num) {
			placeholder = prev[placeholder];
			chain.add(0, g.members[placeholder].name);
		}

		return chain;
	}
	
	/**
	 * Finds all cliques of students in a given school.
	 * 
	 * Returns an array list of array lists - each constituent array list contains
	 * the names of all students in a clique.
	 * 
	 * @param g Graph for which cliques are to be found.
	 * @param school Name of school
	 * @return Array list of clique array lists. Null or empty array list if there is no student in the
	 *         given school
	 */
	public static ArrayList<ArrayList<String>> cliques(Graph g, String school) {
		boolean exist = false;
		ArrayList<ArrayList<String>> cliques = new ArrayList<ArrayList<String>>();

		for (int i = 0; i < g.members.length; i++) {
			if (g.members[i].student == true) {
				if (g.members[i].school.equals(school)) {
					exist = true;
					break;
				}
			}
		}

		if (!exist) {
			System.out.println("School not found. Enter a new school");
			return cliques;
		}

		boolean visited[] = new boolean[g.members.length];
		Queue<Integer> visitQueue = new Queue<Integer>();

		for (int i = 0; i < g.members.length; i++) {
			ArrayList<String> crntNames = new ArrayList<String>();

			if (g.members[i].student && !visited[i]) {
				if (g.members[i].school.equals(school)) {
					crntNames.add(g.members[i].name);
					visited[i] = true;
					visitQueue.enqueue(i);
				}
			}

			while (!visitQueue.isEmpty()) {
				int crnt = visitQueue.dequeue();

				for (Friend frd = g.members[crnt].first; frd != null; frd = frd.next) {
					if (!visited[frd.fnum] && g.members[frd.fnum].student && g.members[frd.fnum].school.equals(school)) {
						visited[frd.fnum] = true;
						crntNames.add(g.members[frd.fnum].name);

						if (crnt != frd.fnum) {
							visitQueue.enqueue(frd.fnum);
						}
					}
				}
			}

			if (!crntNames.isEmpty()) {
				cliques.add(crntNames);
			}
		}
	
		return cliques;
	}
	
	/**
	 * Finds and returns all connectors in the graph.
	 * 
	 * @param g Graph for which connectors needs to be found.
	 * @return Names of all connectors. Null or empty array list if there are no connectors.
	 */
	public static ArrayList<String> connectors(Graph g) {
        ArrayList<String> connectors = new ArrayList<String>();
        
        if (g.members.length < 3) {
            return connectors;
        }

		for (int i = 0; i < g.members.length; i++) {
			int numFriends = 0;

			for (Friend frd = g.members[i].first; frd != null; frd = frd.next) {
				numFriends++;
			}

			if (numFriends == 1 && !connectors.contains(g.members[g.members[i].first.fnum].name)) {
				connectors.add(g.members[g.members[i].first.fnum].name);
			}
		}

		return connectors;
	}
}