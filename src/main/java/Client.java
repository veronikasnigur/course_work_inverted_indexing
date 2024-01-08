import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Arrays;
import java.util.Scanner;

public class Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 12345;
    private static final String BASE_DIRECTORY = "/Users/veronika_snigur/Downloads/cw/course_work_parallel_computing";

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, PORT);
             ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream input = new ObjectInputStream(socket.getInputStream())) {

            // Remove user input for base directory
            // output.writeObject(BASE_DIRECTORY);

            InvertedIndex invertedIndex = (InvertedIndex) input.readObject();

            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter the search query: ");
            String query = scanner.nextLine();

            List<IndexEntry> result = invertedIndex.search(query);

            System.out.println("Search results for '" + query + "':");
            for (IndexEntry entry : result) {
                System.out.println(entry);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
