package com.epimorphics.lda.restlets;

import java.io.*;
import java.util.List;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.core.View;
import com.epimorphics.lda.log.ELog;
import com.epimorphics.lda.renderers.velocity.VelocityRenderer;
import com.epimorphics.lda.renderers.velocity.VelocityRendering;
import com.epimorphics.lda.routing.ServletUtils;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.shortnames.StandardShortnameService;
import com.epimorphics.lda.shortnames.CompleteContext.Mode;
import com.epimorphics.lda.support.pageComposition.Messages;
import com.epimorphics.lda.vocabularies.API;
import com.epimorphics.lda.vocabularies.ELDA_API;
import com.epimorphics.util.CollectionUtils;
import com.epimorphics.util.MediaType;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDF;

public class ErrorPages {    

    protected static Logger log = LoggerFactory.getLogger(RouterRestlet.class);
    
	protected static final String fallBack = 
		"<html>"
		+ "\n<head><title>internal error</title></head>"
		+ "\n<body>"
		+ "\n<h1>Sorry</h1>"
		+ "\n<p>The server suffered an internal error.</p>"
		+ "\n</body>"
		+ "\n</html>"
		+ "\n"
		;
	
	public static Response respond(Bindings b, ServletContext con, String name, String message, int status) {	
		try {
	
		String context = con.getContextPath();
		String baseFilePath = ServletUtils.withTrailingSlash( con.getRealPath("/") );
		String basePrefix = name.startsWith("/") ? "" : baseFilePath;
		
		String[] filesToTry = new String[] {
			"/etc/elda/conf.d/" + context + "/_errors/" + name + ".vm"
			, basePrefix + "_errors/" + name + ".vm"
			, "/etc/elda/conf.d/" + context + "/_errors/" + "_error" + ".vm"
			, basePrefix + "_errors/" + "_error" + ".vm"
		};
		
		
		String page = fetchPage(filesToTry, fallBack);
		
		// Bind the exception message to _message. Quote any left braces to
		// allow them to be passed through possibly multiple Bindings evaluations
		// and eventually unquoted using Bindings::getUnslashed.
		if (message == null) message = "Odd, no additional information is available.";
		
		b.put("_message", 
			message
				.replaceAll("\\{", "{\\\\}")
				.replaceAll("&", "&amp;")
				.replaceAll("<", "&lt;")
				.replaceAll(">", "&gt;")
		);
		
		String builtPage = apply(b, page, name, message);
		
		return Response
			.status(status)
			.entity(builtPage)
			.build()
			;
		} catch (Throwable e) {
			log.error(ELog.message("an exception occurred when rendering an error page"));
			log.error(ELog.message("%s", e.getMessage()));
			return Response
				.status(Status.INTERNAL_SERVER_ERROR)
				.entity(fallBack)
				.build()
				;
		}
	}

	static String fetchPage(String [] fileNames, String ifAbsent) {
		for (String fileName: fileNames) {
			File f = new File(fileName);
			if (f.exists())
				return FileManager.get().readWholeFileAsUTF8(f.getAbsolutePath());
		}
		return ifAbsent;
	}

	static String apply(Bindings b, String template, String name, String message ) {
	//	System.err.println(">> template: " + template );
	//	System.err.println(">> name: " + name );
	//	System.err.println(">> message: " + message );
		
		String templateName = name + ".vm";
		
		try {
			return RenderTemplatePage(b, templateName);
		} catch (IOException e) {
			return Messages.protect(e.toString());
		} 
		
	//	if (message == null) message = "(no further information)";
	//	return template
	//		.replace("{{name}}", name)
	//		.replace("{{message}}", Messages.protect(message))
	//		;
	}
	
	static Resource root = ResourceFactory.createResource("eh:/root");

	static Model model = ModelFactory
		.createDefaultModel()
		.add(root, ELDA_API.velocityTemplate, "response.vm")
		;
	
	static Model graphModel = ModelFactory.createDefaultModel();
	
	static public String RenderTemplatePage(Bindings b, String templateName) throws IOException {
	
		MediaType mt = MediaType.TEXT_HTML;
		Resource config = model.createResource("eh:/root");
		Mode prefixMode = Mode.PreferPrefixes;
		ShortnameService sns = new StandardShortnameService();
			
		List<Resource> noResults = CollectionUtils.list(root.inModel(model));
		Graph resultGraph = graphModel.getGraph();
		
		resultGraph.getPrefixMapping().setNsPrefix("api", API.NS);
		resultGraph.add(Triple.create(root.asNode(), API.items.asNode(), RDF.nil.asNode()));
		
		APIResultSet rs = new APIResultSet(resultGraph, noResults, true, true, "details", View.ALL);
		VelocityRenderer vr = new VelocityRenderer(mt, null, config, prefixMode, sns);
		
		VelocityRendering vx = new VelocityRendering(b, rs, vr);
		
	    VelocityEngine ve = vx.createVelocityEngine();
	    VelocityContext vc = vx.createVelocityContext( b );
	    
	    ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    Writer w = new OutputStreamWriter( bos, "UTF-8" );
	    Template t = ve.getTemplate(templateName);
	    
	    t.merge(vc,  w);
	    w.close();
		
		return bos.toString();
	}

}
