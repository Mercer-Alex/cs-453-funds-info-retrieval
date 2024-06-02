import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;


public class Main {

    public static void main(String[] args) {
        QuerySuggester qs = new QuerySuggester();
        System.out.println("Parsing files...");
        qs.ParseFiles();
        System.out.println("Done parsing files.");

        while (true) {
            String in = ValidInput();
            HashMap<String, Double> outputs =qs.Suggest(in);
            for (int i = 0; i < 8; i++) {
                System.out.println(LargestKey(outputs));
            }
            System.out.println();
        }
    }

    private static String ValidInput() {
        Scanner s = new Scanner(System.in);
        boolean valid = false;
        String input = "";
        while (!valid) {
            System.out.println("Please enter a query:");
            input = s.nextLine();
            if (input.equals("") || input.equals(" ")) {
                System.out.println("You entered a stopword. Try again.");
            }
            else if (input.matches("[A-Za-z ]+")) {
                valid = true;
            }
            else
                System.out.println("Your query must match " + "[A-Za-z ]+");
        }
        return input.toLowerCase();
    }

    private static String LargestKey(HashMap<String, Double> m) {
        Iterator<Entry<String, Double>> it = m.entrySet().iterator();
        double largestVal = 0;
        String bestSugg = "";
        while (it.hasNext()) {
            Map.Entry<String, Double> pair = it.next();
            Double temp = pair.getValue();
            if (temp >= largestVal) {
                largestVal = temp;
                bestSugg = pair.getKey();
            }
        }
        if (bestSugg.equals("")) {
            return "---";
        }
        else {
            m.remove(bestSugg);
            return bestSugg + " (" + largestVal + ")";
        }
    }

}
