package server.logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.time.LocalDateTime;

/** Appends timestamps, messages, and exception stack traces to a local file. */
public class FileLogger implements Logger {
    private static final String DEFAULT_FILE_PATH = "error.log";
    private static final String MESSAGE_FORMAT_STRING = "{0}: {1}";
    private static final String DELIMITER = ": ";

    private final Path path;

    /**
     * Configures the log destination; each log call opens the file in append mode.
     *
     * @param path destination file
     */
    public FileLogger(Path path) {
        this.path = path;
    }

    /** Uses {@code error.log} in the process working directory. */
    public FileLogger() {
        this(Path.of(DEFAULT_FILE_PATH));
    }

    @Override
    public void log(String message) {
        String dateTime = LocalDateTime.now().toString();
        try (var out = new PrintWriter(new BufferedWriter(new FileWriter(path.toFile(), true)))) {
            out.println(MessageFormat.format(MESSAGE_FORMAT_STRING, dateTime, message));
            out.flush();
        } catch (IOException ex) {
            ex.printStackTrace(System.out);
        }
    }

    @Override
    public void log(Exception e) {
        String dateTime = LocalDateTime.now().toString();
        try (var out = new PrintWriter(new BufferedWriter(new FileWriter(path.toFile(), true)))) {
            out.print(dateTime + DELIMITER);
            e.printStackTrace(out);
            out.flush();
        } catch (IOException ex) {
            ex.printStackTrace(System.out);
        }
    }
}
