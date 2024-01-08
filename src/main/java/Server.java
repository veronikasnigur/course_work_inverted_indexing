import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.ServerSocket;


public class Server {
    private static final int PORT = 12345;
    private static final String BASE_DIRECTORY = "/Users/veronika_snigur/Downloads/cw/course_work_parallel_computing";

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream());
                ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream());

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
                    invertedIndex.buildIndex(absoluteFilePaths);
                    System.out.println("Files indexed successfully.");
                }

                output.writeObject(invertedIndex);

                // Note: Do not close the client socket inside the loop
                // clientSocket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
