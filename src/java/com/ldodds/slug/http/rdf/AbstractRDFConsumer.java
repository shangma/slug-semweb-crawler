package com.ldodds.slug.http.rdf;

import java.util.logging.Level;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.ldodds.slug.framework.ConsumerImpl;
import com.ldodds.slug.framework.Result;
import com.ldodds.slug.framework.Task;
import com.ldodds.slug.http.Response;
import com.ldodds.slug.http.URLTask;
import com.ldodds.slug.vocabulary.SCUTTERVOCAB;

/**
 * Abstract base class for consuming components that parse and process RDF data
 * 
 * Sub-classes should implement the getModel method to provide the RDF model 
 * associated with the results of this Task. The processModel method can also be 
 * overridden in order to provide additional custom behaviour.
 * 
 * The class will also ensure that the generated RDF Model is stored as additional 
 * context in the current Result object, enabling additional "downstream" consumers 
 * to rely directly on the Jena model and not have to repeatedly process the 
 * data.
 * 
 * @author ldodds
 */
public abstract class AbstractRDFConsumer extends ConsumerImpl {

	/**
	 * Context URI for storing and accessing RDF Model data in a Result object.
	 */
	public static final String RDF_MODEL = "http://purl.org/NET/schemas/slug/context/RDFModel";
	
	public void consume(Task workItem, Result result) {
		URLTask urlWorkItem = (URLTask) workItem;
		Response response = (Response) result;
	
		//Find the RDF resource (of rdf:type Representation) that has 
		//a scutter:source property of that url 
		Resource representation = getMemory()
				.getRepresentation(urlWorkItem.getURL());
	
		if (representation == null) {
			return;
		}
	
		//Now find the source property for that resource, and use that
		//as our base URL
		//FIXME: So this is always the same as the workItem url...
		//String baseURL = representation.getProperty(SCUTTERVOCAB.source).getString();
		String baseURL = urlWorkItem.getURL().toString();
		
		try {
			
			Model model = getModel(urlWorkItem, response, baseURL);
				
			if (model != null) {
				processModel(urlWorkItem, result, representation, model);
			}
			
		} catch (Exception e) {
			getLogger().log(Level.SEVERE, "Exception when consuming response as RDF", e);
		}
	}

	/**
	 * Update the Scutter memory with information and/or statistics about the retrieved 
	 * data. The default implementation simply adds the raw triple count.
	 *
	 * Override this method to carry out additional updates
	 * 
	 * @param representation the Resource referencing the work item in the Scutter memory
	 * @param model the parsed response data
	 */
	protected void updateMemory(Resource representation, Model model) {
		memory.addRawTripleCount(representation, model.size());
	}
	
	/**
	 * Attempt to parse the results of the task as RDF.
	 * 
	 * Sub-classes should implement this method to provide the parsing behaviour to generate 
	 * a Model from the returned data, e.g. based on the media type in the response.
	 * 
	 * @param task the task that generated the response
	 * @param response the response
	 * @param baseURL the base URL
	 * @return the model to be used in subsequent processing. If null, then no further processing is carried out
	 */
	protected abstract Model getModel(URLTask task, Response response, String baseURL);
	
	/**
	 * Process the generated RDF model.
	 * 
	 * Sub-classes can override this method to carry out custom behaviour, e.g. to carry out 
	 * additional post-processing of the model. The default implementation updates the 
	 * scutter memory and stores the results of the parsing.
	 * 
	 * @param task the task that generated the response data
	 * @param representation reference to the information about the task in the Scutter memory
	 * @param model the generated Model
	 */
	protected void processModel(URLTask task, Result result, Resource representation, Model model) {
		updateMemory(representation, model);
		addToContext(result, model);
	}	

	/**
	 * Adds the provided model as context to the Result object.
	 * 
	 * @param result
	 * @param model
	 */
	protected void addToContext(Result result, Model model) {
		result.addContext(getContextURI(), model);
	}

	/**
	 * @return the context uri for storing the parsed Model.
	 */
	protected String getContextURI() {
		return RDF_MODEL;
	}
	
}