package wikibot;

import java.util.*;
public class RelevanceScore {
    private Map<String, Integer> wordCount;
    private final Set<String> stopWords;

    public RelevanceScore() {
        wordCount = new HashMap<>();
        stopWords = new HashSet<>(Arrays.asList("the","in","at","on","and","or","to","of","as","for","are","with","from","by","an","that","all","wikipedia","archived","original"));
    }

    public void calculateRelevance(String document) {
        String[] words = document.toLowerCase().split("\\W+");
        for (String word : words) {
            if (!stopWords.contains(word) && word.length() > 1) {
                wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
            }
        }
    }

    public List<String> getTopRelevantWords(int n) {
        PriorityQueue<Map.Entry<String, Integer>> maxHeap = new PriorityQueue<>((a, b) -> b.getValue() - a.getValue());
        maxHeap.addAll(wordCount.entrySet());

        List<String> topWords = new ArrayList<>();
        while (n-- > 0 && !maxHeap.isEmpty()) {
            topWords.add(maxHeap.poll().getKey());
        }

        return topWords;
    }

}
