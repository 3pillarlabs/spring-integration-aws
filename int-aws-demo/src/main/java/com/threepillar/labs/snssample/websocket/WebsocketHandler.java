package com.threepillar.labs.snssample.websocket;

import org.eclipse.jetty.websocket.WebSocket.Connection;

public interface WebsocketHandler {

	public abstract void setConnection(Connection connection);

	public abstract void start();

	public abstract void stop();

	public abstract void postMessage(String data);

}