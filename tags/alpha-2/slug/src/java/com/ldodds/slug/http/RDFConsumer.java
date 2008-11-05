package com.ldodds.slug.http;

import java.io.*;
import java.net.URL;
import java.util.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;

import com.ldodds.slug.framework.Consumer;
import com.ldodds.slug.framework.Controller;
import com.ldodds.slug.framework.Task;
import com.ldodds.slug.framework.config.Memory;
import com.ldodds.slug.framework.config.ComponentImpl;
import com.ldodds.slug.vocabulary.SCUTTERVOCAB;

/**
 * @author ldodds
 *
 */
public class RDFConsumer extends ComponentImpl implements Consumer
{
	private Memory _memory;
	private Controller _controller;

    public void consume(Task workItem, Object results)
    {
        URLTask urlWorkItem = (URLTask)workItem;
        Response response = (Response)results;
        
		Resource representation = 
			_memory.getRepresentation( urlWorkItem.getURL() );
        
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
			
            _memory.addRawTripleCount(representation, model.size() );
			
			Set newURLs = ModelUtils.getSeeAlsos(model, new HashSet());
			Iterator iter = newURLs.iterator();
			while (iter.hasNext())
			{
				URL next = (URL)iter.next();
                
				Resource rep = _memory.getOrCreateRepresentation(next, urlWorkItem.getURL());
				
                if (_memory.canBeFetched( rep, _controller.getStarted() ))
                {
                    _controller.addWorkItem( new URLTaskImpl(urlWorkItem, next) );
                }
                
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
    }

    public void setController(Controller controller)
    {
    	_controller = controller;
    }
    
    public void setMemory(Memory memory)
    {
        _memory = memory;
    }
}
