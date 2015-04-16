/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

/*
	(c) Copyright 2010 Epimorphics Limited
	[see end of file]
	$Id$
*/

package com.epimorphics.lda.support;

/**
    Utilities to handle numeric arguments in API queries.
    @author chris
*/
public class NumericArgUtils
    {
    public static float milesToDegrees( float miles )
        { return miles / 70; }

    /**
        Answer a distance in miles as expressed by <i>arg</i> in Double format.
        <i>arg</i> may end in "mi" (to mean miles, no change) or "km" to mean
        kilometres (hence multiplied by 5/8). 
    */
    public static double readMiles( String arg ) 
        {
        double multiplier = 1.0;
        if (arg.endsWith( "mi" )) 
            arg = arg.replace( "mi", "" );
        else if (arg.endsWith( "km" )) 
            { arg = arg.replace( "km", "" ); multiplier = 5.0/8.0; }
        return readNumber( "distance", arg, 0.0, 999.0 ) * multiplier;
        }

    public static double readNumber( String param, String arg, double low, double high )
        {
        try 
            {
            double result = Double.parseDouble( arg );
            if (low <= result && result <= high) return result;
            else throw new IllegalArgumentException( param + " " + arg + " should be between " + low + " and " + high + "." );
            }
        catch (NumberFormatException e) 
            {
            throw new IllegalArgumentException( "the value " + arg + " for " + param + " doesn't look like a number to me.", e );  
            }
        }

    public static double readDegrees( String param, String arg ) 
        {
        return readNumber( param, arg, -99.0f, +99.0f );
        }

    /**
        Answer a delta in degrees latitude which corresponds to the distance
        <i>d</i> [in miles] in the vicinity of the point at latitude <i>lat</i> 
        and longitude <i>lang</i>. The answer is <i>approximate</i>.
    */
    public static double deltaLat( double d, double lat, double lang )
        { 
        return d / 70;
        }

    static final double degreesToRadians = Math.PI / 180.0;
    
    /**
        Answer a delta in degrees longitude which corresponds to the distance
        <i>d</i> [in miles] in the vicinity of the point at latitude <i>lat</i> 
        and longitude <i>lang</i>. The answer is <i>approximate</i>.
    */
    public static double deltaLong( double d, double lat, double lang )
        {
        double latCosine = Math.cos( lat * degreesToRadians );
        return d / 70 / latCosine;
        }
    }

    
/*
    (c) Copyright 2010 Epimorphics Limited
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
