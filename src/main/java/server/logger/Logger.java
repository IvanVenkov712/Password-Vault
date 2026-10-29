package server.logger;

public interface Logger {
    void log(String message);

    void log(Exception e);
}
