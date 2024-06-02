import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

public class QuerySuggester {
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    private static final StopWords stopWords = new StopWords();
    private final Trie trie = new Trie();

    public HashMap<String, Double> Suggest(String in) {
        System.out.println("Calculating suggestions...");
        HashMap<String, Double> ret = new HashMap<>();

        in = FormatQuery(in);
        if (in.equals("") || in.equals(" ")) {
            System.out.println("You entered only a stopword. Input a better query.");
            return ret;
        }

        Trie.Node n = GetSubtree(in + ' ');
        if (n == null) {
            return ret;
        }
        HashSet<String> expansions = n.GetExpansions();

        int maxFreqofSugg = 1;
        int maxModofSugg = 1;
        for (String sq : expansions) {
            Trie.Node node = trie.GetNode(sq);

            int freq = node.freq;
            if (freq > maxFreqofSugg) maxFreqofSugg = freq;

            int mod = node.mod;
            if (mod > maxModofSugg) maxModofSugg = mod;
        }

        for (String sq : expansions) {
            Trie.Node node = trie.GetNode(sq);

            double freq = node.freq / (double)maxFreqofSugg;
            double mod =(float)node.mod / maxModofSugg;
            double rank = (freq + mod) / (1 - Math.min(freq, mod));

            ret.put(sq, rank);
        }
        return ret;
    }

    private Trie.Node GetSubtree(String s) {
        return trie.GetNode(s);
    }

    public void ParseFiles() {
        String nextLine;
        String[] q1;
        String[] q2;

        String[] docs = {
                "Clean-Data-01.txt",
                "Clean-Data-02.txt",
                "Clean-Data-03.txt",
                "Clean-Data-04.txt",
                "Clean-Data-05.txt",
        };
        for (String doc : docs) {
			System.out.printf("parsing file %s \n", doc);

            try {
                BufferedReader bufferedReader = new BufferedReader(new FileReader("Resources/AOL-Clean-Data/" + doc));

                nextLine = bufferedReader.readLine();
               System.out.println("header: " + nextLine);

                // initialize necessary variables
                nextLine = bufferedReader.readLine();
                q1 = FormatLine(nextLine);
                boolean mod;
                trie.AddQuery(q1[1], false);

                while ((nextLine = bufferedReader.readLine()) != null) {
                    q2 = FormatLine(nextLine);
                    mod = IsRelated(q1, q2);

                    trie.AddQuery(q2[1], mod);
                    q1 = q2;
                }
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            System.gc();
        }
    }

    private boolean IsRelated(String[] q1, String[] q2) {
        // different first word
        String w1 = q1[1].split(" ")[0];
        String w2 = q2[1].split(" ")[0];
        if (!w1.equals(w2)) return false;

        // different user id
        if (!q1[0].equals(q2[0])) return false;

        // within 10 minutes
        DateTime t1 = DateTime.parse(q1[2], dateTimeFormatter);
        DateTime t2 = DateTime.parse(q2[2], dateTimeFormatter);
        return Math.abs(t2.getMillis() - t1.getMillis()) <= 600000;
    }

    private String[] FormatLine(String line) {
        // split userID (0), query (1), and date (2)
        String[] q = line.split("\t");
        q[1] = FormatQuery(q[1]);
        return q;
    }

    private String FormatQuery(String query) {
        // format query: ignore punctuation, check if first word is stopword
        query = query.replaceAll("[^a-zA-Z ]", "").toLowerCase();
        String[] q = query.split(" ");
        if (stopWords.contains(q[0])) {
            StringBuilder s = new StringBuilder();
            for (int i = 1; i < q.length; i++) {
                s.append(q[i]);
                if (i+1 < q.length) s.append(" ");
            }
            return s.toString();
        }
        else
            return query;
    }

    // DEBUGGING AND TESTING

    public void PrintSomeTrie() {
        System.out.printf("\nTRIE STATS\nMax Freq: %d\nMax Mod: %d\n", trie.maxFreq, trie.maxMod);
        System.out.println("\nHere are ten queries I saw:");
        for (int i = 0; i < 10; i++) {
            System.out.println(trie.GetRandom());
        }
    }
}
