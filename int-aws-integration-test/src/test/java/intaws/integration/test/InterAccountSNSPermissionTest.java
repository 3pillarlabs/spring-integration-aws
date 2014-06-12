package intaws.integration.test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.MessagingException;
import org.springframework.integration.core.MessageHandler;
import org.springframework.integration.core.SubscribableChannel;
import org.springframework.integration.endpoint.EventDrivenConsumer;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.test.util.TestUtils;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

@RunWith(JUnit4.class)
public class InterAccountSNSPermissionTest {

	private Server server;
	private XmlWebApplicationContext context;
	private Set<String> recdMessages;

	@Test
	public void messagePublishFromOtherAccount() throws Exception {

		final String msg1 = "This is the first message";
		final String msg2 = "This is the second message";

		recdMessages = new HashSet<String>();
		server = createServer();
		server.setHandler(getServletContextHandler());

		ExecutorService webThread = Executors.newSingleThreadExecutor();
		webThread.execute(new Runnable() {

			@Override
			public void run() {
				try {
					server.start();
					server.join();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		});

		// wait for context load
		Thread.sleep(TimeUnit.MINUTES.toMillis(1));

		SubscribableChannel destChannel = context.getBean(
				"message-destination", SubscribableChannel.class);
		destChannel.subscribe(new MessageHandler() {

			@Override
			public void handleMessage(Message<?> message)
					throws MessagingException {
				synchronized (recdMessages) {
					recdMessages.add((String) message.getPayload());
				}
			}
		});

		EventDrivenConsumer srcConsumer = context.getBean("sns-outbound",
				EventDrivenConsumer.class);
		MessageChannel srcChannel = TestUtils.getPropertyValue(srcConsumer,
				"inputChannel", MessageChannel.class);
		srcChannel.send(MessageBuilder.withPayload(msg1).build());
		srcChannel.send(MessageBuilder.withPayload(msg2).build());

		// wait for messages to go out and come back
		Thread.sleep(TimeUnit.MINUTES.toMillis(1));

		assertThat(recdMessages, containsInAnyOrder(msg1, msg2));

		server.stop();
		webThread.shutdown();
	}

	private Server createServer() throws IOException {
		Resource propsFile = new ClassPathResource(
				"InterAccountSNSPermissionFlow.properties", getClass());
		Properties props = new Properties();
		props.load(propsFile.getInputStream());

		return new Server(new InetSocketAddress(
				props.getProperty("reachable.host"), Integer.valueOf(props
						.getProperty("reachable.port"))));
	}

	private ServletContextHandler getServletContextHandler() throws Exception {

		context = new XmlWebApplicationContext();
		context.setConfigLocation("src/main/webapp/WEB-INF/InterAccountSNSPermissionFlow.xml");
		context.registerShutdownHook();

		ServletContextHandler contextHandler = new ServletContextHandler();
		contextHandler.setErrorHandler(null);
		contextHandler.setResourceBase(".");
		ServletHolder servletHolder = new ServletHolder(new DispatcherServlet(
				context));
		contextHandler.addServlet(servletHolder, "/*");

		return contextHandler;
	}

}
