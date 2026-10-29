package exception;

/** Indicates that the requested vault account is not registered. */
public class UserNotExistsException extends PasswordVaultException {
    public UserNotExistsException(String message) {
        super(message);
    }
}
