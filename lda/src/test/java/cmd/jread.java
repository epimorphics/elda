package cmd;

import java.io.IOException;
import java.io.Reader;

import com.epimorphics.jsonrdf.ParseWrapper;
import com.hp.hpl.jena.util.FileManager;

public class jread {

	public static void main( String [] args ) {
		final String text = FileManager.get().readWholeFileAsUTF8( "/tmp/xxx" );
		Reader r = new Reader() {

			int i = 0;
			
			@Override public void close() throws IOException {
				// TODO Auto-generated method stub
				
			}

			@Override public int read(char[] arg0, int arg1, int arg2) throws IOException {
				if (i == text.length()) return 0;
				arg0[arg1] = text.charAt(i++);
				System.err.print(arg0[arg1] );
				return 1;
			}
			
		};
		ParseWrapper.readerToJsonObject( r );
	}
}
