import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class QueryAnalyzer {

	private Dictionary d;
	private TextProcessor t;
//					queryID	query
	private HashMap<String, String> queries;
//					word	count
	private HashMap<String, Integer> corrections;
//				misspelled			correct 	count
	private HashMap<String, HashMap<String, Integer>> misspelled;

	public QueryAnalyzer(Dictionary dict, TextProcessor tp) {
		d = dict;
		t = tp;
		queries = new HashMap<String, String>();
		corrections = new HashMap<String, Integer>();
		misspelled = new HashMap<String, HashMap<String, Integer>>();
	}

	public String[] Possible(String e) {
		HashMap<String, Integer> possibilities = misspelled.get(e);
		String[] ret = new String[possibilities.size()];
		int i = 0;
		for (String key : possibilities.keySet()) {
			ret[i++] = key;
		}
		return ret;
	}
	
	public String Correct(String query) {
		if (NoMisspells(query)) return query;

		String[] words = query.replace("[^a-zA-Z]", "").split(" ");
		StringBuilder ret = new StringBuilder();
		String pre = "";
		for (String w : words) {
			ret.append(pre);
			pre = " ";
			// misspelled word!!!!!
			if (!d.ContainsWord(w))
				ret.append(FindCorrection(w));
			else 
				ret.append(w);
		}
		return ret.toString();
	}

	public void AddCorrectQuery(String id, String q) {
		if (NoMisspells(q)) 
			queries.put(id, q);
	}

	public void AddIncorrectQuery(String id, String q) {
		if (!NoMisspells(q)) {
			String[] temp = q.split(" ");
			for (int i = 0; i < temp.length; i++) {
				// if this word is misspelled
				if (!d.ContainsWord(temp[i])) {

					String e = temp[i];

					// find if query log has a correction
					if (queries.containsKey(id)) {
						String w = "";
						if (queries.get(id).split(" ").length > i) 
							w = queries.get(id).split(" ")[i];
						else
							return;


						// update MISSPELLING table
						HashMap<String, Integer> correct;
						Integer count = 1;
						if (misspelled.containsKey(e)) {
							correct = misspelled.get(e);
							// e has been corrected to w, so increment count
							if (correct.containsKey(w)) 
								count = (correct.get(w)) + 1;
						}
						// e has not been seen before
						else 
							correct = new HashMap<String, Integer>();
						
						correct.put(w, count);
						misspelled.put(e, correct);


						// update CORRECTIONS table
						if (corrections.containsKey(w)) 
							count = (corrections.get(w)) + 1;
						else 
							count = 1;

						corrections.put(w, count);
					}
				}
			}
		}
	}

	@SuppressWarnings("rawtypes")
	private String FindCorrection(String e) {
		String ret = "NULL";
		Double highestScore = 0.0;

		HashMap<String, Integer> possibilities = misspelled.get(e);
		Iterator it;
		try {
			it = possibilities.entrySet().iterator();
		}
		catch (Exception ex) {
			return ret;
		}
		Double score = 0.0;
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			String w = (String) pair.getKey();
			int count = (int) pair.getValue();
			if (Util.EditDistance(e, w) <= 2) {
				Double a = (double) count;
				Double b = (double) corrections.get(w);
				Double c = (double) t.FindFrequencyByDocument(t.stemmer.stem(w), -1);
				Double d = (double) t.WordCount(0);
				score = (double) (a/b);
				score *= (double) (c/d);
			}
//			it.remove();
			if (score > highestScore) {
				highestScore = score;
				ret = w;
			}
		}

		return ret;
	}

	private boolean NoMisspells(String q) {
		String[] words = q.split(" ");
		for (String w : words)
			if (!d.ContainsWord(w))
				return false;

		return true;
	}

	class Tuple<X, Y> { 
		public final X x; 
		public final Y y; 
		public Tuple(X x, Y y) { 
			this.x = x; 
			this.y = y; 
		} 
	} 
}
