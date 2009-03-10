package com.ldodds.slug.framework;

import java.util.*;
import java.util.logging.*;

/**
 * Implementation of the {@link Monitor} interface.
 * @author ldodds
 */
public class MonitorImpl implements Monitor
{
    private Map<Worker, Task> _activeWorkers;
    private Logger _logger; 
    private int _completedTasks;
    private Controller controller;
    
    public MonitorImpl()
    {
        _activeWorkers = new HashMap<Worker, Task>();
        _logger = Logger.getLogger(getClass().getPackage().getName());
        _completedTasks = 0;
    }
    
	/**
	 * @see com.ldodds.slug.framework.Monitor#completedTask(com.ldodds.slug.framework.Worker, java.lang.Object)
	 */
	public synchronized void completedTask(Worker worker, Task workItem)
	{
		_logger.finest(worker.getName() + " completed task " + workItem.getName() );
		_activeWorkers.remove(worker);
		_completedTasks++;
		
		if (_completedTasks % 1000 == 0)
		{
            _logger.log(Level.INFO, _completedTasks + " completed; " + getNumberOfActiveWorkers() + 
                    " of " + controller.getNumberOfWorkers() + " active workers; " + controller.getQueueSize() + " items queued");			
		}
	}

	/**
	 * @see com.ldodds.slug.framework.Monitor#getNumberOfActiveWorkers()
	 */
	public synchronized int getNumberOfActiveWorkers()
	{
		return _activeWorkers.keySet().size();
	}
	
	/**
	 * @see com.ldodds.slug.framework.Monitor#startingTask(com.ldodds.slug.framework.Worker, java.lang.Object)
	 */
	public synchronized void startingTask(Worker worker, Task workItem)
	{
		_logger.fine(worker.getName() + " starting task " + workItem.getName());
		_activeWorkers.put(worker, workItem);
	}

	public void setController(Controller controller) {
		this.controller = controller;
	}
}
