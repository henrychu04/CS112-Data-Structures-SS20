package lse;

import java.io.*;
import java.util.*;

/**
 * This class builds an index of keywords. Each keyword maps to a set of pages
 * in which it occurs, with frequency of occurrence in each page.
 *
 */
public class LittleSearchEngine {

	/**
	 * This is a hash table of all keywords. The key is the actual keyword, and the
	 * associated value is an array list of all occurrences of the keyword in
	 * documents. The array list is maintained in DESCENDING order of frequencies.
	 */
	HashMap<String, ArrayList<Occurrence>> keywordsIndex;

	/**
	 * The hash set of all noise words.
	 */
	HashSet<String> noiseWords;

	/**
	 * Creates the keyWordsIndex and noiseWords hash tables.
	 */
	public LittleSearchEngine() {
		keywordsIndex = new HashMap<String, ArrayList<Occurrence>>(1000, 2.0f);
		noiseWords = new HashSet<String>(100, 2.0f);
	}

	/**
	 * Scans a document, and loads all keywords found into a hash table of keyword
	 * occurrences in the document. Uses the getKeyWord method to separate keywords
	 * from other words.
	 * 
	 * @param docFile Name of the document file to be scanned and loaded
	 * @return Hash table of keywords in the given document, each associated with an
	 *         Occurrence object
	 * @throws FileNotFoundException If the document file is not found on disk
	 */
	public HashMap<String, Occurrence> loadKeywordsFromDocument(String docFile) throws FileNotFoundException {
		try {
			HashMap<String, Occurrence> map = new HashMap<String, Occurrence>();
			Scanner sc = new Scanner(new File(docFile));

			while (sc.hasNext()) {
				String word = sc.next();
				word = getKeyword(word);

				if (word != null) {
					if (map.containsKey(word)) {
						Occurrence newCrnt = map.get(word);
						newCrnt.frequency++;
						map.replace(word, newCrnt);
					} else {
						map.put(word, new Occurrence(docFile, 1));
					}
				}
			}

			sc.close();
			return map;
		} catch (FileNotFoundException e) {
			throw new FileNotFoundException();
		}
	}

	/**
	 * Merges the keywords for a single document into the master keywordsIndex hash
	 * table. For each keyword, its Occurrence in the current document must be
	 * inserted in the correct place (according to descending order of frequency) in
	 * the same keyword's Occurrence list in the master hash table. This is done by
	 * calling the insertLastOccurrence method.
	 * 
	 * @param kws Keywords hash table for a document
	 */
	public void mergeKeywords(HashMap<String, Occurrence> kws) {
		for (Map.Entry<String, Occurrence> crnt : kws.entrySet()) {
			if (keywordsIndex.containsKey(crnt.getKey())) {
				ArrayList<Occurrence> crntAL = keywordsIndex.get(crnt.getKey());

				crntAL.add(crnt.getValue());
				insertLastOccurrence(crntAL);
			} else {
				ArrayList<Occurrence> newAL = new ArrayList<Occurrence>();

				newAL.add(crnt.getValue());
				keywordsIndex.put(crnt.getKey(), newAL);
			}
		}
	}

	/**
	 * Given a word, returns it as a keyword if it passes the keyword test,
	 * otherwise returns null. A keyword is any word that, after being stripped of
	 * any trailing punctuation(s), consists only of alphabetic letters, and is not
	 * a noise word. All words are treated in a case-INsensitive manner.
	 * 
	 * Punctuation characters are the following: '.', ',', '?', ':', ';' and '!' NO
	 * OTHER CHARACTER SHOULD COUNT AS PUNCTUATION
	 * 
	 * If a word has multiple trailing punctuation characters, they must all be
	 * stripped So "word!!" will become "word", and "word?!?!" will also become
	 * "word"
	 * 
	 * See assignment description for examples
	 * 
	 * @param word Candidate word
	 * @return Keyword (word without trailing punctuation, LOWER CASE)
	 */
	public String getKeyword(String word) {
		String punctuation = ".,?:;!", finalWord = "";
		boolean startOfWord = false, endOfWord = false;

		for (int i = 0; i < word.length(); i++) {
			if (!startOfWord) {
				if (punctuation.contains(word.charAt(i) + "")) {
					continue;
				} else {
					startOfWord = true;
				}
			}

			if (startOfWord && !endOfWord) {
				if (!Character.isLetter(word.charAt(i))) {
					if (punctuation.contains(word.charAt(i) + "")) {
						endOfWord = true;
					} else {
						return null;
					}
				} else {
					finalWord += Character.toLowerCase(word.charAt(i));
				}
			}

			if (endOfWord) {
				if (Character.isLetter(word.charAt(i))) {
					return null;
				}
			}
		}

		if (noiseWords.contains(finalWord) || finalWord.length() == 0) {
			return null;
		}

		return finalWord;
	}

	/**
	 * Inserts the last occurrence in the parameter list in the correct position in
	 * the list, based on ordering occurrences on descending frequencies. The
	 * elements 0..n-2 in the list are already in the correct order. Insertion is
	 * done by first finding the correct spot using binary search, then inserting at
	 * that spot.
	 * 
	 * @param occs List of Occurrences
	 * @return Sequence of mid point indexes in the input list checked by the binary
	 *         search process, null if the size of the input list is 1. This
	 *         returned array list is only used to test your code - it is not used
	 *         elsewhere in the program.
	 */
	public ArrayList<Integer> insertLastOccurrence(ArrayList<Occurrence> occs) {
		if (occs.size() == 1) {
			return null;
		}

		ArrayList<Integer> indexes = new ArrayList<Integer>();
		Occurrence toBeInserted = occs.remove(occs.size() - 1);

		int low = 0;
		int high = occs.size() - 1;

		while (low <= high) {
			int middle = (low + high) / 2;
			indexes.add(middle);

			if (occs.get(middle).frequency == toBeInserted.frequency) {
				occs.add(middle + 1, toBeInserted);
				break;
			} else if (occs.get(middle).frequency > toBeInserted.frequency) {
				low = middle + 1;
				continue;
			} else if (occs.get(middle).frequency < toBeInserted.frequency) {
				high = middle - 1;
				continue;
			}
		}

		if (high < low) {
			occs.add(low, toBeInserted);
		}

		return indexes;
	}

	/**
	 * This method indexes all keywords found in all the input documents. When this
	 * method is done, the keywordsIndex hash table will be filled with all
	 * keywords, each of which is associated with an array list of Occurrence
	 * objects, arranged in decreasing frequencies of occurrence.
	 * 
	 * @param docsFile       Name of file that has a list of all the document file
	 *                       names, one name per line
	 * @param noiseWordsFile Name of file that has a list of noise words, one noise
	 *                       word per line
	 * @throws FileNotFoundException If there is a problem locating any of the input
	 *                               files on disk
	 */
	public void makeIndex(String docsFile, String noiseWordsFile) throws FileNotFoundException {
		// load noise words to hash table
		Scanner sc = new Scanner(new File(noiseWordsFile));
		while (sc.hasNext()) {
			String word = sc.next();
			noiseWords.add(word);
		}

		// index all keywords
		sc = new Scanner(new File(docsFile));
		while (sc.hasNext()) {
			String docFile = sc.next();
			HashMap<String, Occurrence> kws = loadKeywordsFromDocument(docFile);
			mergeKeywords(kws);
		}
		sc.close();
	}

	/**
	 * Search result for "kw1 or kw2". A document is in the result set if kw1 or kw2
	 * occurs in that document. Result set is arranged in descending order of
	 * document frequencies.
	 * 
	 * Note that a matching document will only appear once in the result.
	 * 
	 * Ties in frequency values are broken in favor of the first keyword. That is,
	 * if kw1 is in doc1 with frequency f1, and kw2 is in doc2 also with the same
	 * frequency f1, then doc1 will take precedence over doc2 in the result.
	 * 
	 * The result set is limited to 5 entries. If there are no matches at all,
	 * result is null.
	 * 
	 * See assignment description for examples
	 * 
	 * @param kw1 First keyword
	 * @param kw1 Second keyword
	 * @return List of documents in which either kw1 or kw2 occurs, arranged in
	 *         descending order of frequencies. The result size is limited to 5
	 *         documents. If there are no matches, returns null or empty array list.
	 */
	public ArrayList<String> top5search(String kw1, String kw2) {
		ArrayList<String> finalList = new ArrayList<String>();

		if (keywordsIndex.containsKey(kw1) && keywordsIndex.containsKey(kw2)) {
			System.out.println("Both " + "'" + kw1 + "'" + " and " + "'" + kw2 + "'" + " found\nsearching...\n");

			ArrayList<Occurrence> all = new ArrayList<Occurrence>();
			all.addAll(keywordsIndex.get(kw1));
			all.addAll(keywordsIndex.get(kw2));

			while (finalList.size() < 5 && all.size() != 0) {
				int max = -1;

				for (int i = 0; i < all.size(); i++) {
					if (all.get(i).frequency > max) {
						max = all.get(i).frequency;
					}
				}

				for (int i = 0; i < all.size(); i++) {
					if (all.get(i).frequency == max) {
						if (!finalList.contains(all.get(i).document)) {
							finalList.add(all.remove(i).document);
							break;
						} else {
							all.remove(i);
							break;
						}
					}
				}
			}
		} else if (!keywordsIndex.containsKey(kw1) && keywordsIndex.containsKey(kw2)) {
			System.out.println("'" + kw1 + "'" + " not found and " + "'" + kw2 + "'" + " found\nReturning " + "'" + kw2
					+ "'" + " ArrayList\n");

			for (int i = 0; finalList.size() < 5 && i < keywordsIndex.get(kw2).size(); i++) {
				if (!finalList.contains(keywordsIndex.get(kw2).get(i).document)) {
					finalList.add(keywordsIndex.get(kw2).get(i).document);
				}
			}
		} else if (keywordsIndex.containsKey(kw1) && !keywordsIndex.containsKey(kw2)) {
			System.out.println("'" + kw1 + "'" + " found and " + "'" + kw2 + "'" + " not found\nReturning " + "'" + kw1
					+ "'" + " ArrayList\n");

			for (int i = 0; finalList.size() < 5 && i < keywordsIndex.get(kw1).size(); i++) {
				if (!finalList.contains(keywordsIndex.get(kw1).get(i).document)) {
					finalList.add(keywordsIndex.get(kw1).get(i).document);
				}
			}
		} else {
			System.out.println("No match");
		}

		return finalList;
	}
}