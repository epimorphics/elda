package com.epimorphics.lda.testing.rendering.json;

import java.io.IOException;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.jena.atlas.json.*;
import org.junit.Test;

import com.epimorphics.lda.systemtest.Util;
import com.epimorphics.lda.testing.utils.TomcatTestBase;

/**
	Tests that multiValued declarations in the config are respected
	in the generated JSON.
	
	We do so by rendering the model into JSON and then walking
	each item in the model. The walk will terminate because 
	JSON values are trees, not graphs. If during the walk we
	find a multiValued property whose value is not an array,
	or we find	a non-multi-valued property with multiple values that isn't
	one of a selected set, we report a failure.
*/
public class TestNonLD_JSONRendering extends TomcatTestBase {

	@Override public String getWebappRoot() {
		return "src/main/webapp";
	}

	protected List<String> problems = new ArrayList<String>();
	
	protected Set<String> singleValueSeen = new HashSet<String>();
	
	@Test public void testRespectsMultipleValueAnnotations() throws ClientProtocolException, IOException {
		Util.CheckContent checkMultipleValues = new Util.CheckContent() {
			
			@Override public String failMessage() {
				return StringUtils.join(problems, "\n");
			}
			
			@Override public boolean check(String s) {
				JsonValue jv = JSON.parseAny(s);
				JsonArray ja = jv
					.getAsObject().get("result")
					.getAsObject().get("items")
					.getAsArray()
					;
				for (JsonValue i: ja) nest(i);
				return problems.isEmpty();
			}
		};
		
		Util.testHttpRequest("games.json", 200, checkMultipleValues);
	}
	
	protected void nest(JsonValue v) {
		if (v.isObject()) {
			JsonObject o = v.getAsObject();
			for (Map.Entry<String, JsonValue> e: o.entrySet()) {
				JsonValue value = e.getValue();
				String key = e.getKey();
				nest(value);
				boolean isArray = value.isArray();
				if (definedAsMultiValued(key)) {
					if (!isArray) problems.add("expected array for multi-value property " + key);
				} else if (isArray) {
					if (permitsMultipleValues(key)) {
						if (value.getAsArray().size() == 1) {
							problems.add("single value represented as array for plain property " + key);
						}
					} else {
						problems.add("expected non-array for plain property " + key);
					}
				}
			}
				
		} else if (v.isArray()) {
			JsonArray a = v.getAsArray();
			for (int i = 0; i < a.size(); i += 1)
				nest(a.get(i));
		}
	}
	
	// properties that are declared as multi-valued
	private boolean definedAsMultiValued(String key) {
		return key.equals("type") || key.equals("label");
	}

	// properties that are not declared multi-valued but are permitted to
	// (and actually do, in our sample data) have multiple values.
	private boolean permitsMultipleValues(String key) {
		return key.equals("players") || key.equals("designedBy");
	}
}
