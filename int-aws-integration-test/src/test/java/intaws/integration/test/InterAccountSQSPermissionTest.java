package intaws.integration.test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.MessagingException;
import org.springframework.integration.core.MessageHandler;
import org.springframework.integration.core.SubscribableChannel;
import org.springframework.integration.support.MessageBuilder;

@RunWith(JUnit4.class)
public class InterAccountSQSPermissionTest {

	private final Set<String> messages;
	private ConfigurableApplicationContext appCtx;

	public InterAccountSQSPermissionTest() {
		this.messages = new HashSet<String>();
	}

	@Test
	public void messagePublishFromOtherAccount() throws InterruptedException {

		appCtx = new ClassPathXmlApplicationContext(
				"InterAccountSQSPermissionTest.xml", getClass());

		SubscribableChannel inboundChannel = appCtx.getBean("message-in",
				SubscribableChannel.class);
		inboundChannel.subscribe(new MessageHandler() {

			@Override
			public void handleMessage(Message<?> message)
					throws MessagingException {
				synchronized (messages) {
					messages.add((String) message.getPayload());
				}
			}
		});

		MessageChannel outboundChannel = appCtx.getBean("message-out",
				MessageChannel.class);
		final String msg1 = "This is message 1";
		outboundChannel.send(MessageBuilder.withPayload(msg1).build());

		Thread.sleep(30000);

		assertThat(messages, contains(msg1));
	}

	@After
	public void teardown() {
		if (appCtx != null) {
			appCtx.close();
		}
	}
}
