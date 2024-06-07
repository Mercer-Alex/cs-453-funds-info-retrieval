import java.util.HashMap;
import java.util.TreeSet;

public class Dictionary {
	
	private HashMap<String, TreeSet<String>> codes;
	
	public Dictionary() {
		codes = new HashMap<String, TreeSet<String>>();
	}
	
	public void AddWord(String word) {	
		word = word.toLowerCase();

//		code -> list of words
		String code = Util.Soundex(word);
		TreeSet<String> words_by_code = codes.get(code);
		if (words_by_code == null) 
			words_by_code = new TreeSet<String>();
		words_by_code.add(word);
		codes.put(code, words_by_code);
	}
	
	public boolean ContainsWord(String word) {
		String code = Util.Soundex(word);
		if (codes.get(code) != null)
			return codes.get(code).contains(word);
		else
			return false;
	}
	
	public boolean ContainsCode(String code) {
		return (codes.get(code) != null);
	}
	
	public TreeSet<String> GetWords(String code) {
		return codes.get(code);
	}
}
