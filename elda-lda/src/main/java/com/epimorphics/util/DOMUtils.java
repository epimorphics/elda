/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
 */
package com.epimorphics.util;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.renderers.BytesOutTimed;
import com.epimorphics.lda.renderers.Renderer;
import com.epimorphics.lda.support.Times;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.WrappedException;

/**
 * Handles XSLT rewrites for HTML and indented-string display of XML.
 * 
 * @author chris
 */
public class DOMUtils {
	
	public static Document newDocument() {
		return getBuilder().newDocument();
	}

	public static String renderNodeToString(Times times, Node d, PrefixMapping pm) {
		Transformer t = setPropertiesAndParams(times, new Bindings(), pm, null);
		StringWriter sw = new StringWriter();
		StreamResult sr = new StreamResult(sw);
		try {
			t.transform(new DOMSource(d), sr);
		} catch (TransformerException e) {
			throw new WrappedException(e);
		}
		return sw.toString();
	}

	public static Renderer.BytesOut renderNodeToBytesOut
		( final Times t
		, final Document d
		, final Bindings rc
		, final PrefixMapping pm
		, final String transformFilePath
		) {
	//
		Transformer tr = setPropertiesAndParams(t, rc, pm, transformFilePath);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		OutputStreamWriter u = StreamUtils.asUTF8(os);
		StreamResult sr = new StreamResult(u);
		try {
			tr.transform(new DOMSource(d), sr);
			u.flush();
			u.close();
		} catch (TransformerException e) {
			throw new WrappedException(e);
		} catch (IOException e) {
			throw new WrappedException(e);
		}
		final String content = Renderer.UTF8.toString(os);
	//
		return new BytesOutTimed() {

			@Override public void writeAll(OutputStream os) {
				OutputStreamWriter u = StreamUtils.asUTF8(os);
				try {
					u.write(content);
					u.flush();
					u.close();
				} catch (IOException e) {
					throw new WrappedException(e);
				}
				// Transformer tr = setPropertiesAndParams( t, rc, pm,
				// transformFilePath );
				// OutputStreamWriter u = StreamUtils.asUTF8(os);
				// StreamResult sr = new StreamResult( u );
				// try {
				// tr.transform( new DOMSource( d ), sr );
				// u.flush();
				// u.close();
				// }
				// catch (TransformerException e)
				// { throw new WrappedException( e ); }
				// catch (IOException e)
				// { throw new WrappedException( e ); }
			}

			@Override protected String getFormat() {
				return "html";
			}
		};
	}

	static Logger log = LoggerFactory.getLogger(DOMUtils.class);

	public static Transformer setPropertiesAndParams
		(Times times, Bindings rc, PrefixMapping pm, String transformFilePath) {
		Transformer t = getTransformer(times, rc, transformFilePath);
		t.setOutputProperty(OutputKeys.INDENT, "yes");
		t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		for (String name : rc.keySet()) {
			String value = rc.getValueString(name);
			if (value == null) {
				log.debug("ignored null xslt parameter " + name);
			} else {
				t.setParameter(name, value);
				log.debug("set xslt parameter " + name + " = " + value);
			}
		}
		String nsd = namespacesDocument(pm);
		t.setParameter("api:namespaces", nsd);
		return t;
	}

	private static DocumentBuilder getBuilder() {
		try {
			return DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new WrappedException(e);
		}
	}

	public static synchronized void clearCache() {
		cache.clear();
	}

	protected static HashMap<URL, Templates> cache = new HashMap<URL, Templates>();

	private static Transformer getTransformer(Times times, Bindings rc,	String transformFilePath) {
		try {
			TransformerFactory tf = TransformerFactory.newInstance();
			if (transformFilePath == null)
				return tf.newTransformer();
			else
				synchronized (DOMUtils.class) {
					URL u = rc.pathAsURL(Bindings.expandVariables(rc, transformFilePath));
					Templates t = cache.get(u);
					if (t == null) {
						long origin = System.currentTimeMillis();
						t = tf.newTemplates(new StreamSource(u.toExternalForm()));
						long after = System.currentTimeMillis();
						times.setStylesheetCompileTime(after - origin);
						cache.put(u, t);
					}
					return t.newTransformer();
				}
		} catch (TransformerConfigurationException e) {
			throw new WrappedException(e.getMessage() + " ["
					+ transformFilePath + "]", e);
		}
	}

	private static String namespacesDocument(PrefixMapping pm) {
		StringBuilder sb = new StringBuilder();
		sb.append("<namespaces>\n");
		for (Map.Entry<String, String> e : pm.getNsPrefixMap().entrySet()) {
			sb.append("<namespace prefix='");
			sb.append(e.getKey());
			sb.append("'>");
			sb.append(e.getValue());
			sb.append("</namespace>\n");
		}
		sb.append("</namespaces>\n");
		return sb.toString();
	}
}
