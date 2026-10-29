package server.adapter;

import server.model.User;

import java.util.Map;

public interface UserAdapter {
    Map<String, User> loadUsers();

    void saveUsers(Map<String, User> users);
}
