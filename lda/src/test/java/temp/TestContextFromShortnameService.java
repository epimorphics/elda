package temp;

import static org.junit.Assert.*;


import org.junit.Test;

import com.epimorphics.jsonrdf.Context;
import com.epimorphics.lda.shortnames.ContextMaker;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.shortnames.StandardShortnameService;

public class TestContextFromShortnameService {

	@Test public void testEmptyInitialisation() {
		
		ShortnameService sns = new StandardShortnameService();		
		Context fromSNS = sns.asContext();
		Context byConstruction = ContextMaker.contextFrom( sns );
		
		System.err.println( fromSNS.diff( byConstruction ) );
		assertEquals( fromSNS, byConstruction );
	}
	
	
	@Test public void checkingOrderSensitivity() {
		Context c = new Context();
	//
		c.recordPreferredName( "who", "eh:/ns2/who" );
		c.recordPreferredName( "who", "eh:/ns1/who" );
	//
		boolean ns2 = "who".equals( c.getNameForURI( "eh:/ns2/who" ) );
		boolean ns1 = "who".equals( c.getNameForURI( "eh:/ns1/who" ) );
		
//		System.err.println( "ns1 = " + ns1 );
//		System.err.println( "ns2 = " + ns2 );
	}
	
}
