var Elda = function() {
  var init = function() {
    $('.popover-dismiss').popover({
      trigger: 'click'
    });

    $('.popover-dismiss').click(function(){
        $('.popover-dismiss').not( this ).popover( 'hide' );
    });
  };

  return {
    init: init
  }
}();

$( Elda.init );
