package com.ldodds.slug.framework;

import java.util.List;

/**
 * A version of the {@link Controller} class that 
 * applies one or more {@link TaskFilter}s to {@link Task}s 
 * added by its {@link Worker}s 
 *  
 * @author Leigh Dodds
 */
public class FilteringController extends Controller {

	private DelegatingTaskFilterImpl _filter;
	
	/**
	 * @param workItems
	 * @param factory
	 * @param numberOfWorkers
	 * @param monitor
	 */
	public FilteringController(List workItems, WorkerFactory factory,
			int numberOfWorkers, Monitor monitor) {
		super(workItems, factory, numberOfWorkers, monitor);
		_filter = new DelegatingTaskFilterImpl();
	}

	public synchronized void addWorkItem(Task workItem) 
	{
		if ( _filter.accept(workItem) )
		{		
			super.addWorkItem(workItem);
		}
		else
		{
			_logger.warning("Filtered work item: " + workItem);
		}
	}
	
	public synchronized void addFilter(TaskFilter filter)
	{
		_filter.addFilter(filter);
	}
}
