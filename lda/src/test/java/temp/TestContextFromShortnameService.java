package temp;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

import com.epimorphics.jsonrdf.Context;
import com.epimorphics.jsonrdf.ContextPropertyInfo;
import com.epimorphics.lda.shortnames.NameMap;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.shortnames.StandardShortnameService;

public class TestContextFromShortnameService {

	@Test public void testEmptyInitialisation() {
		
		ShortnameService sns = new StandardShortnameService();
		
		Context fromSNS = sns.asContext();
		
		Context byConstruction = contextFrom( sns );
		
//		System.err.println( fromSNS.diff( byConstruction ) );
//		assertEquals( fromSNS, byConstruction );
	}

	private Context contextFrom(ShortnameService sns) {
		Context result = new Context();
		NameMap nm = sns.nameMap();
	//
		Map<String, String> uriToNames = nm.stage2().result();
		for (Map.Entry<String, String> e: uriToNames.entrySet()) {
			result.recordPreferredName( e.getValue(), e.getKey() );
		}
	//
		Map<String, ContextPropertyInfo> im = nm.getInfoMap();
		for (Map.Entry<String, ContextPropertyInfo> e: im.entrySet()) {
			result.setProperty( e.getKey(), e.getValue().clone() );
		}
		return result;
	}
}
