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
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageHandler;
import org.springframework.util.Assert;

public class SnsInboundMessageHandler implements MessageHandler,
		WebsocketHandler, ApplicationContextAware {

	private final Log log = LogFactory.getLog(SnsInboundMessageHandler.class);

	private ApplicationContext applicationContext;
	private DirectChannel logSnsInbound;
	private Connection connection;

	@Override
	public void setConnection(Connection connection) {
		this.connection = connection;
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
	public void start() {
		logSnsInbound = applicationContext.getBean("logSnsInbound",
				DirectChannel.class);
		Assert.notNull(logSnsInbound, "'logSnsInbound' must not be null");
		logSnsInbound.subscribe(this);
	}

	@Override
	public void stop() {
		logSnsInbound.unsubscribe(this);
	}

	@Override
	public void postMessage(String data) {
		// no op: this handler only sends back data.
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

}
