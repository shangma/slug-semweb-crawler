package com.ldodds.slug.http;

import com.ldodds.slug.framework.*;
import com.ldodds.slug.framework.config.Memory;

/**
 * @author ldodds
 */
public class URLRetrievalWorkerFactory extends WorkerFactoryImpl
{
  
    private Consumer _consumer;
	private Memory _memory;
	        
	/**
	 * @see com.ldodds.slug.framework.WorkerFactoryImpl#createWorker(java.lang.String)
	 */
	protected Worker createWorker(String name)
	{
        URLRetrievalWorker worker = new URLRetrievalWorker(_memory, name);
        worker.setConsumer(_consumer); 
        return worker;
	}


    public void setConsumer(Consumer consumer)
    {
        _consumer = consumer;        
    }

	public void setMemory(Memory memory)
	{
		_memory = memory;
	}
    
    public void setController(Controller controller)
    {
        super.setController(controller);
        if (_consumer != null)
        {
        	_consumer.setController(controller);
        }
    }

}
