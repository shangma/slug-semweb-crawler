package com.ldodds.slug.http;

import com.hp.hpl.jena.rdf.model.Resource;
import com.ldodds.slug.vocabulary.CONFIG;

public class DepthFilter extends URLTaskFilter 
{
    private static final int DEFAULT_DEPTH = 2;
    
	private int _depth;
	
	public DepthFilter() 
	{
		_depth = DEFAULT_DEPTH;
	}

	public DepthFilter(int depth)
	{
		_depth = depth;
	}
	
	public boolean acceptURL(URLTask task) 
	{
		return task.getDepth() < _depth;
	}

	protected boolean doConfig(Resource self) 
	{
		if (self.hasProperty(CONFIG.depth))
		{
			_depth = self.getProperty(CONFIG.depth).getInt();
			return true;
		}
		return false;
	}
	
	public int getDepth()
	{
		return _depth;
	}
}
