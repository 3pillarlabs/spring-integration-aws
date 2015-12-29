<?xml version="1.0" encoding="UTF-8" ?>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!doctype html>
<!--[if lt IE 7]> <html class="no-js ie6" lang="en"> <![endif]-->
<!--[if IE 7]>    <html class="no-js ie7" lang="en"> <![endif]-->
<!--[if IE 8]>    <html class="no-js ie8" lang="en"> <![endif]-->
<!--[if gt IE 8]><!-->
<html class="no-js" lang="en-US">
<!--<![endif]-->
<head>
<meta charset="utf-8" />

<!-- Always force latest IE rendering engine (even in intranet) & Chrome Frame -->
<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" />

<title>Spring Integration AWS Demos | 3Pillar Labs</title>

<meta name="viewport" content="width=device-width">

<link rel="stylesheet" href="css/style.css" type="text/css" media="screen" />
<link rel='stylesheet' id='fancybox-stylesheet-css'
  href='http://atg.3pillarglobal.com/wp-content/themes/designfolio-pro/api/js/lightboxes/fancybox-1.3.4/jquery.fancybox-1.3.4.css?ver=3.5'
  type='text/css' media='all' />
<link rel='stylesheet' id='color-scheme-stylesheet-css' href='css/brown.css?ver=3.5' type='text/css' media='all' />
<link rel='stylesheet' id='Droid+Sans:400,700-css'
  href='http://fonts.googleapis.com/css?family=Droid+Sans%3A400%2C700&#038;ver=3.5'
  type='text/css' media='all' />
<link href="css/bootstrap.min.css" rel="stylesheet" media="screen">
<script type='text/javascript'
  src='http://atg.3pillarglobal.com/wp-includes/js/comment-reply.min.js?ver=3.5'></script>
<script type='text/javascript'
  src='http://atg.3pillarglobal.com/wp-includes/js/jquery/jquery.js?ver=1.8.3'></script>
<script type='text/javascript'
  src='http://atg.3pillarglobal.com/wp-content/themes/designfolio-pro/api/js/html5/modernizr/modernizr.custom.97935.js?ver=3.5'></script>
<script type='text/javascript'
  src='http://atg.3pillarglobal.com/wp-content/themes/designfolio-pro/api/js/html5/modernizr/pc_modernizr_custom.js?ver=3.5'></script>
<script type='text/javascript'
  src='http://atg.3pillarglobal.com/wp-content/themes/designfolio-pro/api/js/misc/superfish-1.4.8/js/superfish.js?ver=3.5'></script>
<script type='text/javascript'
  src='http://atg.3pillarglobal.com/wp-content/themes/designfolio-pro/includes/js/pc_superfish_init.js?ver=3.5'></script>
<script type='text/javascript'
  src='http://atg.3pillarglobal.com/wp-content/themes/designfolio-pro/api/js/lightboxes/fancybox-1.3.4/jquery.fancybox-1.3.4.pack.js?ver=3.5'></script>
<script type='text/javascript'
  src='http://atg.3pillarglobal.com/wp-content/themes/designfolio-pro/api/js/presscoders/custom-fancybox.js?ver=3.5'></script>
<script type='text/javascript'
  src='http://atg.3pillarglobal.com/wp-content/themes/designfolio-pro/api/js/lightboxes/fancybox-1.3.4/jquery.easing-1.3.pack.js?ver=3.5'></script>
<script type='text/javascript'
  src='http://atg.3pillarglobal.com/wp-content/themes/designfolio-pro/api/js/lightboxes/fancybox-1.3.4/jquery.mousewheel-3.0.4.pack.js?ver=3.5'></script>
<script type='text/javascript'
  src='http://atg.3pillarglobal.com/wp-content/themes/designfolio-pro/api/js/sliders/flexslider/jquery.flexslider2.1beta.js?ver=3.5'></script>
<script type='text/javascript'
  src='http://atg.3pillarglobal.com/wp-content/themes/designfolio-pro/api/js/misc/jquery.quicksand.js?ver=3.5'></script>
<script type='text/javascript'
  src='http://atg.3pillarglobal.com/wp-content/themes/designfolio-pro/api/js/presscoders/custom-quicksand.js?ver=3.5'></script>
<link rel='prev' title='Frameworks'
  href='http://atg.3pillarglobal.com/frameworks/' />
<script src="js/bootstrap.min.js"></script>
<link rel='canonical' href='http://atg.3pillarglobal.com/apps/' />
<link rel="shortcut icon"
  href="http://atg.3pillarglobal.com/wp-content/uploads/2012/12/favicon.ico" />
<style type="text/css" id="custom-background-css">
body.custom-background {
	background-color: #ffffff;
}
</style>
<!--[if IE 8]>
<style type="text/css">
.comment-body, li.pingback, .quote, .avatar, .defaultbtn, .button, .btn, #searchsubmit, #submit, .submit, .post-edit-link, .more-link, input[type="submit"], input[type="text"], textarea, ol.flex-control-nav li a, ol.flex-control-nav li a.active, .flex-direction-nav li a, .post-date, nav.secondary-menu, nav ul ul {
behavior: url(http://atg.3pillarglobal.com/wp-content/themes/designfolio-pro/includes/js/PIE.htc);
}
</style>
<![endif]-->
<!-- Designfolio Pro user defined custom CSS -->
<style type="text/css">
.post-content {
	padding-top: 20px;
}
h1,h2,h3,h4 {
	font-family: 'Droid Sans', serif;
}
</style>

</head>

<body
  class="page page-id-163 page-template page-template-portfolio-page-php custom-background designfolio-pro">

  <div id="body-container">


    <div id="header-container">
      <header class="cf">


        <div id="logo-wrap">
          <div id="site-logo">
            <a href="http://labs.3pillarglobal.com"><img
              src="http://atg.3pillarglobal.com/wp-content/uploads/2012/12/tpg-labs-logo.jpg" /></a>
          </div>

        </div>
        <!-- #logo-wrap -->

        <div id="header-widget-area" class="widget-area"
          role="complementary">
          <div id="pc_info_widget_designfolio-pro-2"
            class="widget pc_info_widget">
            <a href="http://www.facebook.com/3PillarGlobal"
              target="_blank" class="sm-icon"><img
              src="http://atg.3pillarglobal.com/wp-content/themes/designfolio-pro/images/facebook.png"
              width="32" height="32" alt="Facebook" /></a><a
              href="http://twitter.com/3PillarGlobal" target="_blank"
              class="sm-icon"><img
              src="http://atg.3pillarglobal.com/wp-content/themes/designfolio-pro/images/twitter.png"
              width="32" height="32" alt="Twitter" /></a>
          </div>
        </div>
        <!-- #header-widget-area -->

        <nav class="primary-menu cf">
          <div class="menu">
            <ul>
              <li id="home_page"><a href="index.jsp"
                title="Home">Home</a></li>
              <li id="sns-inbound"><a
                href="log-sns-inbound.jsp">SNS Inbound</a></li>
              <li id="sns-channel"><a
                href="log-sns-channel.jsp">SNS Channel</a></li>
              <li id="sns-sqs"><a
                href="log-sns-sqs.jsp">SNS-SQS Inbound</a></li>
            </ul>
          </div>
          <script>
          	jQuery(function($) {
          		var url = window.location.href;
          		var rexp = new RegExp("\/log\-(.+?)\.jsp$");
          		var match = rexp.exec(url);
          		if (match) {
          			$("li#" + match[1]).attr("class", "current_page_item");
          		} else {
          			$("li#home_page").attr("class", "current_page_item");
          		}
          	});
          </script>
        </nav>

      </header>

    </div>
    <!-- #header-container -->
    <div id="container" class="singular-page">
      <div id="contentwrap" class="one-col">
        <div id="webSocketAlert" class="alert alert-error alert-block hide">
          <h4>No WebSocket!</h4>
          Looks like the WebSocket API is not available in your browser. Please use a compatible browser
          like <strong>Safari</strong>, <strong>Firefox</strong> or <strong>Chrome</strong> to view these demos.
        </div>
        <script>
          jQuery(function($) {
            if (!("WebSocket" in window)) {
              $("#webSocketAlert").removeClass("hide");
            }
          });
        </script>
