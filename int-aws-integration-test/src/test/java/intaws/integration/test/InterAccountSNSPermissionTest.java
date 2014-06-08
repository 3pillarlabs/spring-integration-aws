package intaws.integration.test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

@RunWith(JUnit4.class)
public class InterAccountSNSPermissionTest {

	private Server server;

	@Test
	public void messagePublishFromOtherAccount() throws Exception {

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

		Thread.sleep(120000);
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

		XmlWebApplicationContext context = new XmlWebApplicationContext();
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
