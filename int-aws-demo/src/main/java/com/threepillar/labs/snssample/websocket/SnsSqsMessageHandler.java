package com.threepillar.labs.snssample.websocket;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.websocket.WebSocket.Connection;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.MessagingException;
import org.springframework.integration.core.MessageHandler;
import org.springframework.integration.core.SubscribableChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.util.Assert;

public class SnsSqsMessageHandler implements WebsocketHandler, MessageHandler,
		ApplicationContextAware {

	private final Log log = LogFactory.getLog(SnsSqsMessageHandler.class);

	private ApplicationContext applicationContext;
	private MessageChannel logSnsOutbound;
	private SubscribableChannel logSqsInbound;
	private Connection connection;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
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
	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	@Override
	public void start() {
		logSnsOutbound = applicationContext.getBean("logSnsOutbound",
				MessageChannel.class);
		Assert.notNull(logSnsOutbound, "'logSnsOutbound' must not be null");
		logSqsInbound = applicationContext.getBean("logSqsInbound",
				SubscribableChannel.class);
		Assert.notNull(logSqsInbound, "'logSqsInbound' must not be null");
		logSqsInbound.subscribe(this);
	}

	@Override
	public void stop() {
		logSqsInbound.unsubscribe(this);
	}

	@Override
	public void postMessage(String data) {
		logSnsOutbound.send(MessageBuilder.withPayload(data).build());
	}

}
