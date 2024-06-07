import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Scanner;


public class SnippetProcessor {
	
	private String[] query_words;
	private String doc;
	private int num_sentences;
	private Double max_sentence_length_in_doc;
	private final StopWords stopWords;
	private final PorterStemmer porterStemmer;

	public SnippetProcessor() {
		stopWords = new StopWords();
		porterStemmer = new PorterStemmer();
	}

	public String GetSnippet(String doc_name, String query) {
		StringBuilder ret = new StringBuilder();
		doc = GetContentsOfDoc(doc_name);
		query_words = query.split(" ");
		if (doc != null) {
			HashMap<String, Double> scores = ScoreDoc();

			Double highest_score = 0.0;
			String top_sentence = "";
			double second_highest_score = 0.0;
			String second_top_sentence = "";
			for (Entry<String, Double> entry : scores.entrySet()) {
				String sentence = entry.getKey();
				Double score = entry.getValue();
				if (score > highest_score) {
					second_highest_score = highest_score;
					second_top_sentence = top_sentence;

					highest_score = score;
					top_sentence = sentence;
				}
				else if (score > second_highest_score) {
					second_highest_score = score;
					second_top_sentence = sentence;
				}
			}
			
			ret.append(doc_name).append("\n");

			String delimiter = "";
			String[] words = top_sentence.replaceAll("\\.", "").split(" ");
			for (int i = 0; i < words.length; i++) {
				ret.append(delimiter);
				delimiter = " ";
				for (String query_word : query_words) {
					if (i < words.length && !stopWords.contains(words[i].toLowerCase()) && porterStemmer.stem(words[i].toLowerCase()).equals(porterStemmer.stem(query_word))) {
						ret.append("<b>").append(words[i]).append("</b> ");
						i++;
						continue;
					}
				}
				if (i < words.length) ret.append(words[i]);
			}
			ret.append(". ");
			if (!second_top_sentence.equals("")) {
				words = second_top_sentence.replaceAll("\\.", "").split(" ");
				for (int i = 0; i < words.length; i++) {
					ret.append(delimiter);
					delimiter = " ";
					for (String query_word : query_words) {
						if (i < words.length && !stopWords.contains(words[i]) && porterStemmer.stem(words[i].toLowerCase()).equals(porterStemmer.stem(query_word))) {
							ret.append("<b>").append(words[i]).append("</b> ");
							i++;
							continue;
						}
					}
					if (i < words.length) ret.append(words[i]);
				}
				ret.append(". ");
			}
		}
		
		query_words = null;
		doc = null;
		num_sentences = 0;
		max_sentence_length_in_doc = null;

		return ret.toString();
	}

	private HashMap<String, Double> ScoreDoc() {
		HashMap<String, Double> scores = new HashMap<String, Double>();
		String[] sentences = doc.replaceAll("[^\\w.\\s]|[\\r]|( \\.\\.\\.)", "").replaceAll("\\.? ?(\\n{4}|\\n{2})", "\n").split("\n|\\.(?!\\d)|(?<!\\d)\\.");
		num_sentences = sentences.length;
		String[] originals = doc.replaceAll("[\\r]|( \\.\\.\\.)", "").replaceAll("(\\.')", "'.").replaceAll("(\\.\")", "\".").replaceAll("\\.? ?(\\n{4}|\\n{2})", "\n").split("\n|\\.(?!\\d)|(?<!\\d)\\.");
		assert sentences.length == originals.length;
		for (int i = 0; i < originals.length; i++) {
			originals[i] += ".";
		}

		// determine the length of the largest sentence
		max_sentence_length_in_doc = 0.0;
		for (String sentence : sentences) {
			double length = sentence.length() - sentence.replace(" ", "").length() + 1;
			if (length > max_sentence_length_in_doc)
				max_sentence_length_in_doc = length;
		}
		
		for (int i = 0; i < sentences.length; i++) {
			Double score = ScoreSentence(sentences[i], i);
			scores.put(originals[i], score);
		}

		return scores;
	}

	private Double ScoreSentence(String sentence, int index) {
			double score = 0.0;
			String[] words = sentence.split(" ");
			if (words.length < 2) return 0.0;

			// looks like this: [w, w, s, s, w, w, s, w, w]
			String[] markers = new String[words.length];
			for (int j = 0; j < markers.length; j++) {
				for (int k = 0; k < query_words.length; k++) {
					if (query_words[k].equals(words[j].toLowerCase())) {
						markers[j] = "s";
						k = (j == markers.length-1) ? query_words.length : -1;
						j++;
					}
				}
				if (j < markers.length) markers[j] = "w";
			}

			if (index == 0) score += (0.1 * num_sentences);
			else if (index == 1) score += (0.05 * num_sentences);

			double query_count = 0.0;
			for (String marker : markers) {
				if (marker.equals("s"))
					query_count++;
			}
			query_count = (query_count < 2) ? 0.05 : Math.log(query_count);
			score += query_count / ((double)query_words.length) * max_sentence_length_in_doc / ((double)words.length);

			double unique_query_count = 0.0;
			for (int j = 0; j < query_words.length; j++) 
				for (int k = 0; k < words.length; k++) 
					if (query_words[j].equals(words[k].toLowerCase())) {
						unique_query_count++;
						k = (j == query_words.length-1) ? words.length : -1; 
						j++;
					}
			unique_query_count = (unique_query_count < 2) ? 0.05 : Math.log(unique_query_count) ;
			score += unique_query_count / ((double)query_words.length);


			double longest = 0.0;
			double current_longest = 0.0;
			for (String marker : markers) {
				if (marker.equals("s"))
					current_longest++;
				if (current_longest > longest)
					longest = current_longest;
				else
					current_longest = 0;
			}
			score += Math.log(longest) / Math.log((double)words.length);

			int j = markers.length - 1;
			while (j >= 0 && markers[j].equals("w")) 
				markers[j--] = "*";

			j = 0;
			while (markers[j].equals("w")) 
				markers[j++] = "*";

			int consecutive_w = 0;
			for (  ; j < markers.length; j++) {
				if (markers[j].equals("s")) {
					if (consecutive_w > 4) {
						while (consecutive_w != 0) {
							markers[j-consecutive_w] = "*";
							consecutive_w--;
						}
					}
					else {
						consecutive_w = 0;
					}
				}
				else if (markers[j].equals("*")) {
					break;
				}
				else {
					consecutive_w++;
				}
			}
			double max_s = 0;
			double max_l = 0;
			int cur_s = 0;
			int cur_l = 0;
			for (j = 0; j < markers.length; j++) {
				switch (markers[j]) {
					case "s":
						if (++cur_s > max_s) max_s = cur_s;
						if (++cur_l > max_l) max_l = cur_l;

						break;
					case "w":
						if (++cur_l > max_l) max_l = cur_l;
						break;
					case "*":
						cur_s = 0;
						cur_l = 0;
						break;
				}
			}
			Double significance_factor = (max_s * max_s / max_l);
			significance_factor = (significance_factor.equals(Double.NaN)) ? 0 : significance_factor ;
			score += ((significance_factor > 1) ? Math.log(significance_factor) : significance_factor / 10);

			double stem_count = 0.0;
			String[] stemmed_query = new String[query_words.length];
			String[] stemmed_words = new String[words.length];
			for (j = 0; j < query_words.length; j++) {
				stemmed_query[j] = porterStemmer.stem(query_words[j].toLowerCase());
				for (int k = 0; k < words.length; k++) {
					stemmed_words[k] = porterStemmer.stem(words[k].toLowerCase());
					if (stemmed_words[k].equals(stemmed_query[j])) {
						stem_count++;
					}
				}
			}
			stem_count = (stem_count < 2) ? 0.1 : Math.log(stem_count) ;
			score += stem_count / words.length;
			
			double sw_count = 0.0;
			ArrayList<String> sw_query = new ArrayList<String>();
			for (j = 0; j < query_words.length; j++) {
				if (!stopWords.contains(query_words[j])) {
					sw_query.add(stemmed_query[j]);
				}
			}
			for (j = 0; j < sw_query.size(); j++) {
				for (int k = 0; k < stemmed_words.length; k++) {
					if (!stopWords.contains(words[k]) && sw_query.get(j).equals(stemmed_words[k])) {
						sw_count++;
					}
				}
			}
			sw_count = (sw_count < 2) ? 0.1 : Math.log(sw_count*2) ;
			score += sw_count;
			
			for (j = 0; j < stemmed_words.length; j++) {
				if (stemmed_words[j].equals(stemmed_query[0])) {
					boolean full_query = true;
					for (int a = j, b = 0; (a < stemmed_words.length) && (b < stemmed_query.length); a++, b++) {
						if (!stemmed_words[a].equals(stemmed_query[b])) {
							full_query = false;
							break;
						}
					}
					if (full_query) {
						score += 1.0;
						break;
					}
				}
			}
			return score;
	}
	
	private String GetContentsOfDoc(String doc_name) {
		try {
			return (new Scanner(new File(doc_name)).useDelimiter("\\A").next());
		} catch (Exception e) {
			try {
				BufferedReader r = new BufferedReader(new FileReader(doc_name));
			    StringBuilder sb = new StringBuilder();
			    String nextLine = "";

			    while ((nextLine = r.readLine()) != null) {
			    	nextLine = nextLine.replaceAll("[^\\p{L}\\p{Nd} \\.]+", "");
			        sb.append(nextLine);
			    }
			    r.close();
			    return sb.toString();
			}
			catch (Exception ex) {
				System.out.println("Could not open document: " + doc_name + "\n\n");
				e.printStackTrace();
				return null;
			}
		}
	}
}
