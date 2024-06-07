import java.io.*;
import java.util.*;

public class TextProcessor {
	
	private static final int NUM_DOCS_TO_PROCESS = 322;
	private final StopWords stopwords;
	public PorterStemmer stemmer;
	private final Map<String, int[]> index;

	private final int[] wordcount;
	private int[] max_freq_of_doc;

	public TextProcessor() {
		stopwords = new StopWords();
		stemmer = new PorterStemmer();
		index = new HashMap<>();
		wordcount = new int[NUM_DOCS_TO_PROCESS];
		max_freq_of_doc = null;
	}
	
	public String GetFirstSentence(int doc_num) {
		
		try {
			doc_num++;
			BufferedReader in = new BufferedReader(new FileReader("resources/wikidocs/Doc (" + doc_num + ").txt"));
			String nextLine;
			if ((nextLine = in.readLine()) != null) {
				in.close();
				String firstSentence = nextLine.split("(?<=[a-z])\\.\\s+")[0];
				firstSentence += ".";
				return firstSentence;
			}
			in.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public int[][] FindRelevantDocs(String[] q) {
		int[][] relevant = new int[NUM_DOCS_TO_PROCESS][q.length];
		for (int i = 0; i < q.length; i++) {
			for (int j = 0; j < NUM_DOCS_TO_PROCESS; j++) {
				relevant[j][i] = FindFrequencyByDocument(q[i], j);
			}
		}
		return relevant;
	}
	
	public int[] MaxFreqOfDoc(int doc_num) {
		if (doc_num < -1 || doc_num >= NUM_DOCS_TO_PROCESS) return null;
		
		else if (max_freq_of_doc != null) {
			if (doc_num == -1) {
				return max_freq_of_doc;
			}
			else {
				int[] max = new int[1];
				max[0] = max_freq_of_doc[doc_num];
				return max;
			}
		}

		int[] max;
		if (doc_num > -1) {
			max = new int[1];

			for (Map.Entry<String, int[]> stringEntry : index.entrySet()) {
				int val = ((int[]) ((Map.Entry<?, ?>) stringEntry).getValue())[doc_num];
				if (val > max[0]) max[0] = val;
			}

		}
		else {
			max = new int[NUM_DOCS_TO_PROCESS];

			for (Map.Entry<String, int[]> stringEntry : index.entrySet()) {
				for (int i = 0; i < NUM_DOCS_TO_PROCESS; i++) {
					int val = ((int[]) ((Map.Entry<?, ?>) stringEntry).getValue())[i];
					if (val > max[i]) max[i] = val;
				}
			}

		}
		return max;
	}
	
	public int NumDocsWordIsIn(String word) {
		int docs = 0;
		int[] freq = index.get(word);
		if (freq == null) return 0;

		for (int j : freq) {
			if (j > 0) docs++;
		}
		return docs;
	}

	// wants a doc_num as labeled in the file
	public int WordCount(int doc_num) {
		if (doc_num >= NUM_DOCS_TO_PROCESS || doc_num <= -1) return -1;	// document out of range
		
		if (doc_num == 0) {
			int sum = 0;
			for (int j : wordcount) sum += j;
			return sum;
		}
		else
			return wordcount[doc_num - 1];
	}
	
	public int FindFrequencyByDocument(String word, int doc_num) {
		int[] freq = index.get(word);
		if (freq == null) return 0;										// word not found
		if (doc_num >= NUM_DOCS_TO_PROCESS || doc_num < -1) return -1;	// document out of range
		
		if (doc_num == -1) {
			int sum = 0;
			for (int j : freq) sum += j;
			return sum;
		}
		else { 
			return freq[doc_num];
		}
	}
	
	public void TestIndex() {
		for (Map.Entry<String, int[]> stringEntry : index.entrySet()) {
			int[] value = (int[]) ((Map.Entry<?, ?>) stringEntry).getValue();
			System.out.println(((Map.Entry<?, ?>) stringEntry).getKey() + " = " + value[0] + " " + value[1] + " " + value[2] + " " + value[3]);
		}
	}
	
	public void InitFiles() {
		for (int doc_num = 0; doc_num < NUM_DOCS_TO_PROCESS; doc_num++) {
			String[] words;
			try {
				int num = doc_num + 1;
				BufferedReader bufferedReader = new BufferedReader(new FileReader("resources/wikidocs/Doc (" + num + ").txt"));
				StringBuilder str = new StringBuilder();
				String nextLine;
				while ((nextLine = bufferedReader.readLine()) != null) {
					str.append(nextLine).append("\n");
				}
				bufferedReader.close();

				String file_contents = str.toString();
				
				String[] c = file_contents.split("\\s+");
				wordcount[doc_num] = c.length;
				
				words = Tokenize(file_contents);
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}

			for (String word : words) {
				if (word == null) continue;
				int[] freq;
				if (index.containsKey(word)) {
					freq = index.get(word);
					freq[doc_num] += 1;
				} else {
					freq = new int[NUM_DOCS_TO_PROCESS];
					freq[doc_num] = 1;
				}
				index.put(word, freq);
			}
		}
		
		max_freq_of_doc = MaxFreqOfDoc(-1);
	}
	
	public String[] Tokenize(String phrase) {
		String[] words = phrase.replaceAll("[^a-zA-Z0-9 \n]", " ").toLowerCase().split("\\s+");
		
		for (int i = 0; i < words.length; i++) {
			if (stopwords.contains(words[i])) {
				words[i] = null;
				continue;
			}
			String temp = stemmer.stem(words[i]);
			if (!Objects.equals(temp, "Invalid term")) {
				words[i] = temp;
			}
		}
		
		return words;
	}
	
	private static class Tuple<X, Y> {
		public final X x;
		public final Y y;
		public Tuple(X x, Y y) {
			this.x = x;
			this.y = y;
		}
	}
}
