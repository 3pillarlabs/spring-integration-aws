<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>SNS Channel Log</title>

<script type='text/javascript'
  src='http://labs.3pillarglobal.com/wp-includes/js/jquery/jquery.js?ver=1.8.3'></script>

</head>
<body>

<textarea rows="20" cols="50" id="clientMessage"></textarea>
<button id="postToSNS">Post to SNS</button>

<textarea rows="20" cols="50" id="serverMessageA" readonly="true"></textarea>
<textarea rows="20" cols="50" id="serverMessageB" readonly="true"></textarea>

<script type="text/javascript">
	jQuery(function($) {
		var webSocketA = new WebSocket("ws://localhost:9090/snsChannelSubscriberA");
		var webSocketB = new WebSocket("ws://localhost:9090/snsChannelSubscriberB");
		webSocketA.onmessage = function(event) {
			var messageText = event.data;
			$("#serverMessageA").append(messageText + "\n");
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
			}
		});
	});
	
</script>
</body>
</html>