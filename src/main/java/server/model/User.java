package server.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

public record User(String userName, String hashedPassword, Map<String, WebsitePair> passwords)
    implements Serializable {

    @Serial
    private static final long serialVersionUID = -3294026624543407027L;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;
        return Objects.equals(userName, user.userName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(userName);
    }
}
