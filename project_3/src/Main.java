import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


public class Main {
	private static final String[] queries = new String[] {
		"sentenced to prision",
		"open cuort case",
		"entretainment group",
		"tv axtor",
		"scheduled movie screning"
	};

	public static void main(String[] args) {
		Dictionary d = new Dictionary();
		try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader("resources/dictionary.txt"));
			String nextLine;
			while ((nextLine = bufferedReader.readLine()) != null) {
				nextLine = nextLine.replace("[^a-zA-Z]", "");
				d.AddWord(nextLine);
			}
			bufferedReader.close();
		} 
		catch (IOException e) {
			System.out.println("ERROR: problem reading dictionary file.");
			e.printStackTrace();
			return;
		}

		TextProcessor textProcessor = new TextProcessor();
		textProcessor.InitFiles();
		QueryAnalyzer queryAnalyzer = new QueryAnalyzer(d, textProcessor);
		try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader("resources/query_log.txt"));
			String nextLine = bufferedReader.readLine();
			while ((nextLine = bufferedReader.readLine()) != null) {
				String id = nextLine.split("\t")[0];
				String query = nextLine.split("\t")[1].replace("[^a-zA-Z]", "").toLowerCase();
				queryAnalyzer.AddCorrectQuery(id, query);
			}
			bufferedReader.close();

			bufferedReader = new BufferedReader(new FileReader("resources/query_log.txt"));
			nextLine = bufferedReader.readLine();
			while ((nextLine = bufferedReader.readLine()) != null) {
				String id = nextLine.split("\t")[0];
				String query = nextLine.split("\t")[1].replace("[^a-zA-Z]", "").toLowerCase();
				queryAnalyzer.AddIncorrectQuery(id, query);
			}
			bufferedReader.close();
		}
		catch (IOException e) {
			System.out.println("ERROR: problem reading query file.");
			e.printStackTrace();
			return;
		}

		String[] corrections = new String[queries.length];
		String[] misspelled_soundex_codes = new String[queries.length];
		ArrayList<String[]> suggestedWords = new ArrayList<String[]>();
		for (int i = 0; i < queries.length; i++) {
			corrections[i] = queryAnalyzer.Correct(queries[i]);
			String[] words = queries[i].split(" ");

			for (int j = 0; j < words.length; j++)
				if (!d.ContainsWord(words[j])) {
					misspelled_soundex_codes[i] = Util.Soundex(words[j]);
					suggestedWords.add(queryAnalyzer.Possible(queries[i].split(" ")[j]));
				}
		}
		
		QueryProcessor queryProcessor = new QueryProcessor(textProcessor);
		SnippetProcessor snippetProcessor = new SnippetProcessor();
		Results[] results = new Results[queries.length];
		String[][] snippets = new String[queries.length][5];

		for (int i = 0; i < corrections.length; i++) {
			results[i] = queryProcessor.Search(corrections[i]);

			for (int j = 0; j < 5; j++) {
				String doc_name = results[i].GetDocIDByIndex(j);
				if (doc_name != null)
					snippets[i][j] = snippetProcessor.GetSnippet(doc_name, corrections[i]);
				else
					snippets[i][j] = "ERROR";
			}
		}


		System.out.println("<!DOCTYPE html><html><head>	<title></title></head><body><table><pre>");
		for (int i = 0; i < queries.length; i++) {
			System.out.println("<b>Original query:</b> " + queries[i] + "\t" + "<b>Corrected Query:</b> " + corrections[i]);
			System.out.println("<b>Soundex code:</b> " + misspelled_soundex_codes[i]);
			System.out.print("<b>Suggested corrections:</b> ");
			String delimiter = "";
			for (int j = 0; j < suggestedWords.get(i).length; j++) {
				System.out.print(delimiter + suggestedWords.get(i)[j]);
				delimiter = ", ";
			}
			System.out.println();
			for (int j = 0; j < 5; j++) {
				System.out.println("\n" + snippets[i][j]);
			}
			System.out.println("==============================================================================================\n");
		}
		System.out.println("</pre></body><style>pre {    width: 60em;    white-space: pre-wrap;    font-size: 1.1em;}</style></html>");
	}

	
//	test stuff
	
	public static void TestSoundex() {
		System.out.println(Util.Soundex("extenssions"));	// E235
		System.out.println(Util.Soundex("extensions"));		// E235
		System.out.println(Util.Soundex("marshmellow"));	// M625
		System.out.println(Util.Soundex("marshmallow"));	// M625
		System.out.println(Util.Soundex("brimingham"));		// B655
		System.out.println(Util.Soundex("birmingham"));		// B655
		System.out.println(Util.Soundex("poiner"));			// P560
		System.out.println(Util.Soundex("pointer"));		// P536
	}
	
	public static void TestDictionary(Dictionary d) {
		System.out.println(d.ContainsWord("accommodationism"));
		System.out.println(d.ContainsWord("wheezinesses"));
		System.out.println(d.ContainsWord("cryptococcal"));
		System.out.println(d.ContainsWord("negativism"));
		System.out.println(d.ContainsWord("ye"));

		System.out.println(d.GetWords("A253"));
		System.out.println(d.GetWords("W252"));
		System.out.println(d.GetWords("C613"));
		System.out.println(d.GetWords("N231"));
		System.out.println(d.GetWords("Y000"));
	}

	public static void TestEditDistance() {
		System.out.println(Util.EditDistance("kitten", "sitting"));
		System.out.println(Util.EditDistance("saturday", "sunday"));
		System.out.println(Util.EditDistance("prision", "prison"));
		System.out.println(Util.EditDistance("entretainment", "entertainment"));
		System.out.println(Util.EditDistance("axtor", "actor"));
		System.out.println(Util.EditDistance("screning", "screening"));
	}

	public static void TestSpellChecker(QueryAnalyzer q) {
		for (String query : queries) {
			System.out.print("Did you mean: ");
			System.out.print(q.Correct(query));
			System.out.print("? (Originally ");
			System.out.print(query);
			System.out.println(")");
		}
	}
}
