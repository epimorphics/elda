/* Copyright (c) 2012-2015 Epimorphics Ltd. Released under Apache License 2.0 http://www.apache.org/licenses/ */

/* global define */

define( [
  'lodash',
  'jquery',
  'sprintf',
  'app/remote-sparql-service',
  'codemirror/lib/codemirror',
  'codemirror/mode/sparql/sparql',
  'codemirror/mode/xml/xml',
  'codemirror/mode/javascript/javascript',
  'jquery.spinjs',
  'datatables'
],
function (
  _,
  $,
  sprintf,
  RemoteSparqlService,
  CodeMirror
) {
  'use strict';

  /* --- module vars --- */
  /** The loaded configuration */
  var _config = {};
  var _queryEditor = null;
  var _startTime = 0;
  var _outstandingQueries = 0;

  /* --- application code --- */

  /** Initialisation - only called once
   * @param {object} qonfig The Qonsole configuration object
   */
  var init = function ( qonfig ) {
    loadConfig( qonfig );
    bindEvents();

    $.ajaxSetup( {
      converters: {'script json': true}
    } );
  };

  /**
   * Load the given configuration URL if given
   * @param  {object} qonfig Qonsole configuratoin
   */
  var loadConfig = function ( qonfig ) {
    if (qonfig.configURL) {
      $.getJSON( qonfig.configURL, onConfigLoaded );
    } else {
      onConfigLoaded( qonfig );
    }
  };

  /**
   * Return the current config object
   * @return {object} The current configuration
   */
  var config = function () {
    _config.parsedPrefixes = parseQueryPrefixes();
    return _config;
  };

  /** Bind events that we want to manage */
  var bindEvents = function () {
    $('ul.prefixes').on( 'change', 'input', function ( e ) {
      var elem = $(e.currentTarget);
      updatePrefixDeclaration( elem.data('prefix'), elem.val(), elem.is(':checked') );
    } );
    $('#examples').on( 'change', function ( e ) {
      var query = $(e.currentTarget).val();
      showCurrentExample( query );
    } );
    $('#endpoints').on( 'change', function ( e ) {
      var elem = $(e.currentTarget);
      setCurrentEndpoint( $.trim( elem.val() ) );
    } );

    $('.run-query').on( 'click', runQuery );

    $(document)
      .ajaxStart(function () {
        startTimingResults();
        disableSubmit( true );
        spinStart();
      })
      .ajaxStop(function () {
        disableSubmit( false );
        spinStop();
      });

    // dialogue events
    $('#prefixEditor').on( 'click', '#lookupPrefix', onLookupPrefix )
                      .on( 'keyup', '#inputPrefix', function ( e ) {
                        var elem = $(e.currentTarget);
                        $('#lookupPrefix span').text( sprintf.sprintf( "'%s'", elem.val() ));
                      } );
    $('#addPrefix').on( 'click', onAddPrefix );
  };

  /**
   * List the current defined prefixes from the config
   * @param  {object} qonfig The Qonsole configuration object
   */
  var initPrefixes = function ( qonfig ) {
    var prefixAdd = $('ul.prefixes li:last' );
    $.each( qonfig.prefixes, function ( key, value ) {
      var keyTrimmed = $.trim( key );
      var displayKey = (!keyTrimmed || keyTrimmed === '') ? ':' : key;
      var html = sprintf.sprintf( "<li class='prefix'><label><input type='checkbox' value='%s' data-prefix='%s' checked  /> %s</label></li>",
                                  value, key, displayKey );
      $(html).insertBefore( prefixAdd);
    } );
  };

  /**
   * List the example queries from the config
   * @param  {object} qonfig Current configuration
   */
  var initExamples = function ( qonfig ) {
    var examples = $('#examples');

    examples.empty();

    $.each( qonfig.queries, function ( i, queryDesc ) {
      var html = sprintf.sprintf( '<option>%s</option>', queryDesc.name );
      examples.append( html );

      if (queryDesc.queryURL) {
        loadRemoteQuery( queryDesc.name, queryDesc.queryURL );
      }
    } );

    setFirstExampleActive();
  };

  /** Set the default active query */
  var setFirstExampleActive = function () {
    if (_outstandingQueries === 0) {
      showCurrentExample();
    }
  };

  /**
   * Load a remote query
   * @param  {string} name query name
   * @param  {string} url  query URL
   */
  var loadRemoteQuery = function ( name, url ) {
    _outstandingQueries++;

    var options = {
      success: function ( data ) {
        namedExample( name ).query = data;

        _outstandingQueries--;
        setFirstExampleActive();
      },
      failure: function () {
        namedExample( name ).query = 'Not found: ' + url;

        _outstandingQueries--;
        setFirstExampleActive();
      },
      dataType: 'text'
    };

    $.ajax( url, options );
  };

  /**
   * Set up the drop-down list of end-points
   * @param  {object} qonfig Current configuration object
   */
  var initEndpoints = function ( qonfig ) {
    var endpoints = $('#endpoints');
    endpoints.empty();

    $.each( qonfig.endpoints, function ( key, url ) {
      endpoints.append( sprintf.sprintf( '<option>%s</option>', url ) );
    } );

    setCurrentEndpoint( qonfig.endpoints.default );
  };

  /**
   * Callback for successfully loaded the configuration
   * @param  {object} qonfig Currnet configuration object
   */
  var onConfigLoaded = function ( qonfig ) {
    _config = qonfig;
    initPrefixes( qonfig );
    initExamples( qonfig );
    initEndpoints( qonfig );
  };

  /**
   * Set the current endpoint text
   * @param  {string} url Query URL
   */
  var setCurrentEndpoint = function ( url ) {
    $('#sparqlEndpoint').val( url );
  };

  /**
   * Return the current endpoint text
   * @return {string} Current endpoint
   */
  var currentEndpoint = function () {
    return $('#sparqlEndpoint').val();
  };

  /**
   * Return the query definition with the given name
   * @param  {string} name Example name
   * @return {object}  Query definition
   */
  var namedExample = function ( name ) {
    return _.find( config().queries, function ( ex ) {return ex.name === name;} );
  };

  /**
   * Return the DOM node representing the query editor
   * @return {DOM} The query editor node
   */
  var queryEditor = function () {
    if (!_queryEditor) {
      _queryEditor = new CodeMirror( $('#query-edit-cm').get(0), {
        lineNumbers: true,
        mode: 'sparql',
        autoRefresh: true
      } );
    }
    return _queryEditor;
  };

  /**
   * Return the current value of the query edit area
   * @return {string} Current query as text
   */
  var currentQueryText = function () {
    return queryEditor().getValue();
  };

  /**
   * Set the value of the query edit area
   * @param  {string} text New query text
   */
  var setCurrentQueryText = function ( text ) {
    queryEditor().setValue( text );
  };

  /**
   * Display the given query, with the currently defined prefixes
   * @param  {string} exampleName The name of the example to show
   */
  var showCurrentExample = function ( exampleName ) {
    var example = exampleName || currentNamedExample();
    var query = checkForURLQuery() || namedExample( example );

    displayQuery( query );
  };

  /**
   * Check to see if a query has been passed via the URL
   * @return {string} The query passed-in via the URL, or null
   */
  var checkForURLQuery = function () {
    return config().allowQueriesFromURL ? searchParams().query : null;
  };

  /**
   * Return the currently active named example
   * @return {string} The curently active example name
   */
  var currentNamedExample = function () {
    return $('#examples').val();
  };

  /**
   * Display the given query
   * @param  {string} query The query as a string
   */
  var displayQuery = function ( query ) {
    if (query) {
      var queryBody = query.query ? query.query : query;
      var prefixes = assemblePrefixes( queryBody, query.prefixes );

      var q = sprintf.sprintf( '%s\n\n%s', renderPrefixes( prefixes ), stripLeader( queryBody ) );
      setCurrentQueryText( q );

      syncPrefixButtonState( prefixes );
    }
  };

  /**
   * Return the currenty selected output format
   * @return {string} Output format
   */
  var selectedFormat = function () {
    return $('[name=format]').val();
  };

  /**
   * Return the currenty selected output format
   * @param  {string} format Output format
   * @return {string} selected format
   */
  var setSelectedFormat = function ( format ) {
    return $('[name=format]').val( format );
  };

  /**
   * Return the prefixes currently defined in the query
   * @return {object} The prefixes from the current query
   */
  var parseQueryPrefixes = function () {
    var prefixes = {};
    var prefixPairs = assemblePrefixesFromQuery( currentQueryText() );
    _.each( prefixPairs, function ( pair ) {prefixes[pair.name] = pair.uri;} );
    return prefixes;
  };

  /**
   * Assemble the set of prefixes to use when initially rendering the query
   * @param  {string} queryBody The body of the query
   * @param  {object} queryDefinitionPrefixes The prefixes from the query config
   * @return {object} The preferred set of prefixes
   */
  var assemblePrefixes = function ( queryBody, queryDefinitionPrefixes ) {
    if (queryBody.match( /\@?prefix\s/g )) {
      // strategy 1: there are prefixes encoded in the query body
      return assemblePrefixesFromQuery( queryBody );
    } else if (queryDefinitionPrefixes) {
      // strategy 2: prefixes given in query def
      return _.map( queryDefinitionPrefixes, function ( prefixName ) {
        return {name: prefixName, uri: config().prefixes[prefixName] };
      } );
    }

    return assembleCurrentPrefixes();
  };

  /**
   * Return an array comprising the currently selected prefixes
   * @return {array} Array of prefixes
   */
  var assembleCurrentPrefixes = function () {
    var l = $('ul.prefixes input:checked' ).map( function ( i, elt ) {
      return {name: $.trim( $(elt).data( 'prefix' ) ),
        uri: $(elt).val()};
    } );
    return $.makeArray(l);
  };

  /**
   * Return an array of the prefixes parsed from the given query body
   * @param  {string} queryBody The query body
   * @return {array} Parsed prefixes
   */
  var assemblePrefixesFromQuery = function ( queryBody ) {
    var leader = queryLeader( queryBody )[0].trim();
    var leaderLines = leader.split('\n');
    var prefixLines = _.filter(leaderLines, function (line) { return line.match(/prefix/); });
    var declarations = _.map(prefixLines, function (line) { return line.split(/\@?prefix/); });

    return _.map( declarations, function ( pair ) {
      var m = pair[1].match( /^\s*([\w\-]+)\s*:\s*<([^>]*)>\s*\.?\s*$/ );
      return ( {name: m[1], uri: m[2]} );
    } );
  };

  /**
   * Ensure that the prefix buttons are in sync with the prefixes used in a new query
   * @param  {object} prefixes The prefixes to be used in rendering
   */
  var syncPrefixButtonState = function ( prefixes ) {
    $('ul.prefixes input' ).each( function ( i, elt ) {
      var name = $.trim( $(elt).data( 'prefix' ) );

      if (_.find( prefixes, function (p) {return p.name === name;} )) {
        $(elt).attr( 'checked', true );
      }      else {
        $(elt).removeAttr( 'checked' );
      }
    } );
  };

  /**
   * Split a query into leader (prefixes and leading blank lines) and body
   * @param  {string} query Input query
   * @return {array} Length-2 array of header and body
   */
  var queryLeader = function ( query ) {
    var isLeaderLine = function (line) {
      return line.match(/(^\s*\@?prefix)|(^\s*\#)|(^\s*$)/);
    };

    var lines = query.split( '\n' );
    var leaderLines = [];
    var leader = true;

    while (leader && !_.isEmpty(lines)) {
      leader = isLeaderLine(lines[0]);
      if (leader) {
        leaderLines.push(lines.shift());
      }
    }

    return [leaderLines.join('\n'), lines.join('\n')];
  };

  /**
   * Remove the query leader
   * @param  {string} query Input query
   * @return {string} The leader part of the query
   */
  var stripLeader = function ( query ) {
    return queryLeader( query )[1];
  };

  /**
   * Return a string comprising the given prefixes in SPARQL format
   * @param  {object} prefixes Given prefixes
   * @return {string} SPARQL-format prefixes
   */
  var renderPrefixes = function ( prefixes ) {
    return _.map( prefixes, function ( p ) {
      return sprintf.sprintf( 'prefix %s: <%s>', p.name, p.uri );
    } ).join( '\n' );
  };

  /**
   * Add or remove the given prefix declaration from the current query
   * @param  {string} prefix prefix short-name
   * @param  {uri} uri The full URI
   * @param  {boolean} added True for add, false for remove
   */
  var updatePrefixDeclaration = function ( prefix, uri, added ) {
    var query = currentQueryText();
    var lines = query.split( '\n' );
    var pattern = new RegExp( '^prefix +' + prefix + ':');
    var found = false;
    var i;

    for (i = 0; !found && i < lines.length; i++) {
      found = lines[i].match( pattern );
      if (found && !added) {
        lines.splice( i, 1 );
      }
    }

    if (!found && added) {
      for (i = 0; i < lines.length; i++) {
        if (!lines[i].match( /^prefix/ )) {
          lines.splice( i, 0, sprintf.sprintf( 'prefix %s: <%s>', prefix, uri ) );
          break;
        }
      }
    }

    setCurrentQueryText( lines.join( '\n' ) );
  };

  /**
   * Return the sparql service we're querying against
   * @return {object} Object encapsulting the SPARQL service
   */
  var sparqlService = function () {
    var service = config().service;
    if (!service) {
      // default is the remote service
      config().service = new RemoteSparqlService();
      service = config().service;
    }

    return service;
  };

  /**
   * [description]
   * @param  {event} e The triggering event
   */
  var runQuery = function ( e ) {
    e.preventDefault();
    resetResults();

    var query = currentQueryText();
    var format = checkOutputFormat( query );

    var options = {
      url: currentEndpoint(),
      format: format,
      success: function ( data ) {
        onQuerySuccess( data, format );
      },
      error: onQueryFail
    };

    sparqlService().execute( query, options );
  };


  /**
   * Hide or reveal an element using Bootstrap .hidden class
   * @param  {DOM} elem DOM node to act on
   * @param  {boolean} visible True to render the node visible
   */
  var elementVisible = function ( elem, visible ) {
    if (visible) {
      $(elem).removeClass( 'hidden' );
    }    else {
      $(elem).addClass( 'hidden' );
    }
  };

  /** Prepare to show query time taken */
  var startTimingResults = function () {
    _startTime = new Date().getTime();
    elementVisible( '.timeTaken' );
  };

  /**
   * Show results count and time
   * @param  {int} count Count of results
   */
  var showResultsTimeAndCount = function ( count ) {
    var duration = new Date().getTime() - _startTime;
    var ms = duration % 1000;
    duration = Math.floor( duration / 1000 );
    var s = duration % 60;
    var m = Math.floor( duration / 60 );
    var suffix = (count !== 1) ? 's' : '';

    var html = sprintf.sprintf( '%s result%s in %d min %d.%03d s', count, suffix, m, s, ms );

    $('.timeTaken').html( html );
    elementVisible( '.timeTaken', true );
  };

  /** Reset the results display */
  var resetResults = function () {
    $('#results').empty();
    elementVisible( '.timeTaken', false );
  };

  /**
   * Report query failure
   * @param  {object} jqXHR jQuery response object
   */
  var onQueryFail = function ( jqXHR ) {
    showResultsTimeAndCount( 0 );
    var text = jqXHR.valueOf().responseText || sprintf.sprintf( "Sorry, that didn't work because: '%s'", jqXHR.valueOf().statusText );
    $('#results').html( sprintf.sprintf( "<pre class='text-danger'>%s</pre>", _.escape(text) ) );
  };

  /**
   * Query succeeded - use display type to determine how to render
   * @param  {object} data XHR return
   * @param  {string} format Output format
   */
  var onQuerySuccess = function ( data, format ) {
    var options = data.asFormat( format, config() );

    if (options && !options.table) {
      showCodeMirrorResult( options );
    }    else if (options && options.table) {
      showTableResult( options );
    }
  };

  /**
   * Show the given text value in a CodeMirror block with the given language mode
   * @param  {object} options Display options
   */
  var showCodeMirrorResult = function ( options ) {
    showResultsTimeAndCount( options.count );

    // eslint-disable-next-line no-new
    new CodeMirror( $('#results').get(0), {
      value: options.data,
      mode: options.mime,
      lineNumbers: true,
      extraKeys: {'Ctrl-Q': function (cm) { cm.foldCode(cm.getCursor()); }},
      gutters: ['CodeMirror-linenumbers', 'CodeMirror-foldgutter'],
      foldGutter: true,
      readOnly: true
    } );
  };

  /**
   * Show the result using jQuery dataTables
   * @param  {object} options Display options
   */
  var showTableResult = function ( options ) {
    showResultsTimeAndCount( options.count );

    options.oLanguage = {
      'sEmptyTable': 'Query did not return any results.'
    };

    // if user has specified an order, don't let datatables override the sort
    if (currentQueryText().match( /order by/i )) {
      options.aaSorting = [];
    }

    $('#results').empty()
                 .append( "<div class='auto-overflow'></div>")
                 .children()
                 .append( "<table cellpadding='0' cellspacing='0' border='0' class='display'></table>" )
                 .children()
                 .dataTable( options );
  };

  /**
   * Lookup a prefix on prefix.cc
   * @param  {event} e Input event
   */
  var onLookupPrefix = function ( e ) {
    e.preventDefault();

    var prefix = $.trim( $('#inputPrefix').val() );
    $('#inputURI').val('');

    if (prefix) {
      $.getJSON( sprintf.sprintf( 'http://prefix.cc/%s.file.json', prefix ),
                function ( data ) {
                  $('#inputURI').val( data[prefix] );
                }
            );
    }
  };

  /** User wishes to add the prefix */
  var onAddPrefix = function () {
    var prefix = $.trim( $('#inputPrefix').val() );
    var uri = $.trim( $('#inputURI').val() );

    if (uri) {
      config().prefixes[prefix] = uri;
    }    else {
      delete config().prefixes[prefix];
    }

    // remember the state of current user selections, then re-create the list
    var selections = {};
    var ul = $('ul.prefixes');
    ul.find('input')
      .each( function ( i, elem ) {
        selections[$(elem).data('prefix')] = $(elem).is(':checked');
      } );

    ul.find('li[class!=keep]').remove();
    initPrefixes( config() );

    // restore selections state
    $.each( selections, function ( k, v ) {
      var elem = ul.find(sprintf.sprintf('[data-prefix=%s]', k));
      if (v) {
        elem.attr( 'checked', true );
      }      else {
        elem.removeAttr('checked');
      }
    } );

    var lines = currentQueryText().split('\n');
    lines = _.reject( lines, function ( line ) {return line.match( /^prefix/ );} );
    var q = sprintf.sprintf( '%s\n%s', renderPrefixes( assembleCurrentPrefixes() ), lines.join( '\n' ) );
    setCurrentQueryText( q );
  };

  /**
   * Disable or enable the button to submit a query
   * @param  {boolean} disable Flag
   */
  var disableSubmit = function ( disable ) {
    var elem = $('a.run-query');
    elem.prop( 'disabled', disable );
    if (disable) {
      elem.addClass( 'disabled' );
    }    else {
      elem.removeClass( 'disabled' );
    }
  };

  /**
   * Check the output format. Reset output format to text for describe and construct queries
   * @param  {string} query The current query
   * @return {string} The preferred output format
   */
  var checkOutputFormat = function ( query ) {
    if (isDescribeOrConstructQuery( query ) && _.includes( ['tsv'], selectedFormat() )) {
      setSelectedFormat( 'text' );
    }

    return selectedFormat();
  };

  /**
   * Check for describe or constuct query
   * @param  {string} query The current query
   * @return {boolean} True if this is a describe or construct query
   */
  var isDescribeOrConstructQuery = function ( query ) {
    var body = queryLeader( query )[1];
    return body.match( /^(describe|construct)/i );
  };

  /* Jquery spinner */

  var spinCount = 0;

  var DEFAULT_SPIN_OPTIONS = {
    color: '#ACCD40',
    lines: 12,
    radius: 20,
    length: 10,
    width: 4,
    bgColor: 'white'
  };

  /* Start the spinner */
  var spinStart = function ( options ) {
    spinCount = spinCount + 1;
    if (spinCount === 1) {
      $('body').spin( options || DEFAULT_SPIN_OPTIONS );
    }
  };

  /** Stop the spinner */
  var spinStop = function () {
    spinCount = spinCount - 1;
    if (spinCount === 0) {
      $('body').spin( false );
    }
  };


  /* Utils */

  /**
   * Return an object containing one key for every distinct query
   * parameter, with also a `_vars` key which lists the query parameter
   * variables in order. Keys and values will be automatically
   * unescaped.
   * @param {string} location The current location object. If null, window.location
   *                 will be used.
   * @return {object} environment
   */
  var searchParams = function ( location ) {
    var loc = location || window.location;
    var env = {};

    if (loc.search) {
      var url = loc.search.replace( /^\?/, '' );
      var args = url.split('&');

      for (var i = 0; i < args.length; i++) {
        var argPair = args[i].split('=');

        var key = decodeURIComponent( argPair[0] );
        var val = argPair.length > 1 ? decodeURIComponent( argPair[1] ) : null;

        if (env[key]) {
          env[key] = (env[key].constructor === Array) ? env[key] : [env[key]];
          env[key].push( val );
        }        else {
          env[key] = val;
        }
      }
    }

    return env;
  };

  return {
    currentQueryText: currentQueryText,
    init: init,
    setCurrentQueryText: setCurrentQueryText
  };
} );
