/** Some common event handling */

define( ['jquery'], function( $ ) {
  "use strict";

  var setBodyPaddingForNav = function() {
    $('body').css( 'padding-top', $( 'body>nav' ).outerHeight() + 5 );
  };

  /** Set the body top padding to the correct size for the nav bar when the window changes */
  $(window).resize( setBodyPaddingForNav );

  /** Set the initial size for the nav bar */
  $( setBodyPaddingForNav );
} );