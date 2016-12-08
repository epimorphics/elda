/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
 */

package com.epimorphics.lda.bindings;

import java.net.URL;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.exceptions.EldaException;
import com.epimorphics.lda.log.ELog;
import com.epimorphics.lda.rdfq.Value;
import com.epimorphics.lda.support.MultiMap;

/**
	A Bindings maps variables (identified by their string names) to their Value
	(a lexical form with type & language annotation). It also has a set of
	parameter names, which correspond to the query parameter names, and can map
	partial resource names to their URLs.
<br>	
	Bindings string-like values may contain variable expansion markers
	`{name}`. When the value of a variable is fetched (using get) then
	such sequences are replaced by the value of the named variable.
	The special sequence {\ represents an { that is not part of an expansion.
*/
public class Bindings implements Lookup {
	static Logger log = LoggerFactory.getLogger(Bindings.class);

	protected final Map<String, Object> vars = new HashMap<String, Object>();

	protected final Set<String> parameterNames = new HashSet<String>();

	protected final URLforResource ufr;

	public Bindings(Bindings initial, Set<String> parameterNames, URLforResource ufr) {
		this.ufr = ufr;
		this.putAll(initial);
		this.parameterNames.addAll(parameterNames);
	}

	public Bindings(Bindings initial, URLforResource ufr) {
		this(initial, initial.parameterNames, ufr);
	}

	public Bindings(Set<String> parameterNames, Bindings initial) {
		this(initial, parameterNames, initial.ufr);
	}

	public Bindings(Bindings initial) {
		this(initial, initial.parameterNames, initial.ufr);
	}

	public Bindings(URLforResource ufr) {
		this.ufr = ufr;
	}

	public Bindings() {
		this.ufr = URLforResource.alwaysFails;
	}

	public Bindings(Bindings bindings, Set<String> parameterNames) {
		this(bindings, parameterNames, bindings.ufr);
	}

	/**
		Answer a copy of this Bindings with the given defaults.
	*/
	public Bindings copyWithDefaults(Bindings defaults) {
		Bindings result = new Bindings(defaults, this.parameterNames(), ufr);
		result.putAll(this);
		return result;
	}

	/**
		Answer a copy of this ValValues. Changes to the copy do not affect this
		Bindings.
	*/
	public Bindings copy() {
		return new Bindings(this);
	}

	/**
		Answer a new Bindings based on <code>bindings</code> with additional
		bindings from the query parameters. Query parameters that do not
		correspond to existing bindings are treated as plain literals.
	*/
	public static Bindings createContext(Bindings bindings, MultiMap<String, String> queryParams) {
		Bindings cc = new Bindings(bindings, queryParams.keySet());
		for (String name : queryParams.keySet()) {
			Set<String> values = queryParams.getAll(name);
			if (values.size() > 1)
				EldaException.BadRequest("Multiple values for parameter '" + name + "': feature not implemented.");
			Value basis = cc.get(name);
			if (basis == null)
				basis = Value.emptyPlain;
			cc.put(name, basis.replaceBy(values.iterator().next()));
		}
		return cc;
	}

	/**
		Add all the entries from <code>other</code> to this ValValues,
		overwriting any existing bindings with the same names. Answer this
		Bindings.
	*/
	public Bindings putAll(Bindings other) {
		vars.putAll(other.vars);
		return this;
	}

	/**
		Associate the arbitrary <code>value</code> with the specified
		<code>key</code>. Return this Bindings.
	*/
	public Bindings putAny(String key, Object value) {
		vars.put(key, value);
		return this;
	}

	/**
		Answer the set of variable names bound in this Bindings.
	*/
	public Set<String> keySet() {
		return vars.keySet();
	}

	/**
		Answer the set of parameter names (a subset of the variable names).
	*/
	public Set<String> parameterNames() {
		return new HashSet<String>(parameterNames);
	}

	/**
		Answer the Value of the variable <code>name</code> in this Bindings, or
		null if it is not bound. If the value is not of type Value, convert it
		to one if it's a String, and otherwise return null.
	*/
	public Value get(String name) {
		return getValue(name);
	}
	
	/**
		getUnslashed returns a Value whose spelling is that of the
		value bound to name, with any {\ replaced by {. This provides
		an unquoting operation for {\ sequences introduced to escape {
		in raw strings. If there's no binding for name, getUnslashed
		returns null. 
		
		<p>"getUnbackslashed" just felt clumsy.</p>
	*/
	public Value getUnslashed(String name) {
		Value v = get(name);
		if (v == null) return null;
		return v.replaceBy(v.spelling().replace("{\\}", "{"));
	}
	
	/**
		Return whatever value is associated with the given <code>key</code>,
		or null if there isn't one.
	*/
	public Object getAny(String name) {
		return vars.get(name);
	}

	private Value getValue(String name) {
		Value v = getUnexpandedValue(name);
		return v == null ? null : evaluate(name, v, new ArrayList<String>());
	}

	private Value getUnexpandedValue(String name) {
		Object o = vars.get(name);
		if (o instanceof Value) {
			Value v = (Value) o;
			return v;
		} else if (o instanceof String) {
			String s = (String) o;
			return new Value(s);
		} else {
			return null;
		}
	}

	/**
		Answer the lexical form of the value of the variable <code>name</code> in
		this Bindings, or null if it is not bound. Part of the implementation of
		<code>Lookup</code>.
	*/
	@Override public String getValueString(String name) {
		Value v = get(name);
		return v == null ? null : v.spelling();
	}

	/**
		Answer the lexical form of the value of the variable <code>name</code>,
		or the value of <code>ifAbsent</code> if it is not bound.
	*/
	public String getAsString(String name, String ifAbsent) {
		return vars.containsKey(name) ? get(name).spelling() : ifAbsent;
	}

	/**
		Bind <code>name</code> to a LiteralNode which is a plain string with the
		given <code>valueString</code> as its lexical form. Any existing binding
		for <code>name</code> is discarded. Answer this Bindings.
	*/
	public Bindings put(String name, String valueString) {
		return put(name, new Value(valueString));
	}

	/**
		Bind <code>name</code> to the value <code>v</code>. Discard any existing
		binding for <code>name</code>. Answer this Bindings.
	*/
	public Bindings put(String name, Value v) {
		vars.put(name, v);
		return this;
	}

	/**
		Answer the URL for the resource named with the given partial path. If
		there is no such URL, an exception is thrown.
	*/
	public URL pathAsURL(String path) {
		return ufr.asResourceURL(path);
	}

	/**
		Answer a String which displays the content of this Bindings.
	*/
	@Override public String toString() {
		return "<bindings " + parameterNames + "|" + vars.toString() + ">";
	}

	/**
		Compare this binding with the other one, forcing evaluations along the
		way.
	*/
	public boolean sameValuesAs(Bindings other) {
		Set<String> keys = vars.keySet();
		if (!keys.equals(other.vars.keySet()))
			return false;
		for (String key : keys)
			if (!get(key).equals(other.get(key)))
				return false;
		return true;
	}

	private Value evaluate(String name, Value v, List<String> seen) {
		String vs = v.spelling();
		if (vs == null || vs.indexOf('{') < 0)
			return v;
		String expanded = expandVariables(vs, seen);
		Value newV = v.replaceBy(expanded);
		vars.put(name, newV);
		return newV;
	}

	public String expandVariables(String s, List<String> seen) {
		int start = 0;
		StringBuilder sb = new StringBuilder();		
		while (true) {
			int lb = s.indexOf('{', start);
			if (lb < 0)
				break;
			
			sb.append(s.substring(start, lb));

			// assume char after \ is }
			if (s.charAt(lb + 1) == '\\') {
				sb.append("{\\}");
				start = lb + 3;
				continue;
			}
			
			int rb = s.indexOf('}', lb);
			String name = s.substring(lb + 1, rb);

			if (seen.contains(name))
				throw new RuntimeException("circularity involving: " + seen);

			Value thisV = getUnexpandedValue(name);

			// Patch to allow missing variables (which blow up evaluate, so
			// we're bypassing for now).
			if (thisV == null) {
				sb.append("{").append(name).append("}");
				// issue #177
				log.debug(ELog.message("variable '%s' has no value, not substituted", name));
			} else {
				seen.add(name);
				Value v = evaluate(name, thisV, seen);
				seen.remove(seen.size() - 1);
				String value = v.spelling(); // values.getStringValue( name );
				if (value == null) {
					sb.append("{").append(name).append("}");
					// issue #177
					log.debug(ELog.message("variable '%s' has no value, not substituted", name));
				} else
					sb.append(value);
			}
			start = rb + 1;
		}
		sb.append(s.substring(start));
		return sb.toString();
	}

	/**
		Expands the string <code>s</code> by replacing any occurrence of
		{wossname} by the value of wossname as given by the Lookup
		<code>values</code>.
	*/
	public static String expandVariables(Lookup values, String s) {
		int start = 0;
		StringBuilder sb = new StringBuilder();
		while (true) {
			int lb = s.indexOf('{', start);
			if (lb < 0)
				break;
			
			sb.append(s.substring(start, lb));

			if (s.charAt(lb + 1) == '\\') {
				sb.append("{\\}");
				start = lb + 3;
				continue;
			}
			
			int rb = s.indexOf('}', lb);
			String name = s.substring(lb + 1, rb);
			String value = values.getValueString(name);
			if (value == null) {
				sb.append("{").append(name).append("}");
				// issue #177
				log.debug(ELog.message("variable '%s' has no value, not substituted", name));
			} else
				sb.append(value);
			start = rb + 1;
		}
		sb.append(s.substring(start));
		return sb.toString();
	}

	/**
		Answer a new Bindings constructed from the given map by converting the
		values into a string-valued Value.
	*/
	public static Bindings uplift(Map<String, String> bindings) {
		Bindings result = new Bindings();
		for (String key : bindings.keySet())
			result.put(key, new Value(bindings.get(key)));
		return result;
	}

	public String expandVariables(String val) {
		return expandVariables(this, val);
	}

	/**
		Update all the entries in this Bindings to have the values that are
		mentioned in update.
	*/
	public Bindings updateAll(Map<String, String> update) {
		for (Map.Entry<String, String> e : update.entrySet()) {
			String name = e.getKey();
			Value v = this.get(name);
			this.put(name, (v == null ? new Value(e.getValue()) : v.replaceBy(e.getValue())));
		}
		return this;
	}
}