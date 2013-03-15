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
}
