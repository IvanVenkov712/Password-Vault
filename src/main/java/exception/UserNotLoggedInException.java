package exception;

/** Indicates that an operation requires an authenticated connection. */
public class UserNotLoggedInException extends PasswordVaultException {
    public UserNotLoggedInException(String message) {
        super(message);
    }

    public UserNotLoggedInException(String message, Throwable cause) {
        super(message, cause);
    }
}
