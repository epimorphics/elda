/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.

    (c) Copyright 2014 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.routing;

import java.io.File;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.support.*;
import com.epimorphics.lda.vocabularies.API;
import com.epimorphics.lda.vocabularies.ELDA_API;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;

/**
    Some methods useful in the two servlet-handling components
    of the routing classes.
*/
public class ServletUtils {

    static Logger log = LoggerFactory.getLogger( ServletUtils.class );

    public static String withTrailingSlash(String path) {
        return path.endsWith("/") ? path : path + "/";
    }

    public static String[] safeSplit(String s) {
        return s == null || s.equals("")
            ? new String[] {}
            : s.replaceAll( "[ \n\t]", "" ).split(",")
            ;
    }

    public static Set<String> specNamesFromSystemProperties() {
        Properties p = System.getProperties();
        return MapMatching.allValuesWithMatchingKey( Container.ELDA_SPEC_SYSTEM_PROPERTY_NAME, p );
    }

    public static void addLoadedFrom( Model m, String name ) {
        List<Statement> toAdd = new ArrayList<Statement>();
        List<Resource> apis = m
            .listStatements( null, RDF.type, API.API )
            .mapWith(Statement.Util.getSubject)
            .toList()
            ;
        for (Resource api: apis) toAdd.add( m.createStatement( api, ELDA_API.loadedFrom, name ) );
        m.add( toAdd );
    }

    public static boolean containsStar(String prefixPath) {
        return prefixPath == null ? false : prefixPath.contains("*");
    }

    /**
        nameToPrefix matches the last segment of the pathname <code>specPath</code>
        against the leafname <code>name</code> and replaces any '*' character
        in <code>wildPrefix</code> with the matched wildcard part(s) (joined
        if necessary by the character '-') from the match, returning the
        modified result.
    */
    public static String nameToPrefix(String wildPrefix, String specPath, String name) {
        String wildPart = new File(specPath).getName();
        String matched = new Glob().extract( wildPart, "-", name );
        return wildPrefix.replace( "*", (matched == null ? "NOMATCH" : matched) );
    }

    /** local:something or tdb:something. */
    public static boolean isSpecialName( String specPath ) {
        return specPath.startsWith( Container.LOCAL_PREFIX )
            || specPath.startsWith( TDBManager.PREFIX )
            ;
    }

    public interface GetInitParameter {
        public String getInitParameter(String name);
    }

    public static Set<String> specNamesFromInitParam( GetInitParameter f ) {
        String specString = f.getInitParameter( Container.INITIAL_SPECS_PARAM_NAME );
        return new HashSet<String>( Arrays.asList( safeSplit(specString) ) );
    }

    /**
        The spec names can come from the init parameter set in the web.xml,
        or they may preferentially be set from system properties.

         @return A set of spec names as Strings
    */
    public static Set<String> getSpecNamesFromContext(GetInitParameter f) {
        Set<String> found = specNamesFromSystemProperties();
        return found.size() > 0 ? found : specNamesFromInitParam(f);
    }

    public static String expandLocal( String baseFilePath, String given, String ifNull ) {
        String s = (given == null ? ifNull : given);
        return s.replaceFirst( "^" + Container.LOCAL_PREFIX, baseFilePath );
    }
}
