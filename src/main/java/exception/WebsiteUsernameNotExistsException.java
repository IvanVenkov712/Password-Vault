package exception;

public class WebsiteUsernameNotExistsException extends PasswordVaultException {
    public WebsiteUsernameNotExistsException(String message) {
        super(message);
    }

    public WebsiteUsernameNotExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
