package org.springframework.integration.aws;

/**
 * Exception to be thrown by MessageMarshaller implementations.
 * 
 * @author Sayantam Dey
 * 
 */
public class MessageMarshallerException extends Exception {

	private static final long serialVersionUID = 1L;

	public MessageMarshallerException(String message, Throwable cause) {
		super(message, cause);
	}

	public MessageMarshallerException(String message) {
		super(message);
	}

}
