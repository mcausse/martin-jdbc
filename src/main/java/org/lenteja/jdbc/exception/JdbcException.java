package org.lenteja.jdbc.exception;

public class JdbcException extends RuntimeException {

    private static final long serialVersionUID = 8727129333282283655L;

    public JdbcException() {
        super();
    }

    public JdbcException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public JdbcException(final String message) {
        super(message);
    }

    public JdbcException(final Throwable cause) {
        super(cause);
    }

}