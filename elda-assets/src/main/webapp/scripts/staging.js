// If the document isn't at a location matching urlPattern then rewrite all a href links 
// which do match urlPattern to point to the same server as this document
// Assumes jQuery
$(function() {
	var urlPattern = /^http:\/\/(environment|location)\.data\.gov\.uk/;
    var hostPattern = /(^https?:\/\/[^\/]*)/
	var url = document.URL;
	var apiBaseOffset = url.indexOf("/api");
	var host = hostPattern.exec(url)[1];
	var apiBase = apiBaseOffset>0 ?	apiBase = url.slice(0,apiBaseOffset+4) : host;
		
	$("a[href]^=http://").not($("a[href^="+host+"]")).each( function(a) {
	//Rewrite any <a href=.../> that don't refer back to this page
	   if(this.href.indexOf(host)!=0) {
		  var replacement = /.*[?&=#].*/.test(this.href) ? encodeURIComponent(this.href) : encodeURI(this.href); 
		  this.href = this.href.replace(this.href,apiBase+"/thing?resource="+replacement);
	  }
	});
});