package com.ldodds.slug.http;

import java.io.*;
import java.net.URL;
import java.util.*;

import com.hp.hpl.jena.rdf.model.*;

import com.ldodds.slug.framework.Consumer;
import com.ldodds.slug.framework.Controller;
import com.ldodds.slug.framework.Task;
import com.ldodds.slug.framework.config.Memory;
import com.ldodds.slug.framework.config.ComponentImpl;
import com.ldodds.slug.http.scanner.Scanner;
import com.ldodds.slug.http.scanner.SeeAlsoScanner;
import com.ldodds.slug.vocabulary.CONFIG;
import com.ldodds.slug.vocabulary.SCUTTERVOCAB;

/**
 * Implementation of the Consumer interface that supports parsing of RDF retrieved during the crawl.
 * 
 * The consumer does not store the parsed or raw data, this is the job of other consumers. This 
 * class simply adds the number of triples to the crawlers memory, and processes the RDF to extract 
 * <code>rdfs:seeAlso</code> links.
 * 
 * @author ldodds
 */
public class RDFConsumer extends ComponentImpl implements Consumer
{
	private Memory memory;
	private Controller controller;
	private Scanner scanner;
	
    public void consume(Task workItem, Object results)
    {
        URLTask urlWorkItem = (URLTask)workItem;
        Response response = (Response)results;
        
		Resource representation = 
			memory.getRepresentation( urlWorkItem.getURL() );
        
		if (representation == null)
		{
			return;
		}
		
		String baseURL = representation.getProperty(SCUTTERVOCAB.source).getObject().toString();
		
		Model model = ModelFactory.createDefaultModel();
		RDFReader reader = model.getReader();
		try
		{
		    reader.setErrorHandler(new LoggingErrorHandler( urlWorkItem.getURL().toString() ));
			reader.read( model, new StringReader( response.getContent().toString() ), baseURL);
			
            memory.addRawTripleCount(representation, model.size() );
			
			Set<URL> newURLs = findURLs(model, urlWorkItem.getURL());
			Iterator<URL> iter = newURLs.iterator();
			while (iter.hasNext())
			{
				URL next = iter.next();
                
				Resource rep = memory.getOrCreateRepresentation(next, urlWorkItem.getURL());
				
                if (memory.canBeFetched( rep, controller.getStarted() ))
                {
                    controller.addWorkItem( new URLTaskImpl(urlWorkItem, next) );
                }
                
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
    }

	/**
	 * Find URLs in the provided Model. These will be used to generate new work items.
	 * 
	 * @param model the model to scan for urls
	 * @return
	 */
	protected Set<URL> findURLs(Model model, URL origin) {
		return scanner.findURLs(model, origin);
	}

    public void setController(Controller controller)
    {
    	this.controller = controller;
    }
    
    public void setMemory(Memory memory)
    {
        this.memory = memory;
    }

	@Override
	protected boolean doConfig(Resource self) {
		if ( self.hasProperty( CONFIG.scanner) ) {
			scanner = (Scanner)instantiateReferenced(self, CONFIG.scanner);
		}
		else {
			scanner = new SeeAlsoScanner();
		}
		return super.doConfig(self);
	}
    
	protected Scanner getScanner() {
		return scanner;
	}
}
