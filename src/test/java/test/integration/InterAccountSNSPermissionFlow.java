package test.integration;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Properties;

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
public class InterAccountSNSPermissionFlow {

	@Test
	public void messagePublishFromOtherAccount() throws Exception {

		Server server = getServer();
		server.setHandler(getServletContextHandler());
		server.start();
		server.join();
		Thread.sleep(2000);
		server.stop();
	}

	private Server getServer() throws IOException {
		Resource propsFile = new ClassPathResource(
				"InterAccountSNSPermissionFlow.properties", getClass());
		Properties props = new Properties();
		props.load(propsFile.getInputStream());

		return new Server(new InetSocketAddress(
				props.getProperty("reachable.host"), Integer.valueOf(props
						.getProperty("reachable.port"))));
	}

	private ServletContextHandler getServletContextHandler() throws Exception {
		Resource contextFile = new ClassPathResource(
				"/WEB-INF/InterAccountSNSPermissionFlow.xml", getClass());

		XmlWebApplicationContext context = new XmlWebApplicationContext();
		context.setConfigLocation("/target/test-classes/WEB-INF/InterAccountSNSPermissionFlow.xml");
		context.registerShutdownHook();
		// context.refresh();

		ServletContextHandler contextHandler = new ServletContextHandler();
		contextHandler.setErrorHandler(null);
		contextHandler.setResourceBase(".");
		// contextHandler.setInitParameter("contextConfigLocation",
		// "/WEB-INF/InterAccountSNSPermissionFlow.xml");
		ServletHolder servletHolder = new ServletHolder(new DispatcherServlet(
				context));
		// servletHolder.setName("InterAccountSNSPermissionFlowServlet");
		contextHandler.addServlet(servletHolder, "/*");
		// contextHandler.addEventListener(new ContextLoaderListener(context));

		return contextHandler;
	}

}
