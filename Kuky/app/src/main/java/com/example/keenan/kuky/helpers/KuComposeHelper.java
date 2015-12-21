package com.example.keenan.kuky.helpers;

import android.content.Context;
import android.util.Log;

import com.example.keenan.kuky.R;
import com.opencsv.CSVReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KuComposeHelper {
    //Syllables --> Used to store dictionary of words and syllable counts
    private Map<String, Integer> mSyllables = new HashMap<String, Integer>();
    private Context context;

    /**
     * Used to validate Kus
     * @param context
     * @throws IOException
     */
    public KuComposeHelper(Context context) throws IOException {
        this.context = context;

        // Load syllable dictionary to save time on loading it again.
        loadSyllableDict();
    }

    /**
     * Loads the syllable dictionary from syllables.txt raw file
     * @throws IOException
     */
    private void loadSyllableDict() throws IOException {
        InputStream raw = context.getResources().openRawResource(R.raw.syllables);
        InputStreamReader streamReader = new InputStreamReader(raw);
        BufferedReader bufferedReader = new BufferedReader(streamReader);

        CSVReader reader = new CSVReader(bufferedReader);
        String[] nextLine;
        while((nextLine = reader.readNext()) != null) {
            // THE APP CRASHED HERE, SO I COMMENTED THIS LINE
            //mSyllables.put(nextLine[0], Integer.parseInt(nextLine[1]));
        }
        reader.close();
    }

    /**
     * Counts the number of syllables in a word algorithmically
     * @param word Input words
     * @return number of syllables
     */
    public int countSyllables(String word) {
        /*
        This function is really awesome! However, I think that you
        should comment this further than just a docstring given that this is somewhat complicated.
         */
        Pattern pattern1 = Pattern.compile("[eaoui][eaoui]");
        Pattern pattern2 = Pattern.compile("[eaoui][^eaoui]");
        Pattern pattern3 = Pattern.compile("[eaoui][eaoui][eaoui]");
        Pattern pattern4 = Pattern.compile("[eaoui]");

        String[] exception_add = {"serious", "critical"};
        String[] exception_del = {"fortunately", "unfortunately"};
        String[] co_one = {"cool","coach","coat","coal","count","coin","coarse","coup","coif","cook","coign","coiffe","coof","court"};
        String[] co_two = {"coapt","coed","coinci"};

        String[] pre_one = {"preach"};

        int syls = 0;
        int disc = 0;

        if (word.length() <= 3) {
            syls = 1;
            return syls;
        }

        if (word.endsWith("es") || word.endsWith("ed")) {
            int doubleAndtriple_1 = 0;
            Matcher matcher1 = pattern1.matcher(word);
            while (matcher1.find()) {
                doubleAndtriple_1++;
            }

            int theothercount = 0;
            Matcher matcher2 = pattern2.matcher(word);
            while (matcher2.find()){
                theothercount++;
            }
            if (doubleAndtriple_1 > 1 || theothercount > 1) {
                if (!(word.endsWith("ted") || word.endsWith("tes") || word.endsWith("ses") || word.endsWith("ied") || word.endsWith("ies"))) {
                    disc++;
                }
            }
        }

        String[] le_except = {"whole","mobile","pole","male","female","hale","pale","tale","sale","aisle","whale","while"};

        if (word.endsWith("e")) {
            if (word.endsWith("le") && !(Arrays.asList(le_except).contains(word))){
                // Doing nothing yo
            } else {
                disc += 1;
            }
        }

        int doubleAndtriple = 0;
        Matcher matcher3 = pattern1.matcher(word);
        while (matcher3.find()) {
            doubleAndtriple ++;
        }

        int triple = 0;
        Matcher matcher4 = pattern3.matcher(word);
        while (matcher4.find()) {
            triple++;
        }

        disc += doubleAndtriple+triple;

        int numVowels = 0;
        Matcher matcher5 = pattern4.matcher(word);
        while (matcher5.find()) {
            numVowels++;
        }

        if (word.substring(0, 2) == "mc"){
            syls++;
        }

        String vowels = "aeoui";
        if (word.endsWith("y") && !vowels.contains(String.valueOf(word.charAt(word.length()-2)))) {
            syls++;
        }

        for (int i = 0; i < word.length(); i++) {
            char j = word.charAt(i);
            if (j == 'y') {
                if (i != 0 && i != word.length() - 1) {
                    if (!vowels.contains(String.valueOf(word.charAt(i-1))) && !vowels.contains(String.valueOf(word.charAt(i+1)))) {
                        syls ++;
                    }
                }
            }
        }

        if (word.substring(0,3) == "tri" && vowels.contains(String.valueOf(word.charAt(3)))) {
            syls ++;
        }

        if (word.substring(0,2) == "bi" && vowels.contains(String.valueOf(word.charAt(2)))) {
            syls ++;
        }

        if (word.endsWith("ian")) {
            if (!(word.endsWith("cian") || word.endsWith("tian"))) {
                syls++;
            }
        }

        if (word.startsWith("co") && vowels.contains(String.valueOf(word.charAt(2)))) {
            if (Arrays.asList(co_two).contains(word.substring(0, 4)) || Arrays.asList(co_two).contains(word.substring(0, 5)) || Arrays.asList(co_two).contains(word.substring(0, 6))){
                syls++;
            }
            if (!(Arrays.asList(co_one).contains(word.substring(0, 4)) || Arrays.asList(co_one).contains(word.substring(0, 5)) || Arrays.asList(co_one).contains(word.substring(0, 6)))) {
                syls++;
            }
        }

        if (word.startsWith("pre") && vowels.contains(String.valueOf(word.charAt(3)))) {
            if (!(Arrays.asList(pre_one).contains(word.substring(0,6)))) {
                syls++;
            }
        }

        String[] negatives = {"doesn't", "isn't", "shouldn't", "couldn't","wouldn't"};

        if (word.endsWith("n't")) {
            if (Arrays.asList(negatives).contains(word)) {
                syls++;
            }
        }

        if (Arrays.asList(exception_del).contains(word)) {
            disc++;
        }

        if (Arrays.asList(exception_add).contains(word)) {
            syls++;
        }

        return numVowels - disc + syls;
    }

    /**
     * Gets the number of syllables for a given line. Starts by checking the dictionary
     * If the word doesn't exist in the dictionary, check it algorithmically
     * @param line
     * @return number of syllables in the line
     */
    public int getSyllables(String line) {
        int count = 0;
        for (String word : cleanLine(line)) {
            if (word.length() <= 2) {
                count +=  1;
            } else if (mSyllables.get(word) != null) {
                count += mSyllables.get(word);
            } else {
                count += countSyllables(word);
            }
        }
        return count;
    }

    /**
     * Checks the total number of Kus in a given line
     * @param line1 line one of the ku
     * @param line2 line two of the ku
     * @param line3 line three of the ku
     * @return an array of the number of syllables in each line
     */
    public int[] checkKu(String line1, String line2, String line3) {
        return new int[] {getSyllables(line1), getSyllables(line2), getSyllables(line3)};
    }

    /**
     * Cleans the line by stripping punctuation, making lowercase, removing whitespace, etc.
     * @param line
     * @return
     */
    public String[] cleanLine(String line){
        line = line.toLowerCase().replaceAll("[^a-z-' ]","");
        return line.split("\\s+");
    }
}
