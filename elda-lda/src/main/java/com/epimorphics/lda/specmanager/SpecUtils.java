/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

/******************************************************************
    File:        SpecUtils.java
    Created by:  Dave Reynolds
    Created on:  7 Feb 2010
 * 
 * (c) Copyright 2010, Epimorphics Limited
 * $Id:  $
 *****************************************************************/

package com.epimorphics.lda.specmanager;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import com.epimorphics.lda.exceptions.APIException;

/**
 * Random utility methods for the implementing SpecManagers
 * 
 * @author <a href="mailto:der@epimorphics.com">Dave Reynolds</a>
 * @version $Revision: $
 */
public class SpecUtils {

    public static byte[] digestKey(String uri, String key) {
        MessageDigest digester;
        try {
            digester = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new APIException(e.getMessage(), e);
        }
        digester.update(uri.getBytes());
        digester.update(key.getBytes());
        return digester.digest();
    }
        
    public static boolean keyMatches(String uri, String key, byte[] digest) {
        byte[] newDigest = digestKey(uri, key);
        return Arrays.equals(digest, newDigest);
    }
    
}

