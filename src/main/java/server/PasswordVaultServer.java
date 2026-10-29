package server;

import server.command.CommandExecutor;
import server.command.factory.CommandFactoryImpl;
import server.repository.AdapterUserRepository;
import server.repository.UserRepository;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

/**
 * Serves UTF-8 commands on localhost using a single NIO selector thread.
 * Each read is treated as one complete request and each response uses one write;
 * the current transport does not frame messages or retry partial writes.
 */
public class PasswordVaultServer {

    /** Default port used by the server entry point and console client. */
    public static final int DEFAULT_SERVER_PORT = 7777;

    private static final int BUFFER_SIZE = 4096;
    private static final String HOST = "localhost";

    private final CommandExecutor commandExecutor;
    private final int port;

    private boolean isServerWorking;
    private Selector selector;

    /**
     * Configures a server without opening its listening socket.
     *
     * @param port localhost port to bind when started
     * @param commandExecutor parser, executor, and repository access point
     */
    public PasswordVaultServer(int port, CommandExecutor commandExecutor) {
        this.port = port;
        this.commandExecutor = commandExecutor;
    }

    /**
     * Opens the listening socket and runs the selector loop on the calling thread.
     * Repository work, including file writes and password checks, runs on that thread.
     *
     * @throws UncheckedIOException if socket setup or processing fails
     */
    public void start() {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
             Selector selector = Selector.open()) {

            this.selector = selector;
            configureServerSocketChannel(serverSocketChannel, selector);

            isServerWorking = true;

            while (isServerWorking) {
                int readyChannels = selector.select(); // blocking

                if (readyChannels == 0) {
                    continue;
                }

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                handleSelectedKeys(selectedKeys);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to start server", e);
        }
    }

    void handleSelectedKeys(Set<SelectionKey> selectedKeys) throws IOException {
        Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

        while (keyIterator.hasNext()) {
            SelectionKey key = keyIterator.next();
            keyIterator.remove(); // avoid double-processing

            if (!key.isValid()) {
                continue;
            }

            if (key.isAcceptable()) {
                accept(selector, key);
            } else if (key.isReadable()) {
                handleRead(key);
            }
        }
    }

    /**
     * Requests loop termination, wakes the selector, and closes the repository.
     * The command-line entry point does not install a shutdown hook calling this method.
     *
     * @throws RuntimeException if repository closure reports an I/O failure
     */
    public void stop() {
        isServerWorking = false;
        if (selector != null && selector.isOpen()) {
            selector.wakeup();
        }
        try {
            commandExecutor.getRepository().close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void configureServerSocketChannel(ServerSocketChannel channel, Selector selector) throws IOException {
        channel.bind(new InetSocketAddress(HOST, port));
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_ACCEPT);
    }

    private void accept(Selector selector, SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();

        // In non-blocking mode accept() may return null if no connection is ready
        if (clientChannel == null) {
            return;
        }

        clientChannel.configureBlocking(false);

        // Attach per-client state (here: this connection's buffer) to the selection key
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        clientChannel.register(selector, SelectionKey.OP_READ, buffer);

        System.out.println("Accepted connection from " + clientChannel.getRemoteAddress());
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = (ByteBuffer) key.attachment();

        buffer.clear();
        int readBytes = clientChannel.read(buffer);

        if (readBytes < 0) {
            System.out.println("Client disconnected: " + clientChannel.getRemoteAddress());
            clientChannel.close();
            key.cancel();
            return;
        }

        if (readBytes == 0) {
            return; // nothing to process right now
        }

        buffer.flip();

        byte[] clientInputBytes = new byte[buffer.remaining()];
        buffer.get(clientInputBytes);

        String clientInput = new String(clientInputBytes, StandardCharsets.UTF_8);
        System.out.println(clientInput);
        String output = commandExecutor.execute(key, clientInput).message();
        writeClientOutput(clientChannel, output, buffer);
    }

    private void writeClientOutput(SocketChannel clientChannel, String output, ByteBuffer buffer) throws IOException {
        buffer.clear();
        buffer.put(output.getBytes(StandardCharsets.UTF_8));
        buffer.flip();

        // Simple example: assume the socket can accept the whole response
        clientChannel.write(buffer);
    }

    /** Starts the default server using file persistence and the API password checker. */
    static void main() {
        UserRepository repository = new AdapterUserRepository();
        CommandExecutor executor = new CommandExecutor(new CommandFactoryImpl(repository));
        PasswordVaultServer server = new PasswordVaultServer(DEFAULT_SERVER_PORT, executor);
        server.start();
    }
}
