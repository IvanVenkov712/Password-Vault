package exception;

/** Indicates that a vault account name is already registered. */
public class UserAlreadyExistsException extends PasswordVaultException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }

    public UserAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
