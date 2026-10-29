package server.password.checker;

import java.io.Closeable;

public interface PasswordChecker extends Closeable {
    boolean isPasswordSecure(String password);
}
