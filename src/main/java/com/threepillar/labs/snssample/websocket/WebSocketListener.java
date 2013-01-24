package com.threepillar.labs.snssample.websocket;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketHandler;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.SmartLifecycle;
import org.springframework.web.context.ServletContextAware;

public class WebSocketListener implements InitializingBean, SmartLifecycle,
		ApplicationContextAware, ServletContextAware {

	private final Log log = LogFactory.getLog(WebSocketListener.class);
	private final Server server;
	private final ExecutorService serverThread;
	private String host;
	private int port = 9090;
	private ApplicationContext applicationContext;
	private ServletContext servletContext;

	public WebSocketListener() {
		super();
		serverThread = Executors.newSingleThreadExecutor();
		server = new Server();
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (host == null) {
			host = "localhost";
		}
		servletContext.setAttribute("websocket.host", host);
		servletContext.setAttribute("websocket.port", new Integer(port));
	}

	@Override
	public void start() {

		SelectChannelConnector wsConnector = new SelectChannelConnector();
		wsConnector.setHost(host);
		wsConnector.setPort(port);
		wsConnector.setName("webSocket");
		wsConnector.setThreadPool(new QueuedThreadPool(10));
		server.setConnectors(new Connector[] { wsConnector });
		server.setHandler(new WebSocketHandler() {

			@Override
			public WebSocket doWebSocketConnect(HttpServletRequest request,
					String protocol) {

				log.debug("Request path:" + request.getRequestURI());
				String beanName = request.getRequestURI().replaceFirst("\\/",
						"");
				final WebsocketHandler handler = applicationContext.getBean(
						beanName, WebsocketHandler.class);

				return new WebSocket.OnTextMessage() {

					@Override
					public void onOpen(Connection connection) {
						connection.setMaxIdleTime(3600000);
						handler.setConnection(connection);
						handler.start();
					}

					@Override
					public void onClose(int code, String message) {
						handler.stop();
						log.info("Connection closed.");
					}

					@Override
					public void onMessage(String data) {
						handler.postMessage(data);
					}
				};
			}
		});

		serverThread.execute(new Runnable() {

			@Override
			public void run() {
				try {
					server.start();
					server.join();
				} catch (Exception e) {
					log.warn(e.getMessage(), e);
				}
			}
		});
	}

	@Override
	public void stop() {
		try {
			if (server.isStarted()) {
				server.stop();
			}
			serverThread.shutdown();
			serverThread.awaitTermination(10, TimeUnit.SECONDS);
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
	}

	@Override
	public boolean isRunning() {
		return server.isRunning();
	}

	@Override
	public int getPhase() {
		return Integer.MAX_VALUE;
	}

	@Override
	public boolean isAutoStartup() {
		return true;
	}

	@Override
	public void stop(Runnable callback) {
		stop();
		callback.run();
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

}
