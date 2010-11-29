/*
    See lda-top/LICENCE for the licence for this software.
    (c) copyright Epimorphics Limited 2010
    $Id$
*/
package com.epimorphics.lda.tests;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.is;
import static com.epimorphics.lda.support.MediaTypeSupport.UNSPECIFIED;
import static com.epimorphics.lda.support.MediaTypeSupport.mediaTypeString;
import static com.epimorphics.util.CollectionUtils.list;

public class TestMediaTypeHelp 
	{
	static final MediaType AB = new MediaType( "A", "B" );
	static final MediaType CD = new MediaType( "C", "D" );
	
	@Test public void testEmptyListGeneratesEmptyResultString()
		{
		List<MediaType> none = new ArrayList<MediaType>();
		assertThat( mediaTypeString( none ), is( "" ) );
		}
	
	@Test public void testUnspecifiedListGeneratesEmptyResultString()
		{
		assertThat( mediaTypeString( list( UNSPECIFIED ) ), is( "" ) );
		}
	
	@Test public void testSingletonListGeneratesResultString()
		{
		assertThat( mediaTypeString( list( AB ) ), is( "A/B" ) );
		}
	
	@Test public void testMultipleListGeneratesMultipleResultString()
		{
		assertThat( mediaTypeString( list( AB, CD ) ), is( "A/B, C/D" ) );
		}
	
	@Test public void testMultipleListWithUnspecifiedGeneratesMultipleResultString()
		{
		assertThat( mediaTypeString( list( AB, UNSPECIFIED, CD ) ), is( "A/B, C/D" ) );
		}
	}
