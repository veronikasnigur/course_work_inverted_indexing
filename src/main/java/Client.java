import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

public class Client {
    // Визначення адреси та порту сервера
    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 12345;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, PORT);
             ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream input = new ObjectInputStream(socket.getInputStream())) {

            // Сканер для зчитування вводу від користувача
            Scanner scanner = new Scanner(System.in);

            do {
                // Запит користувача ввести пошуковий запит
                System.out.print("Enter the search query: ");
                String query = scanner.nextLine();
                // Відправка пошукового запиту на сервер
                output.writeObject(query);

                // Зчитування відповіді сервера з результатами пошуку та запис у wrapper
                SearchResultWrapper resultWrapper = (SearchResultWrapper) input.readObject();
                // Виведення результатів пошуку для введеного запиту
                System.out.println("Search results for '" + query + "':");
                List<IndexEntry> result = resultWrapper.getResults();
                for (IndexEntry entry : result) {
                    System.out.println(entry);
                }
                // Запит користувача, чи вони хочуть шукати інше слово
                System.out.print("Do you want to search for another word? (yes/no): ");
            } while ("yes".equalsIgnoreCase(scanner.nextLine().trim()));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
