<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>SNS-SQS Log</title>

<script type='text/javascript'
  src='http://labs.3pillarglobal.com/wp-includes/js/jquery/jquery.js?ver=1.8.3'></script>

</head>
<body>

<textarea rows="20" cols="50" id="clientMessage"></textarea>
<button id="postToSNS">Post to SNS</button>

<textarea rows="20" cols="50" id="serverMessage" readonly="true"></textarea>

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
</body>
</html>