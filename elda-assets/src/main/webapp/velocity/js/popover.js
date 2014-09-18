/** Manage popovers */
define( ['jquery'], function( $ ) {
  var onPageReady = function() {
    $('.popover-dismiss').popover({
      trigger: 'click'
    });

    $('.popover-dismiss').click(function(){
        $('.popover-dismiss').not( this ).popover( 'hide' );
    });

    $('body').on('click', function (e) {
      var t = $(e.target);
      if (!(t.is('.popover-dismiss') || t.parents('.popover-dismiss').length !== 0 || t.parents('.popover.in').length !== 0)) {
        console.log( "dismissing popover" );
        $('.popover-dismiss').popover('hide');
      }
    });
  };

  $( onPageReady );
} );
