package cmd;

import java.io.IOException;
import java.io.Reader;

import com.epimorphics.jsonrdf.ParseWrapper;
import com.hp.hpl.jena.util.FileManager;

public class jread {

	private static final class EchoStringReader extends Reader {
		private final String text;
		int i = 0;

		private EchoStringReader(String text) {
			this.text = text;
		}

		@Override public void close() throws IOException {
			// TODO Auto-generated method stub
			
		}

		@Override public int read(char[] arg0, int arg1, int arg2) throws IOException {
			if (i == text.length()) return 0;
			arg0[arg1] = text.charAt(i++);
			System.err.print(arg0[arg1] );
			return 1;
		}
	}

	public static void main( String [] args ) {
		final String text = FileManager.get().readWholeFileAsUTF8( "/tmp/xxx" );
		ParseWrapper.readerToJsonObject( new EchoStringReader(text) );
	}
}
