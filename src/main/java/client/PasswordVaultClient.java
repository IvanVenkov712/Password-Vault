package client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Interactive blocking client for the server on localhost:7777.
 * Commands are sent as UTF-8 text; {@code disconnect} closes the socket locally.
 */
public class PasswordVaultClient {

    private static final int SERVER_PORT = 7777;
    private static final String SERVER_HOST = "localhost";
    private static final String DISCONNECT_COMMAND = "disconnect";
    private static final int BUFFER_SIZE = 4096;

    static ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);

    /**
     * Connects to the server and processes console commands until disconnect.
     *
     * @throws RuntimeException if network communication fails
     */
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

    /**
     * Sends a command and prints the bytes returned by one blocking read.
     * Both requests and responses must fit the 4096-byte buffer; message framing
     * and partial reads or writes are not handled.
     *
     * @param socketChannel connected blocking channel
     * @param message command text
     * @throws IOException if channel communication fails
     */
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
