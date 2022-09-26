package de.naivetardis.landscaper.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConnectionException extends RuntimeException {
    public ConnectionException() {
        super("(╯°□°）╯︵ ┻━┻");
    }

    public ConnectionException(Throwable e) {
        super("(╯°□°）╯︵ ┻━┻ :" + e.getMessage());
        log.error(super.getMessage());
    }

    public ConnectionException(String s) {
        super("(╯°□°）╯︵ ┻━┻ :" + s);
        log.error(super.getMessage());
    }
}
