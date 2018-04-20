package com.epimorphics.lda.namespace_on_site.test;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.Test;

import com.epimorphics.lda.vocabularies.ELDA_API;

public class TestEldaOntologyOnTheWeb {

	static final String URL = ELDA_API.NS;
	static final String DOWNFILE = "/tmp/elda-downloaded.ttl";
	static final String ELDAVOCAB = "../vocabs/elda_api.ttl";
	
	@Test public void testOnWeb() throws IOException {
		
		boolean ignored = generatesOutput(new String[] {"wget", "-O", DOWNFILE, URL});	
		boolean different = generatesOutput(new String []{"cmp", ELDAVOCAB, DOWNFILE});
		
		if (different) {
			String message = URL + " is either mssing or different from " + ELDAVOCAB;
			System.err.println("\n\n" + message + "\n\n");
			// fail(message);
		}
		
	}
	
	protected boolean generatesOutput(String [] command) throws IOException {
		String s = null;
		boolean generatesOutput = false;
		Process p = Runtime.getRuntime().exec(command);
		BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
		BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		while ((s = in.readLine()) != null) generatesOutput = true;
		while ((s = err.readLine()) != null) {}
		return generatesOutput;
	}
}
