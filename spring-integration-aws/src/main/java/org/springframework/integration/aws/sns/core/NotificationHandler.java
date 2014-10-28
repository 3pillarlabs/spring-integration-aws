package org.springframework.integration.aws.sns.core;

import org.springframework.integration.aws.MessageMarshaller;
import org.springframework.integration.aws.MessageMarshallerException;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;

public abstract class NotificationHandler {

	private MessageMarshaller messageMarshaller;

	public void setMessageMarshaller(MessageMarshaller messageMarshaller) {
		this.messageMarshaller = messageMarshaller;
	}

	public void onNotification(String notification) {

		try {
			dispatch(messageMarshaller.deserialize(notification));

		} catch (MessageMarshallerException e) {
			throw new MessagingException(e.getMessage(), e.getCause());
		}
	}

	protected abstract void dispatch(Message<?> message);

}
