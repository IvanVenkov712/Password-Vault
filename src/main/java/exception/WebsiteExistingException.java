package exception;

/** Indicates that the vault already contains an entry for the requested website. */
public class WebsiteExistingException extends PasswordVaultException {
    public WebsiteExistingException(String message) {
        super(message);
    }

    public WebsiteExistingException(String message, Throwable cause) {
        super(message, cause);
    }
}
