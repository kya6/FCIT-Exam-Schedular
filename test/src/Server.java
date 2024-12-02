import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) throws IOException {
        DatabaseHandler.clear();
        DatabaseHandler.initializeDatabase();
        try {
            System.out.println("Server on, Waiting for clients....");
            int Client_Number = 1;
            ServerSocket server_socket = new ServerSocket(12345);
            while (true) {
                Socket incoming = server_socket.accept();
                System.out.println("Thread Number: " + Client_Number);
                Runnable r = new ClientHandler(incoming);
                Thread th = new Thread(r);
                th.start();
                th.join();

                Client_Number++;

            }
        } catch (IOException | InterruptedException e) {
        }

    }

}
