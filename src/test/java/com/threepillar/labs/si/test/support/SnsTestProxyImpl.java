package com.threepillar.labs.si.test.support;

import java.util.concurrent.BlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.bind.annotation.RequestMethod;

import com.threepillar.labs.si.sns.core.HttpEndpoint;
import com.threepillar.labs.si.sns.support.SnsTestProxy;

public class SnsTestProxyImpl implements SnsTestProxy {

	private final Log log = LogFactory.getLog(SnsTestProxyImpl.class);

	private BlockingQueue<String> queue;
	private HttpEndpoint httpEndpoint;

	@Override
	public void setQueue(BlockingQueue<String> queue) {
		this.queue = queue;
	}

	@Override
	public void setHttpEndpoint(HttpEndpoint httpEndpoint) {
		this.httpEndpoint = httpEndpoint;
	}

	@Override
	public void dispatchMessage(String jsonPayload) {
		if (queue != null) {
			queue.add(jsonPayload);
		}
		if (httpEndpoint != null) {
			MockHttpServletRequest request = new MockHttpServletRequest();
			request.addHeader("x-amz-sns-message-type", "Notification");
			request.setMethod(RequestMethod.POST.name());
			request.setContent(String.format("{\"Message\" : %s}", jsonPayload)
					.getBytes());
			try {
				httpEndpoint.handleRequest(request,
						new MockHttpServletResponse());
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}
}
