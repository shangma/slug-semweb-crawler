package com.ldodds.slug.framework;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import com.ldodds.slug.framework.config.Memory;
import com.ldodds.slug.framework.config.ComponentImpl;

/**
 * <p>
 * Implementation of the {@link Consumer} interface that 
 * supports delegating processing of work item results to 
 * a List of Consumers.
 * </p>
 * <p>
 * Useful when multiple actions should be taken when a particular 
 * work item is completed.
 * </p>
 * @author ldodds
 */
public class DelegatingConsumerImpl extends ComponentImpl implements Consumer
{
	private List _consumers;
	private Memory _memory;	
	
	public DelegatingConsumerImpl()
	{
		_consumers = new ArrayList();	
	}
	
	public DelegatingConsumerImpl(List consumers)
	{
		_consumers = consumers;
			
	}
	
    public void consume(Task workItem, Object results)
    {
    	for (Iterator iter = _consumers.iterator(); iter.hasNext();)
        {
            Consumer element = (Consumer) iter.next();
            try
            {            
            	element.consume(workItem, results);
            } catch (Exception e)
            {
            	e.printStackTrace();
            }
        }
    }
	
	public void addConsumer(Consumer consumer)
	{
		_consumers.add(consumer);
	}

    public void setController(Controller controller)
    {
		for (Iterator iter = _consumers.iterator(); iter.hasNext();)
		{
			Consumer element = (Consumer) iter.next();
			element.setController(controller);
		}

    }
    
    public void setMemory(Memory memory)
    {
        for (Iterator iter = _consumers.iterator(); iter.hasNext();)
        {
            Consumer element = (Consumer) iter.next();
            element.setMemory(memory);
        }
        
    }
     
}
