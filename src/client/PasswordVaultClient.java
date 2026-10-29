package client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

// NIO, blocking
public class PasswordVaultClient {

    private static final int SERVER_PORT = 7777;
    private static final String SERVER_HOST = "localhost";
    private static final String DISCONNECT_COMMAND = "disconnect";
    private static final int BUFFER_SIZE = 4096;

    static ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);

    public static void main() {

        try (SocketChannel socketChannel = SocketChannel.open();
             Scanner scanner = new Scanner(System.in)) {

            socketChannel.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));

            System.out.println("Connected to the server.");

            while (true) {
                System.out.print("Enter command: ");
                String command = scanner.nextLine(); // read a line from the console

                if (DISCONNECT_COMMAND.equals(command)) {
                    break;
                }
                proceedCommand(socketChannel, command);
            }

        } catch (IOException e) {
            throw new RuntimeException("There is a problem with the network communication", e);
        }
    }

    private static void proceedCommand(SocketChannel socketChannel, String message) throws IOException {
        buffer.clear(); // switch to writing mode
        buffer.put(message.getBytes(StandardCharsets.UTF_8)); // buffer fill
        buffer.flip(); // switch to reading mode
        socketChannel.write(buffer); // buffer drain

        buffer.clear(); // switch to writing mode
        socketChannel.read(buffer); // buffer fill
        buffer.flip(); // switch to reading mode

        byte[] byteArray = new byte[buffer.remaining()];
        buffer.get(byteArray);
        String reply = new String(byteArray, StandardCharsets.UTF_8); // buffer drain

        // if the buffer is a non-direct one, it has a wrapped array, and we can get it
        // String reply = new String(buffer.array(), 0, buffer.position(), "UTF-8"); // buffer drain

        System.out.println("Response: " + reply);
    }
}
