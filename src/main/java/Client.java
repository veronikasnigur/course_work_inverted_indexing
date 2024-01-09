import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

public class Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 12345;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, PORT);
             ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream input = new ObjectInputStream(socket.getInputStream())) {

            Scanner scanner = new Scanner(System.in);

            do {
                System.out.print("Enter the search query: ");
                String query = scanner.nextLine();

                output.writeObject(query);

                // Read the server's response wrapper
                SearchResultWrapper resultWrapper = (SearchResultWrapper) input.readObject();

                System.out.println("Search results for '" + query + "':");
                List<IndexEntry> result = resultWrapper.getResults();
                for (IndexEntry entry : result) {
                    System.out.println(entry);
                }

                System.out.print("Do you want to search for another word? (yes/no): ");
            } while ("yes".equalsIgnoreCase(scanner.nextLine().trim()));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
