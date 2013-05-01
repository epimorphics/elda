package com.epimorphics.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import com.epimorphics.lda.query.APIQuery;

public class TemplateUtils {

	public static int countQ(String s) {
		int result = 0;
		for (int i = 0; i < s.length(); i += 1) 
			if (s.charAt(i) == '?') result += 1;
		return result;
	}

	public static List<String> splitTemplate(String template) {
		List<String> result = new ArrayList<String>( countQ( template ) + 1 );
		Matcher m = APIQuery.varPattern.matcher( template );
		int start = 0;
		while (m.find( start )) {
			result.add( template.substring( start, m.start() ) );
			result.add( m.group() );
			start = m.end();
		}
		result.add( template.substring( start ) ); 
		return result;
	}

}
