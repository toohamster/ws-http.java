package ws.http.tools.xml;

@SuppressWarnings("serial")
public class XMLException extends RuntimeException {
    
    /**
     * Constructs a XMLException with an explanatory message.
     *
     * @param message
     *            Detail about the reason for the exception.
     */
    public XMLException(final String message) {
        super(message);
    }

    /**
     * Constructs a XMLException with an explanatory message and cause.
     * 
     * @param message
     *            Detail about the reason for the exception.
     * @param cause
     *            The cause.
     */
    public XMLException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new XMLException with the specified cause.
     * 
     * @param cause
     *            The cause.
     */
    public XMLException(final Throwable cause) {
        super(cause.getMessage(), cause);
    }

}
