package com.epimorphics.lda.support.tests;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.epimorphics.lda.support.Glob;
import com.epimorphics.util.CollectionUtils;

import org.junit.Test;

public class TestGlob {
	
	@Test public void testMatchingA() {
		File f1 = new File( "./dir" ), f2 = new File( "./dir/eSTARt" );
		File f3 = new File( "./dir/notme" ), f4 = new File( "." );
		Glob g = new Glob( new FakeFSI( f1, f2, f3, f4 ) );
		Set<File> files = new HashSet<File>( g.filesMatching( "dir/e*t" ) );
		assertEquals( CollectionUtils.set(f2), files );
	}
	
	@Test public void testMatchingB() {
		File f1 = new File( "./dir" ), f2 = new File( "./dir/eSTARt" );
		File f3 = new File( "./dir/notme" ), f4 = new File( "." );
		Glob g = new Glob( new FakeFSI( f1, f2, f3, f4 ) );
		Set<File> files = new HashSet<File>( g.filesMatching( "dir/*" ) );
		assertEquals( CollectionUtils.set(f2, f3), files );
	}
	
	@Test public void testMatchingAfterWildcard() {
		File f1 = new File( "./dir" ), f2 = new File( "./dir/A/leaf" );
		File f3 = new File( "./dir/B/leaf" ), f5 = new File("./dir/C/foliage");
		File f4 = new File( "." );
		Glob g = new Glob( new FakeFSI( f1, f2, f3, f4, f5 ) );
		Set<File> files = new HashSet<File>( g.filesMatching( "dir/*/leaf" ) );
		assertEquals( CollectionUtils.set(f2, f3), files );
	}
	
	@Test public void testMatchingMultipleWildcards() {
		File f1 = new File( "./dir" ), f2 = new File( "./dir/A/B/leaf" );
		File f3 = new File( "./dir/B/C/leaf" );
		File f4 = new File( "." );
		Glob g = new Glob( new FakeFSI( f1, f2, f3, f4 ) );
		Set<File> files = new HashSet<File>( g.filesMatching( "dir/*/*/leaf" ) );
		assertEquals( CollectionUtils.set(f2, f3), files );
	}
	
	static class FakeFSI implements Glob.FileSystemInterface {

		List<String> fileNames = new ArrayList<String>();
		
		public FakeFSI(File... init) {
			for (File f: init) fileNames.add( f.getPath() );
		}
		
		@Override public boolean isDirectory(File f) {
			String path = f.getPath() + "/";
			for (String fn: fileNames) if (fn.startsWith(path)) return true;
			return false;
		}

		@Override public File[] listFiles(File d, FilenameFilter ff) {
			String dn = d.getPath() + "/";
			int dl = dn.length();
			List<File> result = new ArrayList<File>();
			for (String fn: fileNames) 
				if (fn.startsWith(dn)) {
					String name = fn.substring(dl);
					if (ff.accept( d,  name )) {
						int slash = fn.indexOf('/', dl);
						if (slash > -1) fn = fn.substring(0, slash);
						result.add(new File(fn));
					}
				}
			return result.toArray( new File[result.size()] );
		}
	}

}
