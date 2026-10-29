package exception;

/** Indicates that a vault account password did not match its stored hash. */
public class IncorrectPasswordException extends PasswordVaultException {
    public IncorrectPasswordException(String message) {
        super(message);
    }

    public IncorrectPasswordException(String message, Throwable cause) {
        super(message, cause);
    }
}
