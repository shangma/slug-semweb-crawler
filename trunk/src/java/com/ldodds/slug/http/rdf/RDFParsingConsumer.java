package com.ldodds.slug.http.rdf;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;

import com.hp.hpl.jena.rdf.model.*;

import com.ldodds.slug.framework.Result;
import com.ldodds.slug.http.LoggingErrorHandler;
import com.ldodds.slug.http.Response;
import com.ldodds.slug.http.URLTask;
import com.ldodds.slug.http.URLTaskImpl;
import com.ldodds.slug.http.scanner.Scanner;
import com.ldodds.slug.http.scanner.SeeAlsoScanner;
import com.ldodds.slug.vocabulary.CONFIG;

/**
 * Implementation of the Consumer interface that provides support for parsing 
 * RDF/XML fetched during the crawl.
 * 
 * @author ldodds
 * @see com.ldodds.slug.http.scanner.Scanner
 * TODO: move out behaviour that can rely on context
 */
public class RDFParsingConsumer extends AbstractRDFConsumer {
	
	/**
	 * Attempt to parse the results of the task as RDF. If the Content-Type 
	 * header in the response is not set, or is not "application/rdf+xml" then 
	 * the response will not be processed.
	 * 
	 * @param task the task that generated the response
	 * @param response the response
	 * @param baseURL the base URL
	 * @return the model to be used in subsequent processing.
	 */
	protected Model getModel(URLTask task, Response response, String baseURL) {
		if ( !canProcess(response) ) {
			getLogger().log(Level.FINE, "Cannot parse response of format: " + response.getContentType());
			return null;
		}
		
		Model model = ModelFactory.createDefaultModel();
		RDFReader reader = getReader(response, model);
		
		configureReader(reader);
		
		LoggingErrorHandler errorHandler = new LoggingErrorHandler(task.getURL()
				.toString());
		
		reader.setErrorHandler(errorHandler);
		reader.read(model, new StringReader(response.getContent()
				.toString()), baseURL);
		
		if ( errorHandler.isError() ) {
			getLogger().log(Level.SEVERE, "Error processing RDF, not adding Model to context");
			return null;
		}
		return model;
	}

	/**
	 * Can the consumer parse this response?
	 * 
	 * @param response
	 * @return true if this can be parsed, false otherwise
	 */
	protected boolean canProcess(Response response) {
		return "application/rdf+xml".equals( response.getContentTypeWithoutCharset() );
	}
	
	/**
	 * Return an RDFReader instance suitable for parsing this data. Default implementation 
	 * just returns an RDF/XML parser
	 * 
	 * @param model
	 * @return
	 */
	protected RDFReader getReader(Response response, Model model) {
		return model.getReader();
	}
	
	/**
	 * Can be overridden by sub-classes to configure the RDFReader before it is used to 
	 * parse an RDF/XML document.
	 *
	 * The default implementation does nothing.
	 * 
	 * @param reader
	 */
	protected void configureReader(RDFReader reader) {
		
	}
}
