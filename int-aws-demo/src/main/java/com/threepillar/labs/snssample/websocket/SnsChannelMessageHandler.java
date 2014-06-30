package com.threepillar.labs.snssample.websocket;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.websocket.WebSocket.Connection;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.integration.Message;
import org.springframework.integration.MessagingException;
import org.springframework.integration.core.MessageHandler;
import org.springframework.integration.core.SubscribableChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.util.Assert;

public class SnsChannelMessageHandler implements WebsocketHandler,
		MessageHandler, ApplicationContextAware {

	private final Log log = LogFactory.getLog(SnsChannelMessageHandler.class);

	private Connection connection;
	private ApplicationContext applicationContext;
	private SubscribableChannel channel;

	@Override
	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	@Override
	public void start() {
		channel = applicationContext.getBean("snsChannel",
				SubscribableChannel.class);
		Assert.notNull(channel, "'channel' must not be null");
		channel.subscribe(this);
	}

	@Override
	public void stop() {
		channel.unsubscribe(this);
	}

	@Override
	public void postMessage(String data) {
		channel.send(MessageBuilder.withPayload(data).build());
	}

	@Override
	public void handleMessage(Message<?> message) throws MessagingException {

		Object payload = message.getPayload();
		try {
			connection.sendMessage(payload.toString());
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

}
