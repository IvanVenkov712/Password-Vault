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

public class FileUserAdapter implements UserAdapter {
    private static final String DEFAULT_USERS_FILE_PATH = "users";

    private final Path path;

    public FileUserAdapter(Path path) {
        if (path == null) {
            throw new IllegalArgumentException("path is null");
        }
        this.path = path;
    }

    public FileUserAdapter() {
        this(Path.of(DEFAULT_USERS_FILE_PATH));
    }

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
