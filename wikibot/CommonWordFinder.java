package wikibot;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class CommonWordFinder {
    final int threadNum;
    int pageNum;
    int pagesChecked;
    int totalWords;

    public CommonWordFinder(int pageNum, int threadNum) {
        this.threadNum = threadNum;
        this.pageNum = (pageNum/threadNum)*threadNum; // Set to even multiple of threadNum
        pagesChecked = 0;
        totalWords = 0;
    }
    public static void main(String[] args) throws IOException {
        CommonWordFinder c = new CommonWordFinder(100_000, 8);

        HashMap<String, Double> wordFreq = c.getWordFrequency(500);
        writeMapToFile(wordFreq, c.totalWords, "default_word_freq.txt");
    }

    public static <T extends Comparable<? super T>> void writeMapToFile(HashMap<String, T> map, int totalWords, String filename) throws IOException {
        File file = new File(filename);
        if (!file.exists()) {
            file.createNewFile();
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(""+totalWords);

        HashMap<String, T> sorted = sortTopFromMap(map, map.size());

        for (Map.Entry<String, T> entry : sorted.entrySet()) {
            writer.write("\n" + entry.getKey() + " " + entry.getValue());
        }

        writer.close();
        System.out.println("Successfully wrote to " + filename);
    }

    public HashMap<String, Double> getWordFrequency(int maxWords) {
        HashMap<String, Double> wordFrequencies = new HashMap<>();
        for (Map.Entry<String, Integer> entry : getWordMap().entrySet()) {
            wordFrequencies.put(entry.getKey(), entry.getValue()/(double)totalWords);
        }
        return sortTopFromMap(wordFrequencies, maxWords);
    }

    public HashMap<String, Integer> getWordMap() {
        WordCounter[] threads = new WordCounter[threadNum];

        for (int i = 0; i < threadNum; i++) {
            threads[i] = new WordCounter(pageNum/threadNum, i);
        }
        for (int i = 0; i < threadNum; i++) {
            threads[i].start();
            System.out.println("Thread #" + (i+1) + " dispatched");
        }
        System.out.println("Thread dispatch complete");
        int lastPagesSearched = 0;
        int stepsSincePageSearched = 0;
        while (true) {
            if (allThreadsDone(threads)) {
                System.out.println("All threads finished");
                break;
            }
            if (lastPagesSearched != pagesChecked) {
                lastPagesSearched = pagesChecked;
                stepsSincePageSearched = 0;
            } else {
                if (stepsSincePageSearched++ > 10) {
                    System.out.println("No threads have received info in 10 seconds, ending search");
                    for (WordCounter t : threads) {
                        t.done = true;
                    }
                    continue;
                }
            }
            printProgress(100);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return combineThreadMaps(threads);
    }

    public static boolean allThreadsDone(WordCounter[] threads) {
        for (WordCounter t : threads) {
            if (!t.done) {
                return false;
            }
        }
        return true;
    }

    public void printProgress(int width) {
        int percentDone = (width*pagesChecked)/pageNum;
        StringBuilder str = new StringBuilder();
        str.append('|');
        for (int i = 0; i < width; i++) {
            str.append(i < percentDone ? '=' : ' ');
        }
        str.append('|');
        str.append(" Pages Searched: ").append(pagesChecked).append("/").append(pageNum);
        System.out.println(str);
    }

    public HashMap<String, Integer> combineThreadMaps(WordCounter[] threads) {
        HashMap<String, Integer> output = new HashMap<>();

        for (WordCounter thread : threads) {
            HashMap<String, Integer> threadWords = thread.wordCounts;
            for (String word : threadWords.keySet()) {
                output.put(word, output.getOrDefault(word, 0) + threadWords.get(word));
            }
        }
        return output;
    }

    public List<String> getCommonWords(int wordNum)  {
        return getTopWordsFromMap(getWordMap(), wordNum);
    }

    public List<String> getTopWordsFromMap(HashMap<String, Integer> map, int n) {
        return sortTopFromMap(map, n).keySet().stream().toList();
    }

    public static <K, V extends Comparable<? super V>> HashMap<K, V> sortTopFromMap(HashMap<K, V> map, int n) {
        return map.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(n)
                .collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }


    public class WordCounter extends Thread {
        HashMap<String, Integer> wordCounts = new HashMap<>();
        int pages;
        int threadID;
        boolean done;

        public WordCounter(int pages, int threadID) {
            this.pages = pages;
            this.threadID = threadID;
            done = false;
        }

        public void run() {
            final String randomUrl = "https://en.wikipedia.org/wiki/Special:Random";
            while (pages > 0 && !done) {
                try {
                    Document doc = Jsoup.connect(randomUrl).get();
                    addWordsToMap(wordCounts, doc.text());
                    pagesChecked++;
                    pages--;
                } catch (Exception e) {
                    System.out.println(e + " on thread #" + (threadID+1));
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    System.out.println(e + " on thread #" + (threadID+1));
                }
            }
            done = true;
        }

        public void addWordsToMap(HashMap<String, Integer> map, String doc) {
            String[] words = doc.toLowerCase().split("\\W+");
            for (String word : words) {
                if (word.length() > 1) {
                    map.put(word, map.getOrDefault(word, 0) + 1);
                    totalWords++;
                }
            }
        }
    }

}
