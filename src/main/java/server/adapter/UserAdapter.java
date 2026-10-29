package server.adapter;

import server.model.User;

import java.util.Map;

/** Persistence boundary for vault accounts and their encrypted website credentials. */
public interface UserAdapter {
    /** @return the stored accounts indexed by vault username, or an empty map for a new store */
    Map<String, User> loadUsers();

    /**
     * Writes the supplied account snapshot to persistent storage.
     *
     * @param users accounts indexed by vault username
     */
    void saveUsers(Map<String, User> users);
}
