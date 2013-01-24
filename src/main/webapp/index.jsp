<%@include file="WEB-INF/partials/beforeContent.jsp" %>
<div class="content">
  <div class="post-160 page type-page status-publish hentry" id="post-160">
    <h1 class="page-title entry-title">Spring Integration AWS</h1>                            
  </div>          
  <div class="portfolio-large" id="pc-portfolio">
    <div class="one-half">
      <img class="new-ribbon new-ribbon-173 featured-image featured-image-173 pf-post-image" src="http://labs.3pillarglobal.com/wp-content/uploads/2012/12/bolt-featured.png">
      <div id="webSocketAlert" class="alert alert-error alert-block hide">
        <h4>No WebSocket!</h4>
        Looks like the WebSocket API is not available in your browser. Please use a compatible browser
        like <strong>Safari</strong>, <strong>Firefox</strong> or <strong>Chrome</strong> to view these demos. 
      </div>                
    </div>
    <div class="one-half">
      <p class="lead">
      Demonstrating the capabilities of our Spring Integration AWS project... 
      </p>
      <ul>
        <li><i class="icon-flag"></i> <a href="log-sns-inbound.jsp"><strong>SNS Inbound</strong></a>: receive notifications from a SNS topic</li>
        <li><i class="icon-flag"></i> <a href="log-sns-channel.jsp"><strong>SNS Channel</strong></a>: receive notifications from a SNS topic and relay to multiple subscribers</li>
        <li><i class="icon-flag"></i> <a href="log-sns-sqs.jsp"><strong>SNS-SQS Inbound</strong></a>: push notifications from SNS topic to SQS queue and receive queued messages</li>
      </ul>
    </div>
    <br class="clear"/>
  </div>
</div>
<script>
	jQuery(function($) {
		if (!("WebSocket" in window)) {
			$("#webSocketAlert").removeClass("hide");
		}
	});
</script>
<%@include file="WEB-INF/partials/afterContent.jsp" %>
