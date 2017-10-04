/*! jQuery Spin - v0.0.1 - 2013-05-14
* https://github.com/tdoherty/jQuery.SpinJS
* Copyright (c) 2013 tdoherty; Licensed MIT */
//AMD support - works with or without AMD
(function (factory) {
  if (typeof define === 'function' && define.amd) {
      // AMD. Register as an anonymous module. Aliases for jQuery.js and Spin.js
      define(['jquery', 'spin'], factory);
  } else {
      // Browser globals
      factory(jQuery, Spinner);
  }
}(function($, Spinner) {

  $.fn.spin = function (opts) {
    //spin.js opts
    var defaults = {
      lines: 12, // The number of lines to draw
      length: 7, // The length of each line
      width: 5, // The line thickness
      radius: 10, // The radius of the inner circle
      color: '#fff', // #rbg or #rrggbb
      speed: 1, // Rounds per second
      trail: 100, // Afterglow percentage
      shadow: true, // Whether to render a shadow

      //plugin-specific
      bgColor: 'Gray', //loading background color
      opacity: 4 //loading background opacity
    };

    this.each(function () {
      var $this = $(this);
      var $data = $this.data();

      if ($data.spinner) {
        $data.spinner.stop();
        delete $data.spinner;
        $this.find('.loadingBG').remove();
      }

      if (opts === false) { return; }

      opts = opts || {};
      opts = $.extend({}, defaults, opts);

      var $bgEl = $('<div>');
      $bgEl.addClass('loadingBG');
      $bgEl.css({
        'display': 'none',
        'filter': 'alpha(opacity=' + opts.opacity * 10 + ')',
        'opacity': opts.opacity / 10,
        '-ms-filter': 'progid:DXImageTransform.Microsoft.Alpha(opacity=' + opts.opacity * 10 + ')', /*IE8*/
        'position': 'absolute',
        'z-index': 9999,
        'top': $this.css('position') === 'absolute' ? 0 : $this.position().top - 1,
        'left': $this.css('position') === 'absolute' ? 0 : $this.position().left,
        'background-color': opts.bgColor.toString(),
        'width': $this.outerWidth(),
        'height': $this.outerHeight() === 0 ? '100%' : $this.outerHeight() + 1,
        'marginTop': $this.css('marginTop'),
        'marginRight': $this.css('marginRight'),
        'marginBottom': $this.css('marginBottom'),
        'marginLeft': $this.css('marginLeft')
      });

      delete opts.bgColor;
      delete opts.opacity;

      //call spin.js
      $data.spinner = new Spinner(opts).spin(this);

      $bgEl.prependTo($this).show();
    });
    return this;
  };

}));
