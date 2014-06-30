package com.threepillar.labs.snssample.controller;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.aws.sns.SnsExecutorProxy;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;

@Controller
public class UserMessageController implements InitializingBean, DisposableBean {

	private final Log log = LogFactory.getLog(getClass());

	private AWSCredentialsProvider awsCredentialsProvider;
	private SnsExecutorProxy snsExecutorProxy;
	private AmazonSNS sns;

	@Autowired
	public void setAwsCredentialsProvider(
			AWSCredentialsProvider awsCredentialsProvider) {
		this.awsCredentialsProvider = awsCredentialsProvider;
	}

	@Autowired
	public void setSnsExecutorProxy(SnsExecutorProxy snsExecutorProxy) {
		this.snsExecutorProxy = snsExecutorProxy;
	}

	@RequestMapping(value = "/snsInboundTopic", method = RequestMethod.POST)
	public void postToSnsInboundTopic(@RequestBody String message,
			HttpServletResponse response) {

		log.debug("message to SNS: snsInboundTopic, " + message);
		sns.publish(new PublishRequest(snsExecutorProxy.getTopicArn(), message));
		response.setStatus(HttpServletResponse.SC_ACCEPTED);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		sns = new AmazonSNSClient(awsCredentialsProvider);
	}

	@Override
	public void destroy() throws Exception {
		sns.shutdown();
	}

}
