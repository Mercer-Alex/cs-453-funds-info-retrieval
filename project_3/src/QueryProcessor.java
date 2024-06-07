
public class QueryProcessor {

	private final TextProcessor textProcessor;
	
	public QueryProcessor(TextProcessor tp) {
		this.textProcessor = tp;
	}
	
	public Results Search(String query) {
		int NUM_DOCS_TO_PROCESS = 322;
		double[] result_scores = new double[NUM_DOCS_TO_PROCESS];
		

		String[] queries = textProcessor.Tokenize(query);
		int[][] docs = textProcessor.FindRelevantDocs(queries);
		int[] max_f = textProcessor.MaxFreqOfDoc(-1);
		// the number of documents in which the token appears
		int[] D_scores = new int[queries.length];
		for (int j = 0; j < queries.length; j++) {
			if (queries[j] == null) continue;
			
			for (int i = 0; i < result_scores.length; i++) {
				if (docs[i][j] != 0) D_scores[j] += 1;
			}
		}

		for (int i = 0; i < result_scores.length; i++) {
			double score = 0;
			int B = max_f[i];
			for (int j = 0; j < queries.length; j++) {
				if (queries[j] == null) continue;
				
				double A = docs[i][j];											// if (i==302) System.out.println(A + " " + B);
				double D = D_scores[j];
				
				score += CalculateScore(A, B, NUM_DOCS_TO_PROCESS, D);
			}
			result_scores[i] = score;
		}
		
		return new Results(textProcessor, result_scores);
	}
	
	public double CalculateScore(double A, double B, double C, double D) {
		double tf = A / B;
		double idf_one = Math.log(C)/Math.log(2);
		double idf_two = Math.log(D)/Math.log(2);

		return tf * (idf_one - idf_two);
	}
}
