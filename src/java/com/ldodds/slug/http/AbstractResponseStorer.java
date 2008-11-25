package com.ldodds.slug.http;

import java.util.logging.Logger;

import com.hp.hpl.jena.rdf.model.Resource;
import com.ldodds.slug.framework.Consumer;
import com.ldodds.slug.framework.Controller;
import com.ldodds.slug.framework.Task;
import com.ldodds.slug.framework.config.ComponentImpl;
import com.ldodds.slug.framework.config.Memory;

public abstract class AbstractResponseStorer extends ComponentImpl implements Consumer
{
	//used to say where the local copy of the file is
	protected Controller _controller;
	protected Logger _logger;
	protected Memory _memory;

	public AbstractResponseStorer() 
	{
		super();
		_logger = Logger.getLogger(getClass().getPackage().getName());
	}

    public void consume(Task workItem, Object results)
    {
    	if (results == null)
    	{
    		return;
    	}
    	
    	try
    	{    	
			Response response = (Response)results;
			Resource representation =
				_memory.getRepresentation( response.getRequestURL() );
			if (representation == null)
			{
				return;
			}
			
			store(representation, response);
			
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

    abstract void store(Resource resource, Response response) throws Exception;
}
