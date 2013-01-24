<%@include file="WEB-INF/partials/beforeContent.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<div class="content">
  <div class="post-160 page type-page status-publish hentry" id="post-160">
    <h1 class="page-title entry-title">SNS Inbound</h1>                            
  </div>          
  <div class="portfolio-large" id="pc-portfolio">
  <c:url value="/snsInboundTopic.do" var="snsInboundTopicPath"/>
  <section class="one-half">
    <blockquote>
      This example demonstrates how an SNS inbound adapter can be set up to
      receive messages from a SNS topic. The message you post is directly sent to
      SNS and in a short while, the adapter picks the message and displays it in
      the read-only text area.
    </blockquote>
    <pre><code lang="xml">
&lt;!-- SNS Inbound --&gt;
&lt;int-sns:inbound-channel-adapter id="snsInboundAdapter"
    topic-name="snsInboundTopic"
    channel="logSnsInbound" 
    http-endpoint-path="/aws/sns/inbound.do"
    aws-credentials-provider="awsCredentialsProvider" 
    sns-executor-proxy="snsInboundAdapterProxy"&gt;
    
  &lt;int-sns:subscriptions&gt;
    &lt;int-sns:subscription protocol="http" endpoint="http://203.122.33.232:80/si-aws-sns-sample"/&gt;
  &lt;/int-sns:subscriptions&gt;
&lt;/int-sns:inbound-channel-adapter&gt;

&lt;int:channel id="logSnsInbound" /&gt;

&lt;bean id="snsInboundMessageHandler" 
    class="com.threepillar.labs.snssample.websocket.SnsInboundMessageHandler" /&gt;
    </code></pre>
  </section>
  <section class="one-half">
    <label for="clientMessage" class="text-info"><strong>Your Message:</strong></label>
    <textarea rows="2" cols="20" id="clientMessage" autofocus="autofocus"></textarea>
    <button id="postToSNS" class="btn btn-default">Post to SNS</button>
    <br/>
    <label for="serverMessage">SNS Inbound Message:</label>
    <textarea rows="2" cols="20" id="serverMessage" readonly="true"></textarea>
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
   	  var serverName = "<%= pageContext.getServletContext().getAttribute("websocket.host") %>";
   	  var serverPort = "<%= pageContext.getServletContext().getAttribute("websocket.port") %>";
   	  var wsURL = "ws://" + serverName + ":" + serverPort + "/snsInboundMessageHandler";
      var webSocket = new WebSocket(wsURL);
      webSocket.onmessage = function(event) {
        var messageText = event.data;
        $("#serverMessage").append(messageText + "\n");
        $("#progressBar").css("width", "100%");
        window.setTimeout(function() {
          $("#progressBar").css("width", "1%");
        }, 999);
      };
      
      $("#postToSNS").click(function() {
        var inputArea = $("#clientMessage");
        var message = inputArea.attr("value");
        if (message.length > 0) {
        	$("#progressBar").css("width", "25%");
          $.ajax("${snsInboundTopicPath}", {
            type: "POST",
            contentType: "text/plain",
            data: message,
            processData: false,
            dataType: "text",
            success: function() {
              inputArea.attr("value", "");
              $("#progressBar").css("width", "50%");
            }
          });
        }
      });
    });
  </script>
  </div>
</div>
<%@include file="WEB-INF/partials/afterContent.jsp" %>
  