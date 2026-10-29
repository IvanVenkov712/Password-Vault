package server.adapter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import server.model.User;

import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FileUserAdapterTest {
    @Test
    void testSaveLoadUsers(@TempDir Path tempDir) {
        Map<String, User> users = Map.of(
            "A", new User("A", "APass", Map.of()),
            "B", new User("B", "BPass", Map.of()),
            "C", new User("C", "CPass", Map.of()),
            "D", new User("D", "DPass", Map.of()),
            "E", new User("E", "EPass", Map.of())
        );

        FileUserAdapter adapter = new FileUserAdapter(tempDir.resolve("users.dat"));
        adapter.saveUsers(users);
        assertEquals(users, adapter.loadUsers(), "The users should remain unchanged!");
    }

    @Test
    void testSaveLoadUsersEmptyMap(@TempDir Path tempDir) {
        Map<String, User> emptyUsers = Map.of();

        FileUserAdapter adapter = new FileUserAdapter(tempDir.resolve("users.dat"));
        adapter.saveUsers(emptyUsers);
        assertEquals(emptyUsers, adapter.loadUsers(), "Empty map should be preserved");
    }

    @Test
    void testSaveLoadUsersSingleUser(@TempDir Path tempDir) {
        Map<String, User> users = Map.of(
            "user1", new User("user1", "pass123", Map.of())
        );

        FileUserAdapter adapter = new FileUserAdapter(tempDir.resolve("users.dat"));
        adapter.saveUsers(users);
        assertEquals(users, adapter.loadUsers(), "Single user should be preserved");
    }

    @Test
    void testSaveLoadUsersUserWithPasswords(@TempDir Path tempDir) {
        Map<String, User> users = Map.of(
            "user1", new User("user1", "hashedPass", Map.of(
                "facebook", new server.model.WebsitePair("user@email.com", "encrypted1".getBytes()),
                "github", new server.model.WebsitePair("user@github.com", "encrypted2".getBytes())
            ))
        );

        FileUserAdapter adapter = new FileUserAdapter(tempDir.resolve("users.dat"));
        adapter.saveUsers(users);
        Map<String, User> loaded = adapter.loadUsers();

        assertEquals(users, loaded, "Users with passwords should be preserved");
        assertTrue(loaded.get("user1").passwords().containsKey("facebook"));
        assertTrue(loaded.get("user1").passwords().containsKey("github"));
    }

    @Test
    void testSaveLoadUsersOverwrite(@TempDir Path tempDir) {
        Path filePath = tempDir.resolve("users.dat");

        Map<String, User> firstSave = Map.of(
            "user1", new User("user1", "pass1", Map.of())
        );

        FileUserAdapter adapter = new FileUserAdapter(filePath);
        adapter.saveUsers(firstSave);
        assertEquals(1, adapter.loadUsers().size(), "First save should have 1 user");

        Map<String, User> secondSave = Map.of(
            "user2", new User("user2", "pass2", Map.of()),
            "user3", new User("user3", "pass3", Map.of())
        );

        adapter.saveUsers(secondSave);
        Map<String, User> loaded = adapter.loadUsers();

        assertEquals(2, loaded.size(), "Second save should overwrite with 2 users");
        assertFalse(loaded.containsKey("user1"), "First user should be gone");
        assertTrue(loaded.containsKey("user2"), "user2 should exist");
        assertTrue(loaded.containsKey("user3"), "user3 should exist");
    }

    @Test
    void testLoadUsersNonExistentFile(@TempDir Path tempDir) {
        FileUserAdapter adapter = new FileUserAdapter(tempDir.resolve("nonexistent.dat"));
        Map<String, User> result = adapter.loadUsers();
        assertNotNull(result, "Should return a map");
    }

    @Test
    void testSaveLoadUsersLargeDataSet(@TempDir Path tempDir) {
        Map<String, User> users = new java.util.HashMap<>();
        for (int i = 0; i < 100; i++) {
            users.put("user" + i, new User("user" + i, "pass" + i, Map.of()));
        }

        FileUserAdapter adapter = new FileUserAdapter(tempDir.resolve("users.dat"));
        adapter.saveUsers(users);
        Map<String, User> loaded = adapter.loadUsers();

        assertEquals(100, loaded.size(), "All 100 users should be preserved");
        assertEquals(users, loaded, "Large dataset should be intact");
    }

    @Test
    void testSaveLoadUsersMultipleAdapters(@TempDir Path tempDir) {
        Path filePath = tempDir.resolve("sharedUsers.dat");

        Map<String, User> users = Map.of(
            "shared", new User("shared", "sharedpass", Map.of())
        );

        FileUserAdapter adapter1 = new FileUserAdapter(filePath);
        adapter1.saveUsers(users);

        FileUserAdapter adapter2 = new FileUserAdapter(filePath);
        Map<String, User> loaded = adapter2.loadUsers();

        assertEquals(users, loaded, "Different adapter instances should see same data");
    }

}