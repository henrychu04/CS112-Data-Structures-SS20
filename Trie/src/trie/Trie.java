package trie;

import java.util.ArrayList;

/**
 * This class implements a Trie. 
 * 
 * @author Sesh Venugopal
 *
 */
public class Trie {

	enum MATCH_TYPE {
		FULL_MATCH,
		PARTIAL_MATCH,
		NO_MATCH
	}
	
	// prevent instantiation
	private Trie() { }
	
	/**
	 * Builds a trie by inserting all words in the input array, one at a time,
	 * in sequence FROM FIRST TO LAST. (The sequence is IMPORTANT!)
	 * The words in the input array are all lower case.
	 * 
	 * @param allWords Input array of words (lowercase) to be inserted.
	 * @return Root of trie with all words inserted from the input array
	 */
	public static TrieNode buildTrie(String[] allWords) {
		TrieNode root = new TrieNode(null, null, null);
		TrieNode crntNode = root;
	
			for(int i = 0; i < allWords.length; i++) {
				Indexes newIndex = new Indexes(i, (short)0, (short)(allWords[i].length()-1));
				TrieNode newNode = new TrieNode(newIndex, null, null);

				if(i == 0) {
					crntNode.firstChild = newNode;
					crntNode = newNode;
					continue;
				}

				crntNode = root.firstChild;

				boolean ret = findPlaceToInsert(crntNode, newNode, allWords);
			
				if(ret == false) {
					break;
				}
			}
		return root;
	}

	private static boolean findPlaceToInsert(TrieNode crntNode, TrieNode newNode, String[] allWords) {
		boolean ret = false;

		switch(ifMatch(crntNode, newNode, allWords)) {
			case FULL_MATCH:
				if(crntNode.firstChild == null) {
					System.out.println("A string either fully matches a previous string or is a prefix of an existing string. Program is stopped.");
					ret = false;
					break;
				}

				ret = findPlaceToInsert(crntNode.firstChild, newNode, allWords);
				break;
			case PARTIAL_MATCH:
				String one = allWords[crntNode.substr.wordIndex].substring(crntNode.substr.startIndex, crntNode.substr.endIndex + 1);
				String two = allWords[newNode.substr.wordIndex].substring(crntNode.substr.startIndex, newNode.substr.endIndex + 1);

				int count = 0;
				int length = 0;

				if(one.length() > two.length()) {
					length = two.length();
				} else if(two.length() >= one.length()) {
					length = one.length();
				}

				for(int i = 0; i < length - 1; i++) {
					if(one.charAt(i) != two.charAt(i)) {
						break;
					} else {
						count++;
					}
				}

				if(crntNode.firstChild == null) {
					Indexes newIndexes = new Indexes(crntNode.substr.wordIndex, (short)(crntNode.substr.startIndex + count), crntNode.substr.endIndex);
					TrieNode newTrieNode = new TrieNode(newIndexes, null, newNode);

					int newTrieNode_length = newTrieNode.substr.endIndex - newTrieNode.substr.startIndex + 1;

					newNode.substr.startIndex = (short)(crntNode.substr.startIndex + count);
					crntNode.substr.endIndex -= (short)(newTrieNode_length);

					crntNode.firstChild = newTrieNode;
					ret = true;
				} else {
					Indexes newIndexes = new Indexes(crntNode.substr.wordIndex, (short)(count), crntNode.substr.endIndex);
					TrieNode newTrieNode = new TrieNode(newIndexes, crntNode.firstChild, null);

					int newTrieNode_length = newTrieNode.substr.endIndex - newTrieNode.substr.startIndex + 1;

					crntNode.firstChild = newTrieNode;
					newNode.substr.startIndex = (short)(count);
					newTrieNode.sibling = newNode;
					crntNode.substr.endIndex -= (short)(newTrieNode_length);

					ret = true;
				}
				break;
			case NO_MATCH:
				if(crntNode.sibling == null) {
					crntNode.sibling = newNode;
					newNode.substr.startIndex = crntNode.substr.startIndex;
					return true;
				}

				ret = findPlaceToInsert(crntNode.sibling, newNode, allWords);
				break;
			default:
				ret = false;
				break;
		}
		return ret;
	}

	private static Trie.MATCH_TYPE ifMatch(TrieNode root1, TrieNode root2, String[] allWords) {
		String one = allWords[root1.substr.wordIndex].substring(root1.substr.startIndex, root1.substr.endIndex + 1);
		String two = allWords[root2.substr.wordIndex].substring(root1.substr.startIndex, root2.substr.endIndex + 1);

		int count = 0;
		int length = 0;		

		if(one.length() > two.length()) {
			length = two.length();
		} else if(two.length() >= one.length()) {
			length = one.length();
		}

		for(int i = 0; i < length; i++) {
			if(one.charAt(i) != two.charAt(i)) {
				break;
			} else {
				count++;
			}
		}

		if(count == 0) {
			return Trie.MATCH_TYPE.NO_MATCH;
		} else if(count == length) {
			return Trie.MATCH_TYPE.FULL_MATCH;
		} else if(count < length) {
			return Trie.MATCH_TYPE.PARTIAL_MATCH;
		}
		return Trie.MATCH_TYPE.NO_MATCH;
	}

	/**
	 * Given a trie, returns the "completion list" for a prefix, i.e. all the leaf nodes in the 
	 * trie whose words start with this prefix. 
	 * For instance, if the trie had the words "bear", "bull", "stock", and "bell",
	 * the completion list for prefix "b" would be the leaf nodes that hold "bear", "bull", and "bell"; 
	 * for prefix "be", the completion would be the leaf nodes that hold "bear" and "bell", 
	 * and for prefix "bell", completion would be the leaf node that holds "bell". 
	 * (The last example shows that an input prefix can be an entire word.) 
	 * The order of returned leaf nodes DOES NOT MATTER. So, for prefix "be",
	 * the returned list of leaf nodes can be either hold [bear,bell] or [bell,bear].
	 *
	 * @param root Root of Trie that stores all words to search on for completion lists
	 * @param allWords Array of words that have been inserted into the trie
	 * @param prefix Prefix to be completed with words in trie
	 * @return List of all leaf nodes in trie that hold words that start with the prefix, 
	 * 			order of leaf nodes does not matter.
	 *         If there is no word in the tree that has this prefix, null is returned.
	 */
	public static ArrayList<TrieNode> completionList(TrieNode root,
										String[] allWords, String prefix) {
		if(root.firstChild == null) {
			return null;
		}

		TrieNode crntNode = root.firstChild;

		ArrayList<TrieNode> returningArray = new ArrayList<TrieNode>();

		findMatchStartingPoint(crntNode, prefix, allWords, returningArray);

		if(returningArray.size() == 0) {
			return null;
		}
		return returningArray;
	}

	private static Trie.MATCH_TYPE matchPrefix(TrieNode crntNode, String prefix, String[] allWords) {
		String one = allWords[crntNode.substr.wordIndex].substring(crntNode.substr.startIndex, crntNode.substr.endIndex + 1);
		String two = prefix.substring(crntNode.substr.startIndex);

		int count = 0;
		int length = 0;		

		if(one.length() > two.length()) {
			length = two.length();
		} else if(two.length() >= one.length()) {
			length = one.length();
		}

		for(int i = 0; i < length; i++) {
			if(one.charAt(i) != two.charAt(i)) {
				break;
			} else {
				count++;
			}
		}

		if(count == 0) {
			return MATCH_TYPE.NO_MATCH;
		} else if(count == two.length()) {
			return MATCH_TYPE.FULL_MATCH;
		} else if(count > 0) {
			return MATCH_TYPE.PARTIAL_MATCH;
		} 
		return MATCH_TYPE.NO_MATCH;
	}

	private static void findMatchStartingPoint(TrieNode crntNode, String prefix, String[] allWords, ArrayList<TrieNode> prefixList) {
		switch(matchPrefix(crntNode, prefix, allWords)) {
			case FULL_MATCH:
				printLeafNodes(crntNode, prefixList);
				break;
			case PARTIAL_MATCH:
				if(crntNode.firstChild == null) {
					break;
				} else {
					findMatchStartingPoint(crntNode.firstChild, prefix, allWords, prefixList);
				}
				break;
			case NO_MATCH:
				if(crntNode.sibling != null) {
					findMatchStartingPoint(crntNode.sibling, prefix, allWords, prefixList);
				} else {
					return;
				}
				break;
			default:
				break;
		}
		return;
	}

	private static void printLeafNodes(TrieNode crntNode, ArrayList<TrieNode> leafList){
		if(crntNode.firstChild == null) {
			leafList.add(crntNode);
		}
		
		for (TrieNode ptr=crntNode.firstChild; ptr != null; ptr=ptr.sibling) {
			printLeafNodes(ptr, leafList);
		}
		return;
	}
	
	public static void print(TrieNode root, String[] allWords) {
		System.out.println("\nTRIE\n");
		print(root, 1, allWords);
	}
	
	private static void print(TrieNode root, int indent, String[] words) {
		if (root == null) {
			return;
		}
		for (int i=0; i < indent-1; i++) {
			System.out.print("    ");
		}
		
		if (root.substr != null) {
			String pre = words[root.substr.wordIndex]
							.substring(0, root.substr.endIndex+1);
			System.out.println("      " + pre);
		}
		
		for (int i=0; i < indent-1; i++) {
			System.out.print("    ");
		}
		System.out.print(" ---");
		if (root.substr == null) {
			System.out.println("root");
		} else {
			System.out.println(root.substr);
		}
		
		for (TrieNode ptr=root.firstChild; ptr != null; ptr=ptr.sibling) {
			for (int i=0; i < indent-1; i++) {
				System.out.print("    ");
			}
			System.out.println("     |");
			print(ptr, indent+1, words);
		}
	}
 }