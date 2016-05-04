package nl.tno.idsa.framework.population;

import nl.tno.idsa.framework.utils.RandomNumber;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


// TODO Document class, including how input must be formatted et cetera. Is it in a logical package now?

public class NameGenerator {

    private static Map<String, NameGenerator> instances;

    public static NameGenerator getInstance(String language_id) throws IOException {
        if (instances == null) {
            instances = new HashMap<>();
        }
        NameGenerator instance = instances.get(language_id);
        if (instance == null) {
            instance = new NameGenerator(language_id);
            instances.put(language_id, instance);
        }
        return instance;
    }

    private HashMap<Integer, List<NameData>> maleFirstNames = new HashMap<>();       // Year -> [ Freq, Name ]
    private HashMap<Integer, List<NameData>> femaleFirstNames = new HashMap<>();
    private HashMap<Integer, List<NameData>> lastNames = new HashMap<>();

    private NameGenerator(String language_id) throws IOException {

        // TODO move to config
        String firstNames = readFile("../../data/nl/names/firstnames_" + language_id + ".csv");
        String[] firstNameLines = firstNames.split("\n");
        for (String line : firstNameLines) {
            try {
                String[] data = line.split(";");
                String mname = data[1];
                String mfreq = data[2];
                int mfreqi = Integer.parseInt(mfreq);
                String myear = data[3];
                int myeari = Integer.parseInt(myear);
                if (maleFirstNames.get(myeari) == null) {
                    maleFirstNames.put(myeari, new ArrayList<NameData>());
                }
                maleFirstNames.get(myeari).add(new NameData(mfreqi, mname));

                String fname = data[5];
                String ffreq = data[6];
                int ffreqi = Integer.parseInt(ffreq);
                String fyear = data[7];
                int fyeari = Integer.parseInt(fyear);
                if (femaleFirstNames.get(fyeari) == null) {
                    femaleFirstNames.put(fyeari, new ArrayList<NameData>());
                }
                femaleFirstNames.get(fyeari).add(new NameData(ffreqi, fname));
            } catch (Exception e) {
            }
        }

        String lastNames = readFile("../../data/nl/names/lastnames_" + language_id + ".csv");
        String[] lastNameLines = lastNames.split("\n");
        for (String line : lastNameLines) {
            try {
                String[] data = line.split(";");
                String name = data[1];
                String freq = data[2];
                int freqi = Integer.parseInt(freq);
                String year = data[3];
                int yeari = Integer.parseInt(year);
                if (this.lastNames.get(yeari) == null) {
                    this.lastNames.put(yeari, new ArrayList<NameData>());
                }
                this.lastNames.get(yeari).add(new NameData(freqi, name));
            } catch (Exception e) {
            }
        }
    }

    /**
     * Generate a first and a last name.
     */
    public String[] generateName(int year, Gender gender, double age) {
        HashMap<Integer, List<NameData>> firstNameData = gender == Gender.MALE ? maleFirstNames : femaleFirstNames;
        String firstName = sampleName(year, age, firstNameData);
        String lastName = sampleName(year, age, lastNames);
        return new String[]{firstName, lastName};
    }

    private static <X> X findClosestMatch(int key, HashMap<Integer, X> map) {
        int closestKeyDistance = Integer.MAX_VALUE;
        X closestMatch = null;
        for (int mapKey : map.keySet()) {
            int keyDistance = Math.abs(mapKey - key);
            if (closestMatch == null || keyDistance < closestKeyDistance) {
                closestKeyDistance = keyDistance;
                closestMatch = map.get(mapKey);
            }
        }
        return closestMatch;
    }

    private String readFile(String fileName) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF8")); // TODO Wrong encoding. Why can't Java GUESS the encoding out of the 50 options they provide.
        String lines = "";
        String line;
        while ((line = br.readLine()) != null) {
            lines = lines + "\n" + line;
        }
        br.close();
        return lines;
    }

    private String sampleName(int year, double age, HashMap<Integer, List<NameData>> firstNameData) {
        List<NameData> names = findClosestMatch((int) (year - age), firstNameData);
        ArrayList<Integer> frequencies = new ArrayList<>(names.size());
        for (NameData nameData : names) {
            frequencies.add(nameData.getFrequency());
        }
        int index = RandomNumber.drawFrom(frequencies);
        return names.get(index).getName();
    }


    private class NameData {
        private int frequency;
        private String name;

        private NameData(int frequency, String name) {
            this.frequency = frequency;
            this.name = name;
        }

        public int getFrequency() {
            return frequency;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return "NameData{" +
                    "frequency=" + frequency +
                    ", name='" + name + '\'' +
                    '}';
        }
    }
}
