package dev.forgepack.utils.api.exception;

/**
* Custom exception for EncryptorService operations
*/
public class EncryptorException extends Exception {
    public EncryptorException(String message) {
        super(message);
    }

    public EncryptorException(String message, Throwable cause) {
        super(message, cause);
    }
}
