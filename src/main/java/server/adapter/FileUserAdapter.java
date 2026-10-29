package server.adapter;

import server.model.User;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Serializes the complete account map into one Java object stream.
 * Writes replace the target file directly; they are not atomic and have no backup.
 */
public class FileUserAdapter implements UserAdapter {
    private static final String DEFAULT_USERS_FILE_PATH = "users";

    private final Path path;

    /**
     * Configures the serialized users file without reading or creating it.
     *
     * @param path file used for both loading and saving
     * @throws IllegalArgumentException if path is null
     */
    public FileUserAdapter(Path path) {
        if (path == null) {
            throw new IllegalArgumentException("path is null");
        }
        this.path = path;
    }

    /** Uses the {@code users} file relative to the process working directory. */
    public FileUserAdapter() {
        this(Path.of(DEFAULT_USERS_FILE_PATH));
    }

    /**
     * Loads an existing serialized map or returns an empty map if the file is absent.
     * Only trusted application-created files should be used as input.
     *
     * @return mutable accounts indexed by vault username
     * @throws RuntimeException if the file cannot be read or its classes cannot be resolved
     */
    @Override
    public Map<String, User> loadUsers() {
        Map<String, User> users;
        if (!Files.exists(path)) {
            users = new ConcurrentHashMap<>();
        } else {
            try (var stream = new ObjectInputStream(new BufferedInputStream(Files.newInputStream(path)))) {
                users = (Map<String, User>) stream.readObject();
            } catch (ClassNotFoundException | IOException e) {
                throw new RuntimeException(e);
            }
        }
        return users;
    }

    /**
     * Truncates the configured file and serializes the entire account map.
     *
     * @param users accounts and encrypted credentials to persist
     * @throws RuntimeException if writing fails; the previous file may already be truncated
     */
    @Override
    public void saveUsers(Map<String, User> users) {
        try (ObjectOutputStream output = new ObjectOutputStream(
            new BufferedOutputStream(Files.newOutputStream(path)))) {

            output.writeObject(users);
            output.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
