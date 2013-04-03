package com.threepillar.labs.si.sns.core;

import org.springframework.integration.Message;

import com.threepillar.labs.si.aws.MessagePacket;

public abstract class NotificationHandler {

	protected abstract void dispatch(Message<?> message);

	public void onNotification(String notification) {
		MessagePacket packet = MessagePacket.fromJSON(notification);
		dispatch(packet.assemble());
	}
}
