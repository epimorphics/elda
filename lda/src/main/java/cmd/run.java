package cmd;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class run 
	{
	public static void main( String [] ignored ) throws IOException, InterruptedException 
		{
		String toDir = "Elda_1.0";
		unzipJarfile( toDir, "elda.jar" );
	//
		ProcessBuilder pb = new ProcessBuilder( "java", "-jar", "start.jar" );
		pb.directory( new File( toDir ) );
		pb.redirectErrorStream( true );
		Process p = pb.start();
	//
		byte [] bytes = new byte[1024];
		BufferedInputStream is = new BufferedInputStream( p.getInputStream() );
		while (true) 
			{
			int count = is.read( bytes );
			if (count < 0) break;
			System.out.write( bytes, 0, count );
			System.out.flush();
			}
		}

	public static void unzipJarfile( String toDir, String jarFile ) throws IOException 
		{
		byte [] buffer = new byte[100 * 1024];
		File pathPrefix = new File( toDir );
		ZipFile z = new ZipFile( jarFile );
		Enumeration<? extends ZipEntry> e = z.entries();
		while (e.hasMoreElements()) 
			{
			ZipEntry x = e.nextElement();
			if (x.isDirectory())
				{
				// System.err.println( "creating directory " + x.getName() + " in " + pathPrefix );
				new File( pathPrefix, x.getName() ).mkdirs();
				}
			else
				{
				File whereto = new File( pathPrefix, x.getName() );
				// System.err.println( "creating file " + whereto );
				InputStream is = z.getInputStream( x );
				OutputStream os = new FileOutputStream( whereto );
				while (true)
					{
					int count = is.read( buffer );
					if (count < 0) break;
					os.write( buffer, 0, count );
					}
				is.close();
				os.flush();
				os.close();
				}
			}
		}
	
	}
