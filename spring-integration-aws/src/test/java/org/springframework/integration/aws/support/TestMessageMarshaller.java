package org.springframework.integration.aws.support;

import org.springframework.messaging.Message;
import org.springframework.integration.aws.MessageMarshaller;
import org.springframework.integration.aws.MessageMarshallerException;
import org.springframework.integration.support.MessageBuilder;

/**
 * Dummy implementation for configuration tests.
 * 
 * @author Sayantam Dey
 * 
 */
public class TestMessageMarshaller implements MessageMarshaller {

	@Override
	public String serialize(Message<?> message)
			throws MessageMarshallerException {

		return message.getPayload().toString();
	}

	@Override
	public Message<?> deserialize(String input)
			throws MessageMarshallerException {

		return MessageBuilder.withPayload(input).build();
	}

}
