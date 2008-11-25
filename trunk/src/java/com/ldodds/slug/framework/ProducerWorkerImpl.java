package com.ldodds.slug.framework;


/**
 * @author ldodds
 */
public abstract class ProducerWorkerImpl extends WorkerImpl implements Producer
{
    private Consumer _consumer;
    private Task _workItem;
    
    public ProducerWorkerImpl(String name)
    {
        super(name);
    }
    
    
	/**
	 * @see com.ldodds.slug.framework.Producer#addConsumer(com.ldodds.slug.framework.Consumer)
	 */
	public void setConsumer(Consumer consumer)
	{
		_consumer = consumer;
	}

    protected abstract Object doTask(Task workItem);
    
	/**
	 * @see java.lang.Runnable#run()
	 */
	public void run()
	{
		_logger.finest( getName() + " running");
		
		while (true)
		{
	        //_workItem = _controller.popWorkItem(this);;
			_workItem = null;
			
	        while (_workItem == null )
	        {
	            try
	            {
	            	//_logger.finest( getName() + " waiting for task");
	            	Thread.sleep(1000);
	            	if (_shouldStop)
	            	{
	            		break;          
	            	}	            	
	            } catch (InterruptedException ie)
	            {
	                //try again
	            }
		        _workItem = _controller.popWorkItem(this);		        
	        }
	        
	        //left the above loop either because there's something
	        //to do, or we should stop
	        if (_workItem != null)
	        {
				//_monitor.startingTask(this, _workItem);
				Object results = doTask(_workItem);
				//_logger.finest( getName() + " notifying consumer");
				if (results != null)
				{
					_consumer.consume(_workItem, results);
				}
				//TODO note error?
				//_monitor.completedTask(this, _workItem);
				_controller.completedTask(this, _workItem);
	        }
	        if (_shouldStop)
	        {
	        	break;
	        }
		}        
	}
}
