/** Javascript support for adding maps to Elda rendered pages */
var EldaMaps = function() {
  /** The correct projection for the UK - other locations may require different projections */
  var projection = new OpenLayers.Projection( "EPSG:4326" );

  var addSmallMap = function( divID, lon, lat, markerImage ) {
    var map = createMap( divID );
    addControlLayers( map );
    centreMap( lon, lat, map );

    var markersLayer = new OpenLayers.Layer.Markers( "Markers" );
    map.addLayer( markersLayer );
    addMarker( markersLayer, lon, lat, markerImage, map );
  };

  /** @return A new map object for the given div */
  var createMap = function( divID ) {
    var controls = controlsList();
    return new OpenLayers.Map( divID, {controls: controls} );
  };

  /** @return The controls we want to display on the map */
  var controlsList = function() {
    return [new OpenLayers.Control.ArgParser()];
  };

  /** Add layers to the given map */
  var addControlLayers = function( map ) {
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
    var size = new OpenLayers.Size( markerImage.w, markerImage.h );
    var offset = new OpenLayers.Pixel( -(size.w/2), -size.h );
    var icon = new OpenLayers.Icon( markerImage.image, size, offset );
    var marker = new OpenLayers.Marker( longLatPoint( lon, lat, map ), icon );

    markersLayer.addMarker(marker);
  };

  return {
    addSmallMap: addSmallMap
  };
}();
