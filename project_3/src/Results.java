import java.util.*;

public class Results {
	public int[] doc_ids;
	public String[] first_sentences;
	public double[] top_scores;
	
	TextProcessor tp;
	
	public Results(TextProcessor tp, double[] scores) {
		doc_ids = new int[10];
		first_sentences = new String[10];
		top_scores = new double[10];
		this.tp = tp;
		
		for (int i = 0; i < scores.length; i++) {
			if (scores[i] > top_scores[9] || doc_ids[9] == 0) {
				BubbleUp(i, scores[i]);
			}
		}
	}
	
	public String GetDocIDByIndex(int result_index) {
		if (result_index < 10 && 0 <= result_index) 
			return "wikidocs/Doc (" + (doc_ids[result_index]+1) + ").txt";
		else return null;
	}
	
	private void BubbleUp(int doc_id, double score) {
		int i = 9;
		while (i >= 0 && (score > top_scores[i] || doc_ids[i] == 0)) {
			if (i != 9) {
				doc_ids[i+1] = doc_ids[i];
				first_sentences[i+1] = first_sentences[i];
				top_scores[i+1] = top_scores[i];
			}
			i--;
		}
		i++;
		doc_ids[i] = doc_id;
		first_sentences[i] = tp.GetFirstSentence(doc_id);
		top_scores[i] = score;
	}
	
	public String toString() {
		StringBuilder ret = new StringBuilder();
		Formatter f = new Formatter(ret, Locale.US);
		for (int i = 0; i < 10; i++) {
			if (doc_ids[i] > 0) {
				f.format("Doc (%d).txt\t", doc_ids[i]+1);
				f.format("%s\t", first_sentences[i]);
				f.format("%f\t\n", top_scores[i]);
			}
			else {
				f.format("From:  none\n");
				f.format("Summary:  ---\n");
				f.format("Score:  ---\n\n");
			}
		}
		f.close();
		return ret.toString();
	}
}
