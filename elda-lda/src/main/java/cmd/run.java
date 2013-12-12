/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package cmd;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class run 
	{
	public static void main( String [] args ) throws IOException, InterruptedException 
		{
		String eldaJarName = System.getProperty( "java.class.path", "elda.jar" );
		String toDir = jarToDir( eldaJarName );
		unzipJarfile( toDir, eldaJarName );
	//
		ProcessBuilder pb = new ProcessBuilder( buildArgs( args ) );
		pb.directory( new File( toDir ) );
		pb.redirectErrorStream( true );
	//
		forwardOutput( pb.start() );
		}

	private static String jarToDir( String jarName ) 
		{
		return jarName.replaceFirst( "^e", "E" ).replaceAll( "-", "_" ).replace( ".jar", "" );
		}

	/**
	    copy whatever the process <code>p</code> produces to standard
	    output. Give up on any exception.
	*/
	private static void forwardOutput( Process p ) throws IOException 
		{
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

	/**
	    Build the args for the Jetty command line that will be launched
	    when we've unzipped the jarfile. Any of <code>args</code> that
	    start with -D will be inserted after the java command; any that
	    don't will be inserted after start.jar.
	*/
	private static List<String> buildArgs( String[] args ) 
		{
		List<String> pargs = new ArrayList<String>();
		List<String> postponed = new ArrayList<String>();
		pargs.add( "java" );
		for (String arg: args) 
			(arg.startsWith("-D") ? pargs : postponed).add( arg );
		pargs.add( "-jar" );
		pargs.add( "start.jar" );
		pargs.addAll( postponed );
		return pargs;
		}

	/**
	    Unzip the file named <code>jarFile</code> into the directory named
	    <code>toDir</code>. Give up if there are any exceptions, which may
	    trash existing files and leave incomplete new ones.
	*/
	public static void unzipJarfile( String toDir, String jarFile ) throws IOException 
		{
		System.err.println( "INFO: unzipping " + jarFile + " to " + toDir );
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
