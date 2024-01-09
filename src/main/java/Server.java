import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.ArrayList;

public class Server {
    private static final int PORT = 12345;
    private static final String BASE_DIRECTORY = "/Users/veronika_snigur/Downloads/cw/course_work_parallel_computing";
    private static final ExecutorService mainExecutorService = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                // Ask the user for the number of threads
                System.out.print("Enter the number of threads for indexing: ");
                int numThreads = Integer.parseInt(new Scanner(System.in).nextLine());

                // Submit client handling with the given number of threads
                mainExecutorService.submit(() -> handleClient(clientSocket, numThreads));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mainExecutorService.shutdown();
        }
    }

    private static void handleClient(Socket clientSocket, int numThreads) {
        // Create a fixed thread pool with the specified number of threads
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);

        try (ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream());
             ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream())) {

            List<String> filesToIndex = Arrays.asList(
                    "aclImdb/test/neg", "aclImdb/test/pos",
                    "aclImdb/train/neg", "aclImdb/train/pos", "aclImdb/train/unsup"
            );
            List<String> absoluteFilePaths = filesToIndex.stream()
                    .map(dir -> BASE_DIRECTORY + File.separator + dir)
                    .flatMap(dir -> Arrays.stream(new File(dir).listFiles()))
                    .map(File::getAbsolutePath)
                    .collect(Collectors.toList());

            InvertedIndex invertedIndex = new InvertedIndex();

            if (invertedIndex.isIndexed()) {
                System.out.println("Files are already indexed.");
            } else {
                System.out.println("Indexing files...");
                long startTime = System.currentTimeMillis();

                // Divide the list of files among available threads
                int filesPerThread = absoluteFilePaths.size() / numThreads;
                List<Callable<Void>> indexingTasks = new ArrayList<>();

                for (int i = 0; i < numThreads; i++) {
                    int startIndex = i * filesPerThread;
                    int endIndex = (i == numThreads - 1) ? absoluteFilePaths.size() : (i + 1) * filesPerThread;

                    List<String> subList = absoluteFilePaths.subList(startIndex, endIndex);

                    // Add an indexing task to the list
                    indexingTasks.add(() -> {
                        System.out.println("Indexing files in thread " + Thread.currentThread().getId() +
                                " from index " + startIndex + " to " + (endIndex - 1));
                        invertedIndex.buildIndex(subList);
                        System.out.println("Time for execution with thread "+ Thread.currentThread().getId() + ":\t" + (System.currentTimeMillis() - startTime)+" milliseconds");
                        return null;
                    });
                }

                try {
                    // Invoke all tasks and wait for their completion
                    executorService.invokeAll(indexingTasks);
                    System.out.println("Files indexed successfully.");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            while (true) {
                // Read the search query from the client
                String query = (String) input.readObject();

                // Check if the client wants to continue searching
                if ("no".equalsIgnoreCase(query.trim())) {
                    System.out.println("Client opted out of further search. Closing connection.");
                    break;  // Exit the loop and close the connection
                }

                // Get the search results using the provided search query
                List<IndexEntry> searchResults = invertedIndex.getSearchResults(query);

                if (!searchResults.isEmpty()) {
                    System.out.println("Files for query '" + query + "' found successfully. Results are sent to server:");

                    // Print search results
                    for (IndexEntry entry : searchResults) {
                        //потрібно було для перевірки правильності індексації, щоб відслідкувати знайдені співпадіння
                        // System.out.println(entry.getFilePath());
                    }
                } else {
                    System.out.println("No files found for query '" + query + "'.");
                }

                // Wrap the results in a SearchResultWrapper
                SearchResultWrapper resultWrapper = new SearchResultWrapper(searchResults);

                output.writeObject(resultWrapper);
                output.flush(); // Flush the output stream
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Shutdown the executor service after client handling
            executorService.shutdown();
        }
    }
}
