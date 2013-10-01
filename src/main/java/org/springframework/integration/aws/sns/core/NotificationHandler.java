package org.springframework.integration.aws.sns.core;

import org.springframework.integration.Message;
import org.springframework.integration.aws.MessagePacket;


public abstract class NotificationHandler {

	protected abstract void dispatch(Message<?> message);

	public void onNotification(String notification) {
		MessagePacket packet = MessagePacket.fromJSON(notification);
		dispatch(packet.assemble());
	}
}
