<%@include file="WEB-INF/partials/beforeContent.jsp" %>
<div class="content">
  <div class="post-160 page type-page status-publish hentry" id="post-160">
    <h1 class="page-title entry-title">SNS-SQS Inbound</h1>                            
  </div>          
  <div class="portfolio-large" id="pc-portfolio">
    <section class="one-half">
      <blockquote>
      This example demonstrates SNS-SQS communication. The SQS queue is setup as
      a SNS topic subscriber with appropriate permissions for the SNS topic to post
      to the queue. When you send your message to the SNS topic, it is relayed to the
      SQS queue. This message is picked up by the SQS inbound handler is displayed
      in the text area.  
      </blockquote>
      <pre><code>
&lt;!-- SNS outbound -&gt; SQS  --&gt;
&lt;int:channel id="logSnsOutbound" /&gt;

&lt;int-sns:outbound-channel-adapter id="snsOutbound" 
    topic-name="snsOutboundTopic" 
    aws-credentials-provider="awsCredentialsProvider"
    channel="logSnsOutbound"&gt;
    
  &lt;int-sns:subscriptions&gt;
    &lt;int-sns:subscription protocol="sqs" endpoint="sqsInbound"/&gt;
  &lt;/int-sns:subscriptions&gt;  
&lt;/int-sns:outbound-channel-adapter&gt;

&lt;int-sqs:inbound-channel-adapter id="sqsInbound" 
    queue-name="sqsInboundQueue" 
    aws-credentials-provider="awsCredentialsProvider"
    channel="logSqsInbound" /&gt;
    
&lt;int:channel id="logSqsInbound" /&gt;

&lt;bean id="snsSqsMessageHandler" 
    class="com.threepillar.labs.snssample.websocket.SnsSqsMessageHandler" /&gt;
      </code></pre>
    </section>
    
    <section class="one-half">
      <label for="clientMessage" class="text-info"><strong>Your Message:</strong></label>
      <textarea rows="2" cols="20" id="clientMessage" autofocus="autofocus"></textarea>
      <button id="postToSNS" class="btn">Post to SNS</button>
      <br/>
      <label for="serverMessage">SQS Inbound Message:</label>
      <textarea rows="2" cols="20" id="serverMessage" readonly="true"></textarea>
    </section>

    <br class="clear" />
    <script type="text/javascript">
      jQuery(function($) {
        var webSocket = new WebSocket("ws://localhost:9090/snsSqsMessageHandler");
        webSocket.onmessage = function(event) {
          var messageText = event.data;
          $("#serverMessage").append(messageText + "\n");
        };
        
        $("#postToSNS").click(function() {
          var inputArea = $("#clientMessage");
          var message = inputArea.attr("value");
          if (message.length > 0) {
                    webSocket.send(message);
                    inputArea.attr("value", "");
          }
        });
      });
      
    </script>
  </div>
</div>
<%@include file="WEB-INF/partials/afterContent.jsp" %>
