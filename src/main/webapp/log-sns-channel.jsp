<%@include file="WEB-INF/partials/beforeContent.jsp" %>
<div class="content">
  <div class="post-160 page type-page status-publish hentry" id="post-160">
    <h1 class="page-title entry-title">SNS Channel</h1>                            
  </div>          
  <div class="portfolio-large" id="pc-portfolio">
    
    <section class="one-half">
      <blockquote>
      This example demonstrates how a SNS channel can be setup to relay messages
      in a publish-subscribe manner. Here, the SNS topic has 2 subscribers, when you
      post a message to SNS, the channel sends the message to both the subscribers 
      and the subscribers display the message.  
      </blockquote>
      <pre><code>
&lt;!-- SNS channel  --&gt;
&lt;int-sns:publish-subscribe-channel id="snsChannel" 
    topic-name="snsChannelTopic"
    aws-credentials-provider="awsCredentialsProvider"
    http-endpoint-path="/aws/sns/channel.do"&gt;

  &lt;int-sns:subscriptions&gt;
    &lt;int-sns:subscription protocol="http" endpoint="http://203.122.33.232:80/si-aws-sns-sample"/&gt;
  &lt;/int-sns:subscriptions&gt;    
&lt;/int-sns:publish-subscribe-channel&gt;

&lt;bean id="snsChannelSubscriberA" 
    class="com.threepillar.labs.snssample.websocket.SnsChannelMessageHandler" /&gt;
&lt;bean id="snsChannelSubscriberB" 
    class="com.threepillar.labs.snssample.websocket.SnsChannelMessageHandler" /&gt;
      </code></pre>
    </section>

    <section class="one-half">
      <label for="clientMessage" class="text-info"><strong>Your Message:</strong></label>
      <textarea rows="2" cols="20" id="clientMessage" autofocus="autofocus"></textarea>
      <button id="postToSNS" class="btn">Post to SNS</button>
      <br/>
      <label for="serverMessageA">Subscriber A:</label>
      <textarea rows="2" cols="20" id="serverMessageA" readonly="true"></textarea>
      <label for="serverMessageB">Subscriber B:</label>
      <textarea rows="2" cols="20" id="serverMessageB" readonly="true"></textarea>
      <div class="row">
        <div class="span3">
          <div class="progress progress-success">
            <div id="progressBar" class="bar" style="width: 1%"></div>
          </div>
          <br class="clear" />
        </div>
      </div>
    </section>
    <br class="clear"/>
    <script type="text/javascript">
    	jQuery(function($) {
            var serverName = "<%= pageContext.getServletContext().getAttribute("websocket.host") %>";
            var serverPort = "<%= pageContext.getServletContext().getAttribute("websocket.port") %>";
            var wsURLA = "ws://" + serverName + ":" + serverPort + "/snsChannelSubscriberA";
            var wsURLB = "ws://" + serverName + ":" + serverPort + "/snsChannelSubscriberB";
    		var webSocketA = new WebSocket(wsURLA);
    		var webSocketB = new WebSocket(wsURLB);
    		webSocketA.onmessage = function(event) {
    			var messageText = event.data;
    			$("#serverMessageA").append(messageText + "\n");
                $("#progressBar").css("width", "100%");
                window.setTimeout(function() {
                  $("#progressBar").css("width", "1%");
                }, 999);
    		};
    		webSocketB.onmessage = function(event) {
    			var messageText = event.data;
    			$("#serverMessageB").append(messageText + "\n");
    		};
    		
    		$("#postToSNS").click(function() {
    			var inputArea = $("#clientMessage");
    			var message = inputArea.attr("value");
    			if (message.length > 0) {
    				webSocketA.send(message);
    				inputArea.attr("value", "");
    				$("#progressBar").css("width", "50%");
    			}
    		});
    	});
    	
    </script>
  </div>
</div>
<%@include file="WEB-INF/partials/afterContent.jsp" %>
