package search;

import model.DocumentData;
import model.Result;
import model.SerializationUtils;
import model.Task;
import networking.OnRequestCallback;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SearchWorker implements OnRequestCallback {

    private static final String ENDPOINT = "/task";

    @Override
    public byte[] handleRequest(byte[] requestPayload) {
        Task task = (Task) SerializationUtils.deserialize(requestPayload);
        Result result = createResult(task);
        return SerializationUtils.serialize(result);
    }

    private Result createResult(Task task) {
        List<String> documents = task.getDocuments();
        System.out.println(String.format("Received %d documents to process", documents.size()));

        Result result = new Result();

        for (String document : documents) {
            List<String> words = parseWordsFromDocument(document);
            DocumentData documentData = TFIDF.createDocumentData(words, task.getSearchTerms());
            result.addDocumentData(document, documentData);
        }

        return result;
    }

    @Override
    public String getEndPoint() {
        return ENDPOINT;
    }

    private List<String> parseWordsFromDocument(String document) {
        try {
            FileReader fileReader = new FileReader(document);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            List<String> lines = bufferedReader.lines().collect(Collectors.toList());
            List<String> words = TFIDF.getWordsFromLines(lines);
            return words;
        } catch (FileNotFoundException e) {
            return Collections.emptyList();
        }
    }

}
