package server.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * Persisted vault account whose equality and hash code depend only on its username.
 * The credential map is mutable and is not defensively copied.
 *
 * @param userName vault account identifier
 * @param hashedPassword account password hash, not an encrypted website password
 * @param passwords website entries indexed by exact website string
 */
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
