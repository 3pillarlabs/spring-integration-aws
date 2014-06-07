package org.springframework.integration.aws;

import org.springframework.integration.Message;

/**
 * Interface for marshalling a Message to a String suitable for sending to AWS
 * and unmarshalling message from AWS back to a Message.
 * 
 * @author Sayantam Dey
 * 
 */
public interface MessageMarshaller {

	/**
	 * Converts a Message to a String.
	 * 
	 * @param message
	 * @return serialized string
	 * @throws MessageMarshallerException
	 */
	String serialize(Message<?> message) throws MessageMarshallerException;

	/**
	 * Converts input String to Message.
	 * 
	 * @param input
	 * @return Message
	 * @throws MessageMarshallerException
	 */
	Message<?> deserialize(String input) throws MessageMarshallerException;
}
