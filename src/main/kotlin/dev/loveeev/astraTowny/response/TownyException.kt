package dev.loveeev.astratowny.response;

public class TownyException extends RuntimeException {

    public TownyException(String message) {
        super(message);
    }

    public TownyException(String message, Throwable cause) {
        super(message, cause);
    }
}
