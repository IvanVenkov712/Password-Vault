package exception;

/** Indicates that the requested website username differs from the stored username. */
public class WebsiteUsernameNotExistsException extends PasswordVaultException {
    public WebsiteUsernameNotExistsException(String message) {
        super(message);
    }

    public WebsiteUsernameNotExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
