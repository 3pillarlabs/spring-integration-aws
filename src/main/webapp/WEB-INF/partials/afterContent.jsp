      </div>
      <!-- #contentwrap -->
    </div>
    <!-- #container -->
    <br class="clear"/>
    <footer class="hide">
      <!-- .footer-widget-container -->
      <div id="site-info">
        <p class="copyright">
          &copy; 2013 <a href="http://labs.3pillarglobal.com/"
            title="3Pillar Labs" rel="home">3Pillar Labs</a>
        </p>
        <br class="clearfix"/>
      </div>
    </footer>
  </div>
  <!-- #body-container -->
  <script>
  	jQuery(function($) {
  		var displayHeight = (window.innerHeight > window.document.body.scrollHeight ? window.innerHeight : window.document.body.scrollHeight); 
  		$("footer").css("top", displayHeight * 1 - 30);
  		$("footer").removeClass("hide");
  	});
  </script>
</body>
</html>
