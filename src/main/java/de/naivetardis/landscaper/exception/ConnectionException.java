package de.naivetardis.landscaper.exception;

public class ConnectionException extends RuntimeException {
    public ConnectionException() {
        super("(╯°□°）╯︵ ┻━┻");
    }

    public ConnectionException(Throwable e) {
        super("(╯°□°）╯︵ ┻━┻ :" + e.getMessage());
    }
}
