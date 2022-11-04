package search;

import cluster.management.ServiceRegistry;
import com.google.protobuf.InvalidProtocolBufferException;
import model.DocumentData;
import model.Result;
import model.SerializationUtils;
import model.Task;
import model.proto.SearchModel;
import networking.OnRequestCallback;
import networking.WebClient;
import org.apache.zookeeper.KeeperException;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class SearchCoordinator implements OnRequestCallback {

    private static final String ENDPOINT = "/search";
    private static final String BOOKS_DIRECTORY = "./resources/books/";
    private final ServiceRegistry workersServiceRegistry;
    private final WebClient client;
    private final List<String> documents;

    public SearchCoordinator(ServiceRegistry workersServiceRegistry, WebClient client) {
        this.workersServiceRegistry = workersServiceRegistry;
        this.client = client;
        this.documents = readDocumentList();
    }

    private static List<String> readDocumentList() {
        File documentDirectory = new File(BOOKS_DIRECTORY);
        return Arrays.stream(Objects.requireNonNull(documentDirectory.list()))
                .map(documentName -> BOOKS_DIRECTORY + "/" + documentName)
                .collect(Collectors.toList());
    }

    @Override
    public byte[] handleRequest(byte[] requestPayload) {
        SearchModel.Request request = null;
        try {
            request = SearchModel.Request.parseFrom(requestPayload);
            SearchModel.Response response = createResponse(request);

            return response.toByteArray();
        } catch (InvalidProtocolBufferException | InterruptedException | KeeperException e) {
            e.printStackTrace();
            return SearchModel.Response.getDefaultInstance().toByteArray();
        }
    }

    private SearchModel.Response createResponse(SearchModel.Request request) throws InterruptedException, KeeperException {
        SearchModel.Response.Builder searchResponse = SearchModel.Response.newBuilder();

        System.out.println("Received search query : " + request.getSearchQuery());

        List<String> searchTerms = TFIDF.getWordsFromLine(request.getSearchQuery());

        List<String> workers = workersServiceRegistry.getAllServiceAddress();

        if (workers.isEmpty()) {
            System.out.println("No Search workers currently available");
            return searchResponse.build();
        }

        List<Task> tasks = createTasks(workers.size(), searchTerms);
        List<Result> results = sendTasksToWorkers(workers, tasks);

        List<SearchModel.Response.DocumentStats> sortedDocuments = aggregateResults(results, searchTerms);
        searchResponse.addAllRelevantDocuments(sortedDocuments);

        return searchResponse.build();
    }

    private List<SearchModel.Response.DocumentStats> aggregateResults(List<Result> results, List<String> searchTerms) {
        Map<String, DocumentData> allDocumentsResults = new HashMap<>();

        for (Result result : results) {
            allDocumentsResults.putAll(result.getDocumentToDocumentData());
        }

        System.out.println("Calculating score fore all the documents");

        Map<Double, List<String>> documentsSortedByScore = TFIDF.getDocumentsSortedByScore(searchTerms, allDocumentsResults);

        return sortDocumentsByScore(documentsSortedByScore);
    }

    private List<SearchModel.Response.DocumentStats> sortDocumentsByScore(Map<Double, List<String>> documentsSortedByScore) {
        List<SearchModel.Response.DocumentStats> sortedDocumentsStatsList = new ArrayList<>();

        for (Map.Entry<Double, List<String>> docScorePair : documentsSortedByScore.entrySet()) {
            Double score = docScorePair.getKey();

            for (String document : docScorePair.getValue()) {
                File documentPath = new File(document);

                SearchModel.Response.DocumentStats documentStats = SearchModel.Response.DocumentStats.newBuilder()
                        .setScore(score)
                        .setDocumentName(documentPath.getName())
                        .setDocumentSize(documentPath.length())
                        .build();

                sortedDocumentsStatsList.add(documentStats);
            }
        }


        return sortedDocumentsStatsList;
    }

    @Override
    public String getEndPoint() {
        return ENDPOINT;
    }

    private List<Result> sendTasksToWorkers(List<String> workers, List<Task> tasks) {
        CompletableFuture<Result>[] futures = new CompletableFuture[workers.size()];
        for (int i = 0; i < workers.size(); i++) {
            String worker = workers.get(i);
            Task task = tasks.get(i);
            byte[] payload = SerializationUtils.serialize(task);

            futures[i] = client.sendTask(worker, payload);
        }

        List<Result> results = new ArrayList<>();
        for (CompletableFuture<Result> future : futures) {
            try {
                Result result = future.get();
                results.add(result);
            } catch (InterruptedException | ExecutionException e) {
            }
        }
        System.out.println(String.format("Received %d/%d results", results.size(), tasks.size()));
        return results;
    }

    public List<Task> createTasks(int numberOfWorkers, List<String> searchTerms) {
        List<List<String>> workersDocuments = splitDocumentList(numberOfWorkers, documents);

        List<Task> tasks = new ArrayList<>();

        for (List<String> documentsForWorker : workersDocuments) {
            Task task = new Task(searchTerms, documentsForWorker);
            tasks.add(task);
        }

        return tasks;
    }

    private static List<List<String>> splitDocumentList(int numberOfWorkers, List<String> documents) {
        int numberOfDocumentsPerWorker = (documents.size() + numberOfWorkers - 1) / numberOfWorkers;

        List<List<String>> workersDocuments = new ArrayList<>();

        for (int i = 0; i < numberOfWorkers; i++) {
            int firstDocumentIndex = i * numberOfDocumentsPerWorker;
            int lastDocumentIndexExclusive = Math.min(firstDocumentIndex + numberOfDocumentsPerWorker, documents.size());

            if (firstDocumentIndex >= lastDocumentIndexExclusive) {
                break;
            }
            List<String> currentWorkerDocuments = new ArrayList<>(documents.subList(firstDocumentIndex, lastDocumentIndexExclusive));

            workersDocuments.add(currentWorkerDocuments);
        }
        return workersDocuments;
    }

    private static List<String> readDocumentsList() {
        File documentsDirectory = new File(BOOKS_DIRECTORY);
        return Arrays.asList(documentsDirectory.list())
                .stream()
                .map(documentName -> BOOKS_DIRECTORY + "/" + documentName)
                .collect(Collectors.toList());
    }

}
