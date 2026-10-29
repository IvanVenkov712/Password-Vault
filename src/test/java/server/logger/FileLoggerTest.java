package server.logger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileLoggerTest {
    @Test
    void testLogException(@TempDir Path path) {
        FileLogger logger = new FileLogger(path.resolve("error.log"));
        logger.log(new Exception("Exception thrown for testing purposes"));
    }

    @Test
    void testLogMessage(@TempDir Path path) {
        FileLogger logger = new FileLogger(path.resolve("error.log"));
        logger.log("Exception thrown for testing purposes");
    }

    @Test
    void testLogMessageMultipleMessages(@TempDir Path path) {
        Path logFile = path.resolve("error.log");
        FileLogger logger = new FileLogger(logFile);

        logger.log("First message");
        logger.log("Second message");
        logger.log("Third message");

        assertTrue(Files.exists(logFile), "Log file should be created");
    }

    @Test
    void testLogExceptionMultipleExceptions(@TempDir Path path) {
        Path logFile = path.resolve("error.log");
        FileLogger logger = new FileLogger(logFile);

        logger.log(new Exception("First exception"));
        logger.log(new RuntimeException("Second exception"));
        logger.log(new IllegalArgumentException("Third exception"));

        assertTrue(Files.exists(logFile), "Log file should be created");
    }

    @Test
    void testLogMixedMessages(@TempDir Path path) {
        Path logFile = path.resolve("error.log");
        FileLogger logger = new FileLogger(logFile);

        logger.log("Text message");
        logger.log(new Exception("Exception message"));
        logger.log("Another text message");

        assertTrue(Files.exists(logFile), "Log file should be created");
    }

    @Test
    void testLogMessageEmptyString(@TempDir Path path) {
        Path logFile = path.resolve("error.log");
        FileLogger logger = new FileLogger(logFile);

        logger.log("");

        assertTrue(Files.exists(logFile), "Log file should be created even for empty message");
    }

    @Test
    void testLogMessageLongMessage(@TempDir Path path) {
        Path logFile = path.resolve("error.log");
        FileLogger logger = new FileLogger(logFile);

        String longMessage = "A".repeat(1000);
        logger.log(longMessage);

        assertTrue(Files.exists(logFile), "Log file should handle long messages");
    }

    @Test
    void testLogExceptionNestedCause(@TempDir Path path) {
        Path logFile = path.resolve("error.log");
        FileLogger logger = new FileLogger(logFile);

        Exception cause = new RuntimeException("Root cause");
        Exception wrapper = new Exception("Wrapped exception", cause);
        logger.log(wrapper);

        assertTrue(Files.exists(logFile), "Log file should handle nested exceptions");
    }

    @Test
    void testLogMessageSpecialCharacters(@TempDir Path path) {
        Path logFile = path.resolve("error.log");
        FileLogger logger = new FileLogger(logFile);

        logger.log("Special chars: !@#$%^&*()_+-={}[]|:;<>?,./");

        assertTrue(Files.exists(logFile), "Log file should handle special characters");
    }

}