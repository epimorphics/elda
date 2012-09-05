package temp;

import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.epimorphics.lda.renderers.XMLRenderer;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.shortnames.StandardShortnameService;
import com.epimorphics.lda.support.Times;
import com.epimorphics.util.DOMUtils;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.util.FileManager;

public class TestSuspectRendering {
	
	static Model m = FileManager.get().loadModel( "/tmp/bw.ttl" );

	@Test @Ignore public void testNastyBug() {
		Resource root = m.createResource( "http://environment.data.gov.uk/def/bathing-water/BathingWater" );
		
		m.write( System.out, "TTL" );

		PrefixMapping pm = root.getModel();
		ShortnameService sns = new StandardShortnameService();
		XMLRenderer xr = new XMLRenderer( sns );
		Document d = DOMUtils.newDocument();
		xr.renderInto( root, d, false );
		Node de = d.getDocumentElement(); // .getFirstChild();
		String obt = DOMUtils.renderNodeToString( new Times(), de, pm );
		System.err.println( ">> " + obt );
		
//		Node expected = new TinyParser().parse( desired );
//		if (!de.isEqualNode( expected )) 
//			{
//			String exp = DOMUtils.renderNodeToString( new Times(), expected, pm );
//			String obt = DOMUtils.renderNodeToString( new Times(), de, pm );
////			System.err.println( "expected:\n" + exp );
////			System.err.println( "obtained:\n" + obt );
//			fail( "ALAS -- rendering not as expected:\n" + exp + "obtained:\n" + obt );
//			}
		}
		
}
