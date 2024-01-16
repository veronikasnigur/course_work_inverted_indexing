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
import java.util.stream.Collectors;
import java.util.ArrayList;

public class Server {
    //визначаємо порт для з'єднань
    private static final int PORT = 12345;
    //визначимо основну директорію, яка використовуватиметься у цій програмі завжди*
    private static final String BASE_DIRECTORY = "/Users/veronika_snigur/Downloads/cw/course_work_parallel_computing";
    private static final ExecutorService mainExecutorService = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running...");

            while (true) {
                // Приймаємо нового клієнта за виконання умови try
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                // Запитуємо користувача про кількість потоків для індексації
                System.out.print("Enter the number of threads for indexing: ");
                int numThreads = Integer.parseInt(new Scanner(System.in).nextLine());

                // Відправляємо в обробку клієнта з вказаною кількістю потоків
                mainExecutorService.submit(() -> handleClient(clientSocket, numThreads));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Завершуємо роботу основного пулу потоків при закритті сервера
            mainExecutorService.shutdown();
        }
    }

    private static void handleClient(Socket clientSocket, int numThreads) {
        // Створює фіксований пул потоків з вказаною кількістю потоків
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);

        try (ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream());
             ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream())) {
            // Шляхи до файлів для індексації
            List<String> filesToIndex = Arrays.asList(
                    "aclImdb/test/neg", "aclImdb/test/pos",
                    "aclImdb/train/neg", "aclImdb/train/pos", "aclImdb/train/unsup"
            );
            List<String> absoluteFilePaths = filesToIndex.stream()
                    .map(dir -> BASE_DIRECTORY + File.separator + dir)
                    .flatMap(dir -> Arrays.stream(new File(dir).listFiles()))
                    .map(File::getAbsolutePath)
                    .collect(Collectors.toList());
            // Створює об'єкт для інвертованого індексу
            InvertedIndex invertedIndex = new InvertedIndex();

            if (invertedIndex.isIndexed()) {
                System.out.println("Files are already indexed.");
            } else {
                System.out.println("Indexing files...");
                long startTime = System.currentTimeMillis();

                // Розподіляє список файлів між доступними потоками
                int filesPerThread = absoluteFilePaths.size() / numThreads;
                List<Callable<Void>> indexingTasks = new ArrayList<>();

                for (int i = 0; i < numThreads; i++) {
                    int startIndex = i * filesPerThread;
                    int endIndex = (i == numThreads - 1) ? absoluteFilePaths.size() : (i + 1) * filesPerThread;

                    List<String> subList = absoluteFilePaths.subList(startIndex, endIndex);

                    // Додає завдання індексації до списку
                    indexingTasks.add(() -> {
                        System.out.println("Indexing files in thread " + Thread.currentThread().getId() +
                                " from index " + startIndex + " to " + (endIndex - 1));
                        invertedIndex.buildIndex(subList);
                        System.out.println("Time for execution with thread "+ Thread.currentThread().getId() + ":\t" + (System.currentTimeMillis() - startTime)+" milliseconds");
                        return null;
                    });
                }

                try {
                    // Викликає всі завдання і чекає на їх завершення
                    executorService.invokeAll(indexingTasks);
                    System.out.println("Files indexed successfully.");
                    if(numThreads>1)
                    {
                        System.out.println("Total time for execution " + (System.currentTimeMillis() - startTime)+" milliseconds");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            while (true) {
                // Читає пошуковий запит від клієнта
                String query = (String) input.readObject();

                // Перевіряє, чи клієнт бажає продовжувати пошук
                if ("no".equalsIgnoreCase(query.trim())) {
                    System.out.println("Client opted out of further search. Closing connection.");
                    break;  // Виходить із циклу та закриває з'єднання
                }

                // Отримує результати пошуку за вказаним запитом
                List<IndexEntry> searchResults = invertedIndex.getSearchResults(query);

                if (!searchResults.isEmpty()) {
                    System.out.println("Files for query '" + query + "' found successfully. Results are sent to server:");

                    // Виводить результати пошуку
                    for (IndexEntry entry : searchResults) {
                        //потрібно було для перевірки правильності індексації, щоб відслідкувати знайдені співпадіння
                        // System.out.println(entry.getFilePath());
                    }
                } else {
                    System.out.println("No files found for query '" + query + "'.");
                }

                // Обгортає результати в SearchResultWrapper та відправляє клієнту
                SearchResultWrapper resultWrapper = new SearchResultWrapper(searchResults);

                output.writeObject(resultWrapper);
                output.flush(); // Очищує вихідний потік
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Завершує роботу пулу потоків після обробки клієнта
            executorService.shutdown();
        }
    }
}
