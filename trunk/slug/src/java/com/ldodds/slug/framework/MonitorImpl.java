package com.ldodds.slug.framework;

import java.util.*;
import java.util.logging.*;

/**
 * Implementation of the {@link Monitor} interface.
 * @author ldodds
 */
public class MonitorImpl implements Monitor
{
    private Map _activeWorkers;
    private Logger _logger; 
    private int _completedTasks;
    
    public MonitorImpl()
    {
        _activeWorkers = new HashMap();
        _logger = Logger.getLogger(getClass().getPackage().getName());
        _completedTasks = 0;
    }
    
	/**
	 * @see com.ldodds.slug.framework.Monitor#completedTask(com.ldodds.slug.framework.Worker, java.lang.Object)
	 */
	public synchronized void completedTask(Worker worker, Task workItem)
	{
		_logger.finest(worker.getName() + " completed task");
		_activeWorkers.remove(worker);
		_completedTasks++;
		if (_completedTasks % 1000 == 0)
		{
			_logger.info(_completedTasks + " completed so far");
		}
	}

	/**
	 * @see com.ldodds.slug.framework.Monitor#getNumberOfActiveWorkers()
	 */
	public synchronized int getNumberOfActiveWorkers()
	{
		return _activeWorkers.keySet().size();
	}

	public synchronized Map getActiveWorkers()
	{
		return Collections.unmodifiableMap(_activeWorkers);
	}
	
	/**
	 * @see com.ldodds.slug.framework.Monitor#startingTask(com.ldodds.slug.framework.Worker, java.lang.Object)
	 */
	public synchronized void startingTask(Worker worker, Task workItem)
	{
		_logger.finest(worker.getName() + " starting task");
		_activeWorkers.put(worker, workItem);
	}

}
