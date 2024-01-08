import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static Scanner scanner = new Scanner(System.in);
    private static DataInputStream inputStream;
    private static DataOutputStream outputStream;

    public static void main(String[] args) throws IOException {
        try (Socket socket = new Socket("localhost", 8000)) {
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());
            boolean findOneMoreWord = false;
            System.out.println("Welcome to 'Indexer'!!! \nHere you can search words in movie reviews. \nSo we can start our work ;)");

            System.out.println("First step: files indexing. Now you will enter number of threads that will be used to index files.");

        } catch (Exception e) {
            throw e;
        }
    }
}
