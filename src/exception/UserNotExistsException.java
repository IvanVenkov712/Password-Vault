package exception;

public class UserNotExistsException extends PasswordVaultException {
    public UserNotExistsException(String message) {
        super(message);
    }
}
