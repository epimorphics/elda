package com.epimorphics.lda.support;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 	<p>
 	Glob allows glob-like wildcard expansion in pathnames, specifically the
 	use of * to represent any sequence of non-/ characters. A Glob is 
 	initialised with a FileSystemInterface instance that encapsulates the
 	test for isDirectory() and listing files in a given directory; typically
 	this just falls back to the usual File primitives.
 	</p>
 	
 	<p><b>note</b>
 	Future versions of this class may implement more globbing features.
 	</p>
*/
public class Glob {
	
	/**
	 	The FileSystemInterface for querying the filesystem.
	*/
	protected final FileSystemInterface fs;
	
	/**
	    Initialise this Glob with the default FileSystemInterface.
	*/
	public Glob() {
		this( FileSystemInterface.fsInstance );
	}
	
	/**
	    Initialise this Glob with the specified FileSystemInterface.
	*/
	public Glob(FileSystemInterface fs) {
		this.fs = fs;
	}
	
	/**
	    filesMatching(path) returns a list of all the files whose names match
	    the provided path. "Matching" means that there are replacement
	    strings (not containing /) for each of the '*'s in the path that
	    make the path equal to the filename.
	*/
	public List<File> filesMatching(String path) {
		return filesMatching( new ArrayList<File>(), path );
	}

	/**
	 	filesMatching(files,path) appends to the files list all the files
	 	whose names match the provided path (as above) and returns that
	 	same list.	 	
	*/
	public List<File> filesMatching(List<File> files, String path) {
		List<String> segments = splitFilePath( new File( path ) );
		String xroot = segments.get(0);
		List<String> remainder = segments.subList(1, segments.size() );
		return  filesMatching( files, new File( xroot ), remainder );	
	}

	/**
	    Return the portion(s) of name that is matched by the wildcard(s) in
	    glob, or null if there is no match.
	*/
	public String extract( String glob, String name ) {
		return extract( glob, "", name );
	}

	public String extract( String glob, String join, String name ) {
	
		Pattern g = Pattern.compile(toRegex(glob));
		Matcher m = g.matcher( name );
		if (m.matches()) {
			boolean needsJoin = false;
			StringBuilder result = new StringBuilder();
			for (int i = 1; i <= m.groupCount(); i += 1) {
				if (needsJoin) result.append( join );
				else needsJoin = true;
				result.append(m.group(i));				
			}
			return result.toString();
		}
		return null;
	}
	
	private List<File> filesMatching(List<File> files, File root, List<String> segments ) {
		if (segments.isEmpty()) {
			files.add( root );
		} else if (fs.isDirectory(root)) {
			File[] listedFiles = fs.listFiles( root, matching( segments.get(0) ) );
			if (listedFiles != null)
				for (File f: listedFiles ) {
					filesMatching( files, f, segments.subList(1, segments.size() ) );
			}
		} 			
		return files;
	}

	private FilenameFilter matching(String globString) {
		final Pattern p = Pattern.compile( toRegex( globString ) );
		return new FilenameFilter() {	
			@Override public String toString() {
				return "`" + p.toString() + "`";
			}
			
			@Override public boolean accept (File f, String name) {
				return p.matcher( name ).matches();
			}
		};
	}

	private String toRegex( String globString ) {
		StringBuilder re = new StringBuilder( globString.length() * 11 / 10 );
		for (int i = 0, limit = globString.length(); i < limit; i += 1) {
			char ch = globString.charAt(i);
			if (ch == '*') re.append('(').append('.').append('*').append(')');
			else if ("[].*+()?^$\\".indexOf(ch) > -1) re.append("\\").append(ch);
			else re.append(ch);
		}
		String result = re.toString();
		return result;
	}	
	
	public static boolean isPathSeparator(char ch) {
		return ch == '/' || ch == '\\';
	}
	
	/**
	    Split a file into components such that the components are differences in
	    reverse order of repeated getParentFile() calls, with file separators
	    stripped if present.
	*/
	public List<String> splitFilePath(File f) {
		ArrayList<String> segments = new ArrayList<String>();
		File af = fs.getCanonicalFile(f);
		split( segments, af, fs.getCanonicalPath(af) );
		return segments;
	}

	private void split( ArrayList<String> segments, File f, String fullPath ) {
		File parent = f.getParentFile();
		if (parent == null) {
			segments.add( fullPath );
		} else {
			String partPath = parent.getPath();
			split( segments, parent, partPath );
			String x = fullPath.substring( partPath.length() );
			if (x.startsWith(File.separator)) x = x.substring(1);
			segments.add( x );
		}
	}
	
	/**
	    A FileSystemInterface provides an interface onto a file system
	    that allows querying for a name being a directory name and extracting
	    from a directory a list of names that match a given pattern.
	*/
	public interface FileSystemInterface {
		
		/**
		    Returns true if f names a directory.
		*/
		public boolean isDirectory(File f);

		/**
		    Return a canonical form of the file.
		*/
		public File getCanonicalFile(File f);
		
		/**
		    Return the canonical path of the file.
		*/
		public String getCanonicalPath(File f);
		
		/**
		    Returns an array of files that appear in the directory f and
		    which pass the given filename filter.
		*/
		public File [] listFiles(File f, FilenameFilter ff);
		
		/**
		    fsInstance is a FileSystemInterface that uses the .isDirectory and
		    .listFiles methods of File for implementation. 
		*/
		public static final FileSystemInterface fsInstance = new FileSystemInterface() {

			@Override public boolean isDirectory(File f) {
				return f.isDirectory();
			}

			@Override public File[] listFiles(File f, FilenameFilter ff) {
				return f.listFiles(ff);
			}

			@Override public File getCanonicalFile(File f) {
				try { return f.getCanonicalFile(); }
				catch (IOException e) { return f; }
			}

			@Override public String getCanonicalPath(File f) {
				try { return f.getCanonicalPath(); }
				catch (IOException e) { return f.getPath(); }
			}
			
		};
	}
}