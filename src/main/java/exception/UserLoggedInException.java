package exception;

public class UserLoggedInException extends PasswordVaultException {
    public UserLoggedInException(String message) {
        super(message);
    }

    public UserLoggedInException(String message, Throwable cause) {
        super(message, cause);
    }
}
