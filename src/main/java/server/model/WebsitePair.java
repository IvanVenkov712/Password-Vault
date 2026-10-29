package server.model;

import java.io.Serial;
import java.io.Serializable;

/**
 * Persisted credential pair for a website; the byte array is not defensively copied.
 *
 * @param userName website account username
 * @param encryptedPassword ciphertext encrypted with the server's shared key
 */
public record WebsitePair(String userName, byte[] encryptedPassword) implements Serializable {

    @Serial
    private static final long serialVersionUID = -1694429386310073645L;
}
