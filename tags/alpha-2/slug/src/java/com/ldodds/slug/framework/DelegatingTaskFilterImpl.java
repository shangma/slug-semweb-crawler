package com.ldodds.slug.framework;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Resource;

public class DelegatingTaskFilterImpl implements TaskFilter
{
	private List _filters;
	private boolean _configured;
	
	public DelegatingTaskFilterImpl() 
	{
		_filters = new ArrayList();
		_configured = false;
	}

	public DelegatingTaskFilterImpl(List filters)
	{
		_filters = filters;
	}
	
	public void addFilter(TaskFilter filter)
	{
		_filters.add( filter );
	}
	public boolean accept(Task task) 
	{
    	for (Iterator iter = _filters.iterator(); iter.hasNext();)
        {
            TaskFilter filter = (TaskFilter) iter.next();
            if (!filter.accept(task))
            {
            	return false;
            }
        }
    	return true;
	}

	public void configure(Resource self) 
	{
    	for (Iterator iter = _filters.iterator(); iter.hasNext();)
        {
            TaskFilter filter = (TaskFilter) iter.next();
            filter.configure(self);
        }
    	_configured = true;    	
	}

	public boolean isConfigured()
	{
		return _configured;
	}
}
