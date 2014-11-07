/** Javascript support for adding maps to Elda rendered pages */
/* global: define */
define( ['OpenLayers', 'OpenStreetMap', 'proj4js-combined'], function( OpenLayers ) {
  /* jshint strict: true, undef:true */
  "use strict";

  /** The correct projection for the UK - other locations may require different projections */
  var projection = new OpenLayers.Projection( "EPSG:4326" );

  /** Add a small map, suitable for displaying per item */
  var addSmallMap = function( divID, lon, lat, markerImage ) {
    var map = createMap( divID, smallMapControls() );
    addOSMLayer( map );
    centreMap( lon, lat, map );

    var markersLayer = new OpenLayers.Layer.Markers( "Markers" );
    map.addLayer( markersLayer );
    addMarker( markersLayer, lon, lat, markerImage, map );
  };

  /** Add a large map, for showing a group of items */
  var addLargeMap = function( divId, locations, markerImage ) {
    var map = createMap( divId, largeMapControls() );

    var markersLayer = new OpenLayers.Layer.Markers("Markers");
    map.addLayer( markersLayer );
    addOSMLayer( map );

    var bounds = new OpenLayers.Bounds();

    for( var i = 0; i < locations.length; i++) {
      var loc = locations[i];
      var point = longLatPoint( loc.lon, loc.lat, map );

      bounds.extend( point );

      addMarker( markersLayer, loc.lon, loc.lat, markerImage, map );
    }

    var zoom = map.getZoomForExtent(bounds);
    map.setCenter( bounds.getCenterLonLat(), zoom < 14 ? zoom : 14 );
  };

  /** @return A new map object for the given div */
  var createMap = function( divID, controls ) {
     return new OpenLayers.Map( divID, {controls: controls} );
  };

  /** @return The controls we want to display on the small map */
  var smallMapControls = function() {
    return [new OpenLayers.Control.ArgParser()];
  };

  /** @return The list of controls for the large map */
  var largeMapControls = function() {
    return [
      new OpenLayers.Control.Navigation(),
      new OpenLayers.Control.KeyboardDefaults(),
      new OpenLayers.Control.ZoomBox(),
      new OpenLayers.Control.ZoomPanel(),
      new OpenLayers.Control.Attribution()
    ];
  };

  /** Add layers to the given map */
  var addOSMLayer = function( map ) {
    map.addLayer( new OpenLayers.Layer.OSM.Mapnik() );
  };

  /** Centre the map on the given point */
  var centreMap = function( lon, lat, map ) {
    var center = longLatPoint( lon, lat, map );
    map.setCenter( center, defaultZoom() );
  };

  /** Return the default zoom level */
  var defaultZoom = function() {
    return 13;
  };

  /** Return a point representing a given longitude and latitude */
  var longLatPoint = function( lon, lat, map ) {
    return new OpenLayers.LonLat( lon, lat ).transform( projection, map.getProjectionObject() );
  };

  /** Add a marker for the given location */
  var addMarker = function( markersLayer, lon, lat, markerImage, map ) {
    var marker;

    if (markerImage.icon) {
      marker = new OpenLayers.Marker( longLatPoint( lon, lat, map ), markerImage.icon.clone() );
    }
    else {
      var size = new OpenLayers.Size( markerImage.w, markerImage.h );
      var offset = new OpenLayers.Pixel( -(size.w/2), -size.h );
      var icon = new OpenLayers.Icon( markerImage.image, size, offset );
      marker = new OpenLayers.Marker( longLatPoint( lon, lat, map ), icon );
      markerImage.icon = icon;
    }

    markersLayer.addMarker(marker);
  };

  return {
    addLargeMap: addLargeMap,
    addSmallMap: addSmallMap
  };
} );
