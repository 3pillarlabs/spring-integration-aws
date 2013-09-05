Spring Integration AWS
======================

A collection of channels and channel adapters for receiving and sending messages using Amazon SQS and SNS, built on top of the Spring Integration Framework.

Getting Started
----------------

The project artifacts are available from Maven Central. The dependency information is as follows:

    <dependency>
        <groupId>com.3pillarglobal.labs</groupId>
        <artifactId>spring-integration-aws</artifactId>
        <version>1.1.0-SNAPSHOT</version>
    </dependency>

Demo
----

Please [visit our demo app](http://labs.3pillarglobal.com/spring-integration-aws-demo/) to see the adapters in action.

SQS Channel and Adapters
-----------------------

Amazon SQS (Simple Queue Service) is a poll-based, highly available distributed queue with point to point delivery.

### Features

* Supports both event driven and polling messaging model 
* Works with POJO message payloads (auto-serialization)
* Handles messages from a subscribed SNS topic
* Auto-acknowledge SQS messages
* Transfers Spring Integration `Message` headers to SQS message headers and vice-versa
* Auto create queues if not available

### XML namespace
   
**namespace**: `http://www.springframework.org/schema/integration/sqs`

**schemaLocation**: `https://raw.github.com/3pillarlabs/spring-integration-aws/master/src/main/resources/spring/integration/sqs/config/xml/spring-integration-sqs-1.1.xsd`

### Outbound Adapter

An outbound adapter is used send messages to a SQS queue.

Minium XML configuration:

    <int-sqs:outbound-channel-adapter id="outbound-adapter"
		queue-name="image-jobs" 
		channel="incoming"
		aws-credentials-provider="bean-ref" />

1. **id**: id of adapter element
1. **queue-name**: Name of the SQS queue
1. **channel**: channel to subscribe for incoming messages; your application sends `Message` payloads to this channel
1. **aws-credentials-provider**: reference to a Spring bean of type `com.threepillar.labs.si.aws.SimpleAWSCredentialsProvider`

An `poller` sub-element can be added when the _incoming_ channel is a polling channel. Use your IDE to view more options.

### Outbound Gateway

An outbound adapter is used send messages to a SQS queue with a `reply-channel`.

Minimum XML configuration:

    <int-sqs:outbound-gateway id="outbound-gateway"
		queue-name="image-jobs"
		channel="incoming"
		aws-credentials-provider="bean-ref"
		reply-channel="reply-channel" />

Use your IDE to view more options.

### Inbound Adapter

An inbound adapter is used to receive messages from a SQS queue.

Minimum XML configuration:

    <int-sqs:inbound-channel-adapter id="inbound-adapter" 
		queue-name="image-jobs"
		channel="out"
		aws-credentials-provider="bean-ref" />

1. **channel**: the message channel to which retrieved messages will be sent

There are many more attributes to control message visibility and worker shutdown period, use your IDE to learn more.

### Channel

If your application needs to send and receive messages to the same SQS queue, you can use a channel.

Minimum XML configuration:

    <int-sqs:channel id="channel" 
		queue-name="image-jobs"
		aws-credentials-provider="bean-ref" />

There are many more attributes to control the behavior of the channel in how it receives messages, sends messages and component lifecycle in the Spring container.

SNS Channel and Adapters
-------------------------

Amazon SNS (Simple Notification Service) is a publish subcribe messaging system, where you publish your messages to a topic and all subscribers to that topic receive the message. Amazon SNS can send messages to a subcribed URL, a subscribed SQS queue and via email.

### Features

- Supports both outgoing messages and incoming messages (via a transparent HTTP endpint)
- Support for transparent SQS subscriptions and permissions
- Publish subscribe channel for multiple subscribers
- Signature verification of incoming messages to HTTP endpoint
- Auto create SNS topics if they do not exist

### XML Namespace

**namespace** : `http://www.springframework.org/schema/integration/sns`

**schemaLocation**: `https://raw.github.com/3pillarlabs/spring-integration-aws/master/src/main/resources/spring/integration/sns/config/xml/spring-integration-sns-1.1.xsd`

### Outbound Adapter

An outbound adapter is used to send messages to a SNS topic.

Minimum XML configuration:

    <int-sns:outbound-channel-adapter id="outbound-adapter" 
		topic-name="top-picks"
		aws-credentials-provider="bean-ref"
		channel="incoming" />

1. **topic-name**: Name of the SNS topic to post messages

### Outbound Gateway

This is same as the outbound adapter, you need to add the `reply-channel` attribute.

### Inbound Adapter

An inbound adapter can be used to receive messages from a SNS topic. Behind the scenes, a HTTP endpoint is registered for an inbound adapter; this implies that the inbound adapter can only be used in a web context.

Minimum XML configuration

    <int-sns:inbound-channel-adapter id="inbound-adapter" 
			topic-name="top-picks"
			aws-credentials-provider="bean-ref"
			channel="out">
			
		<int-sns:endpoint base-uri="http://www.example.com/context"/>
	</int-sns:inbound-channel-adapter>

1. **endpoint**: specifies the base URL of the web application. This will be used to subscribe a HTTP endpoint to the SNS topic

### Channel

If your application sends and receives messages from the same SNS topic, you should use a channel. Incoming messages are received over a HTTP endpoint, so this only works in a web context.

Minimum XML configuration

    
	<int-sns:publish-subscribe-channel id="channel" 
			topic-name="top-picks" 
			aws-credentials-provider="bean-ref">
			
		<int-sns:endpoint base-uri="http://www.example.com/context"/>
	</int-sns:publish-subscribe-channel>

1. The `publish-subscribe-channel` broadcasts incoming messages to all subscribers.

### SNS Subscriptions

It is possible to add SNS subscriptions to the SNS adapters. Information on GitHub wiki to be added soon.


### Amazon client configuration

If you wand to overwrite the default connection/transport settings of the Amazon SNS/SQS clients used internally you may specify an additional ClientConfiguration bean:

  <bean id="awsClientConfiguration" class="com.amazonaws.ClientConfiguration">
    <property name="proxyHost" value="some proxy host"/>
    <property name="protocol" value="some proxy port"/>
  </bean>
  
this bean can be injected in the respective channel-adapter, channel or gateway beans by setting the property, e.g.:
   
   <int-sns:publish-subscribe-channel id="snsChannel" topic-name="topic" aws-client-configuration="awsClientConfiguration">
     <int-sns:endpoint base-uri="http://www.example.com"/>
   </int-sns:publish-subscribe-channel>

Reporting Issues
-----------------

Please report any issues you see in the application to GitHib and we will get back to you.
