package server.model;

import java.io.Serial;
import java.io.Serializable;

public record WebsitePair(String userName, byte[] encryptedPassword) implements Serializable {

    @Serial
    private static final long serialVersionUID = -1694429386310073645L;
}
