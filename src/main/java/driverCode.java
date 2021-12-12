import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import java.util.Scanner;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class driverCode {
    private static String[] stopWords;
    static int scannedDocumentCount = 0;
    private static HashDictionary<String,String> mymap;
    static long totalIndexTime = 0;
    static long totalSearchTime=0;
    static long minSearchTime=Long.MAX_VALUE;
    static long maxSearchTime=Long.MIN_VALUE;

    public static int listFilesForFolder(final File folder)  {


        for (final File fileEntry : folder.listFiles()) {
            //Check if path is directory go recursive else index the document
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
                scannedDocumentCount++;
                String absolutePathofTxt=fileEntry.getAbsolutePath();
                String[] splittedAbsolutePath = absolutePathofTxt.split(Pattern.quote("\\"));
                String Value =splittedAbsolutePath[splittedAbsolutePath.length-2]+"-"+fileEntry.getName();
                //System.out.println(Value);

                String originalText =textFromPath(absolutePathofTxt);
                //System.out.println(originalText);
                ArrayList<String> splittedCleanedText= cleanSplitText(originalText);
                //System.out.println(splittedCleanedText);

                for (int i=0; i<splittedCleanedText.size();i++) {
                    long startTime = System.currentTimeMillis();
                    //System.out.println("adding"+splittedCleanedText.get(i));
                    mymap.put(splittedCleanedText.get(i),Value);
                    long endTime = System.currentTimeMillis();
                    totalIndexTime+=(endTime - startTime);

                }

            }
        }
        return scannedDocumentCount;
    }

    private static String[] readStopWords(String filePath) {
        //Read stopwords as array from given path for excluding from text
        ArrayList<String> stopWords = new ArrayList<>();
        try {
            File stopWordsFile = new File(filePath);
            Scanner myReader = new Scanner(stopWordsFile);

            while (myReader.hasNextLine()) {
                String word = myReader.nextLine();
                if(word!="")
                stopWords.add(word);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return stopWords.toArray(new String[0]);
    }

    private static String textFromPath(String filePath) {
        //Return full text of given path of document
        String fullText = "";

        try {
            File myObj = new File(filePath);
            Scanner myReader = new Scanner(myObj);

            while (myReader.hasNextLine()) {
                String x= myReader.nextLine();
                if(x.equals(""))
                    fullText +=" ";
                else
                    fullText += x;

            }
            myReader.close();



        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return fullText;
    }

    private static ArrayList<String> cleanSplitText(String fulltext)

    {       //Get text as argument and split and clean data return afterwards
        ArrayList<String> splittedCleaned=new ArrayList<>();

        String[] splitted = fulltext.split(Delimiter.DELIMITERS);


        for (int i = 0; i < splitted.length; i++) {
            boolean isValidWord= true;
            for (int j = 0; j < stopWords.length; j++){


                if(splitted[i] == "" || splitted[i].equalsIgnoreCase(stopWords[j])) {
                    isValidWord = false;

                }

            }
            if (isValidWord)
                splittedCleaned.add(splitted[i].toLowerCase());
        }

        return splittedCleaned;
    }

    public static String millisToShortDHMS(long duration) {
        //Returns Day hour minute second millisecond
        String res = "";    // java.util.concurrent.TimeUnit;
        long days       = TimeUnit.MILLISECONDS.toDays(duration);
        long hours      = TimeUnit.MILLISECONDS.toHours(duration) -
                TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(duration));
        long minutes    = TimeUnit.MILLISECONDS.toMinutes(duration) -
                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration));
        long seconds    = TimeUnit.MILLISECONDS.toSeconds(duration) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration));
        long millis     = TimeUnit.MILLISECONDS.toMillis(duration) -
                TimeUnit.SECONDS.toMillis(TimeUnit.MILLISECONDS.toSeconds(duration));

        if (days == 0)      res = String.format("%02d:%02d:%02d.%04d", hours, minutes, seconds, millis);
        else                res = String.format("%dd %02d:%02d:%02d.%04d", days, hours, minutes, seconds, millis);
        return res;
    }

    public static ArrayList<String> loadSearchWords(String filePath)
    {   //Load words to search afterwards
        ArrayList<String> listOfSearchWords=new ArrayList<>();

        try {
            File stopWordsFile = new File(filePath);
            Scanner myReader = new Scanner(stopWordsFile);

            while (myReader.hasNextLine()) {
                String word = myReader.nextLine();
                if(word!="")
                    listOfSearchWords.add(word);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return listOfSearchWords;
    }


        public static void main(String[] args) {
        Scanner inputScanner=new Scanner(System.in);


        //Get input from user for hashDictionary parameters
        double loadFactor=0;
        while(loadFactor<0.5 || loadFactor>1)
        {
            System.out.println("Select Load factor(0,5-1)");
            loadFactor = inputScanner.nextDouble();
        }

        HashDictionary.hashFunctionType hashingType =null;
        while(hashingType==null)
        {
            System.out.println("Select hashing type(SSF/PAF)");
            String hTypeString=inputScanner.nextLine();

            if (hTypeString.equals("SSF"))
                hashingType=HashDictionary.hashFunctionType.SSF;
            else if(hTypeString.equals("PAF"))
                hashingType=HashDictionary.hashFunctionType.PAF;

        }


            HashDictionary.collisionHandlingType collisionType =null;
            while(collisionType==null)
            {   System.out.println("Select collision handling type(LP/DH)");
                String hTypeString=inputScanner.nextLine();

                if (hTypeString.equals("LP"))
                    collisionType=HashDictionary.collisionHandlingType.LP;
                else if(hTypeString.equals("DH"))
                    collisionType=HashDictionary.collisionHandlingType.DH;
            }






        //Initialize main datamap with selected arguments
        mymap = new HashDictionary<>(101,loadFactor, hashingType, collisionType);
        //Get stop words from path
        String absolutePathStopWords ="C:\\Users\\orkun\\IdeaProjects\\DataStructures_HW\\src\\main\\resources\\stop_words_en.txt";
        stopWords = readStopWords(absolutePathStopWords);

        // Read documents from path and execute indexing
        String absolutePathMainFolder="C:\\Users\\orkun\\IdeaProjects\\DataStructures_HW\\src\\main\\resources\\toRead";
        final File folder = new File(absolutePathMainFolder);
        listFilesForFolder(folder);

        /* Print each stop word
           for (int i = 0; i < stopWords.length; i++)
           System.out.println(stopWords[i]);
        */


            //Path of search words text
            String pathOfSearchWords="C:\\Users\\orkun\\IdeaProjects\\DataStructures_HW\\src\\main\\resources\\search.txt";
            //Load search words
            ArrayList<String> searchWords=loadSearchWords(pathOfSearchWords);

            for (int i=0;i<searchWords.size();i++)
            {
                long startTime = System.currentTimeMillis();
                ArrayList<String> wordFrequency = mymap.get(searchWords.get(i));
                System.out.println(searchWords.get(i)+" found in "+wordFrequency.size()+" document "+" "+wordFrequency);
                long endTime = System.currentTimeMillis();
                long searchTime=(endTime - startTime);
                totalSearchTime+=searchTime;

                if (i==0 || minSearchTime>searchTime)
                {
                    minSearchTime=searchTime;

                }
                if (i==0 || maxSearchTime<searchTime)
                {
                    maxSearchTime=searchTime;
                }

            }
            //Report total indexing time and collision count
            System.out.println("Total indexing time= "+millisToShortDHMS(totalIndexTime));
            System.out.println("Total collision count= "+ mymap.getCollisionCount());
            //Report min max average and total search time for given search words
            System.out.println("Minimum search time= "+millisToShortDHMS(minSearchTime));
            System.out.println("Maximum search time= "+millisToShortDHMS(maxSearchTime));
            System.out.println("Total search time= "+millisToShortDHMS(totalSearchTime));

            System.out.println("Average search time= "+millisToShortDHMS(totalSearchTime/searchWords.size()));
            //Total count of different words in table
            System.out.println("Count"+mymap.getSize());

    }


}
