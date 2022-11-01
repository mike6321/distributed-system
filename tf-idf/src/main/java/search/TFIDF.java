package search;

import model.DocumentData;

import java.util.*;

public class TFIDF {

    // frequency 계산
    public static double calculateTermFrequency(List<String> words, String term) {
        long count = 0;
        for (String word : words) {
            if (term.equalsIgnoreCase(word)) {
                count++;
            }
        }

        double termFrequency = (double) count / words.size();
        return termFrequency;
    }

    // DocumentData 생성
    public static DocumentData createDocumentData(List<String> words, List<String> terms) {
        DocumentData documentData = new DocumentData();

        for (String term : terms) {
            double termFrequency = calculateTermFrequency(words, term);
            documentData.putTermFrequency(term, termFrequency);
        }

        return documentData;
    }

    private static double getInverseDocumentFrequency(String term, Map<String, DocumentData> documentResults) {
        int documentSize = documentResults.size(); // 문서의 개수
        double nt = 0; // 용어가 등장하는 문서의 개수

        for (String document : documentResults.keySet()) {
            DocumentData documentData = documentResults.get(document);
            double termFrequency = documentData.getFrequency(term);
            if (termFrequency > 0.0) {
                nt++;
            }
        }

        return nt == 0 ? 0 : Math.log10(documentSize / nt);
    }

    private static Map<String, Double> getTermToInverseDocumentFrequencyMap(List<String> terms,
                                                                            Map<String, DocumentData> documentResults) {
        Map<String, Double> termToIDF = new HashMap<>();

        for (String term : terms) {
            double idf = getInverseDocumentFrequency(term, documentResults);
            termToIDF.put(term, idf);
        }

        return termToIDF;
    }

    private static double calculateDocumentScore(List<String> terms,
                                                 DocumentData documentData,
                                                 Map<String, Double> termToInverseDocumentFrequency) {
        double score = 0;

        for (String term : terms) {
            double termFrequency = documentData.getFrequency(term);
            Double inverseTermFrequency = termToInverseDocumentFrequency.get(term);
            score += termFrequency * inverseTermFrequency;
        }

        return score;
    }

    public static Map<Double, List<String>> getDocumentsSortedByScore(List<String> terms,
                                                                      Map<String, DocumentData> documentResults) {
        TreeMap<Double, List<String>> scoreToDocuments = new TreeMap<>();

        Map<String, Double> termToInverseDocumentFrequency = getTermToInverseDocumentFrequencyMap(terms, documentResults);
        for (String document : documentResults.keySet()) {
            DocumentData documentData = documentResults.get(document);

            double score = calculateDocumentScore(terms, documentData, termToInverseDocumentFrequency);

            addDocumentScoreToTreeMap(scoreToDocuments, score, document);
        }

        return scoreToDocuments.descendingMap();
    }

    private static void addDocumentScoreToTreeMap(TreeMap<Double, List<String>> scoreToDocuments,
                                                  double score,
                                                  String document) {
        List<String> documentsWithCurrentScore = scoreToDocuments.get(score);
        if (documentsWithCurrentScore == null) {
            documentsWithCurrentScore = new ArrayList<>();
        }
        documentsWithCurrentScore.add(document);
        scoreToDocuments.put(score, documentsWithCurrentScore);
    }

    public static List<String> getWordsFromLine(String line) {
        return Arrays.asList(line.split("(\\.)+|(,)+|( )+|(-)+|(\\?)+|(!)+|(;)+|(:)+|(/d)+|(/n)+"));
    }

    public static List<String> getWordsFromLines(List<String> lines) {
        List<String> words = new ArrayList<>();

        for (String line : lines) {
            words.addAll(getWordsFromLine(line));
        }

        return words;
    }

}
