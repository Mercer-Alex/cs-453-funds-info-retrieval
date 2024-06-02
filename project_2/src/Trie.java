import java.util.HashSet;
import java.util.Random;


public class Trie {

    //							  parent, char rep, isLast
    public Node root = new Node(null, (char)0, false);
    private static Random r = new Random();

    public int maxFreq = 0;
    public int maxMod = 0;

    // q should consist ONLY of lower case letters and spaces. nothing else.
    public void AddQuery(String q, boolean mod) {
        boolean isMultipleWords = false;
        boolean isLast;
        char c;
        Node current = root;

        for (int i = 0; i < q.length(); i++) {
            // true if this is the last letter to process
            isLast = (i == q.length() - 1);

            // get current character, check to see if there's a space, then convert it to an index
            c = q.charAt(i);
            if (c == ' ') isMultipleWords = true;
            c = ConvertToIndex(c);

            // make/get the node
            if (current.children[c] == null)
                current.children[c] = new Node(current, q.charAt(i), isLast);

            // if it was the last node to add, do bookkeeping
            if (isLast) {
                current.children[c].freq++;
                if (isMultipleWords && current.children[c].freq > maxFreq)
                    maxFreq = current.children[c].freq;
                if (mod) {
                    current.children[c].mod++;
                    if (isMultipleWords && current.children[c].mod > maxMod)
                        maxMod = current.children[c].mod;
                }
            }

            current = current.children[c];
        }
    }

    public Node GetNode(String s) {
        try {
            Node current = root;
            char c;
            for (int i = 0; i < s.length(); i++) {
                c = s.charAt(i);
                c = ConvertToIndex(c);
                current = current.children[c];
            }
            return current;
        }
        catch (Exception e) {
            return null;
        }
    }

    // find a random word in the trie
    public String GetRandom() {
        Node current = root;
        int randomInt;
        while (!current.complete) {
            randomInt = r.nextInt(26);
            while (current.children[randomInt] == null)
                randomInt = (randomInt + 1) % 27;
            current = current.children[randomInt];
        }
        return GetWord(current);
    }

    // pass in a node, traverse up the tree, return the whole string
    private String GetWord(Node n) {
        StringBuilder ret = new StringBuilder();
        while (n.parent != null) {
            ret.insert(0, n.c);
            n = n.parent;
        }
        return ret.toString();
    }

    private char ConvertToIndex(char c) {
        if (c == ' ') return c -= 6;			// put space at the last spot in the array
        else return c -= 97;					// put everything else in order in the array
    }

    public class Node {
        public Node[] children;
        public Node parent;
        public char c;
        public boolean complete;
        public int freq = 0;
        public int mod = 0;

        public Node(Node parent, char c, boolean iscomplete) {
            children = new Node[27];	// hold all letters + space
            this.parent = parent;
            this.c = c;
            complete = iscomplete;
        }

        // I needed to make two functions because the first query is always going to be marked as "complete"
        public HashSet<String> GetExpansions() {
            HashSet<String> ret = new HashSet<String>();
            for (Node n : children)
                if (n != null)
                    ret.addAll(this.GetValidExpansions());

            return ret;
        }

        private HashSet<String> GetValidExpansions() {
            HashSet<String> ret = new HashSet<String>();
            if (this.complete)
                ret.add(GetWord(this));
            else
                for (Node n : children)
                    if (n != null)
                        ret.addAll(n.GetValidExpansions());

            return ret;
        }
    }
}
