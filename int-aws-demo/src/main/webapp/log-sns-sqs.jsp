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
      SQS queue. This message is picked up by the SQS inbound handler and displayed
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
    	&lt;int-sns:sqs queue-id="sqsInbound"/&gt;
    &lt;/int-sns:subscriptions&gt;  
  &lt;/int-sns:outbound-channel-adapter&gt;

  &lt;int-sqs:inbound-channel-adapter id="sqsInbound" 
      queue-name="sqsInboundQueue" 
      aws-credentials-provider="awsCredentialsProvider"
      channel="logSqsInbound" /&gt;
      
  &lt;int:publish-subscribe-channel id="logSqsInbound" /&gt;
  
  &lt;bean id="snsSqsMessageHandler" 
      class="com.threepillar.labs.snssample.websocket.SnsSqsMessageHandler"
      scope="prototype" /&gt;
      </code></pre>
    </section>
    
    <section class="one-half">
      <label for="clientMessage" class="text-info"><strong>Your Message:</strong></label>
      <textarea rows="2" cols="20" id="clientMessage" autofocus="autofocus"></textarea>
      <button id="postToSNS" class="btn">Post to SNS</button>
      <br/>
      <label for="serverMessage">SQS Inbound Message:</label>
      <textarea rows="15" cols="20" id="serverMessage" readonly="true"></textarea>
      <div class="row">
        <div class="span3">
          <div class="progress progress-success">
            <div id="progressBar" class="bar" style="width: 1%"></div>
          </div>
          <br class="clear" />
        </div>
      </div>
    </section>

    <br class="clear" />
    <script type="text/javascript">
      jQuery(function($) {
    	  if ("WebSocket" in window) {
        var serverName = "<%= pageContext.getServletContext().getAttribute("websocket.host") %>";
        var serverPort = "<%= pageContext.getServletContext().getAttribute("websocket.port") %>";
        var wsURL = "ws://" + serverName + ":" + serverPort + "/snsSqsMessageHandler";
        var webSocket = new WebSocket(wsURL);
        webSocket.onmessage = function(event) {
          var messageText = event.data;
          $("#serverMessage").append(messageText + "\n");
          $("#progressBar").css("width", "100%");
          window.setTimeout(function() {
        	  $("#progressBar").css("width", "1%");
          }, 999);
        };
        webSocket.onclose = function() {
      	  $("#postToSNS").attr("disabled", true);
      	  $("label[for=clientMessage]").prepend('<div class="alert"><strong>Connection Lost</strong>: please refresh page</div>');
        };
        
        $("#postToSNS").click(function() {
          var inputArea = $("#clientMessage");
          var message = inputArea.attr("value");
          if (message.length > 0) {
            webSocket.send(message);
            inputArea.attr("value", "");
            $("#progressBar").css("width", "50%");
          }
        });
    	  } else {
    		  $("#postToSNS").attr("disabled", true);
    	  }
      });
      
    </script>
  </div>
</div>
<%@include file="WEB-INF/partials/afterContent.jsp" %>
