// rewrite references to the real education URIs to our local
// version.
// Assumes jQuery

$(function() {
	apiStart = document.URL.indexOf( "/api" )
	if (apiStart < 0) return

	editFrom = "http://education.data.gov.uk/"
	editTo = document.URL.slice(0, apiStart + 4) + "/" 

	$("a[href^=" + editFrom + "]").each( function( a ) {
		// this.href = this.href.replace( editFrom, editTo )
	})
})
