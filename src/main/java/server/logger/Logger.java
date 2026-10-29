package server.logger;

/** Records command failures and diagnostic messages. */
public interface Logger {
    /**
     * Records a diagnostic message.
     *
     * @param message text to log
     */
    void log(String message);

    /**
     * Records an exception with diagnostic details.
     *
     * @param e failure to log
     */
    void log(Exception e);
}
