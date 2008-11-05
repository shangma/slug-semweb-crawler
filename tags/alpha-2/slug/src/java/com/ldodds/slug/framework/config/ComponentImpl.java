package com.ldodds.slug.framework.config;

import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Default implementation of the {@see Component} 
 * interface. By default the configure method does 
 * nothing.
 * 
 * @author ccslrd
 */
public class ComponentImpl implements Component 
{
	protected boolean _configured;
	
	public ComponentImpl() 
	{
		super();
		_configured = false;
	}

	public void configure(Resource self) 
	{
		if (_configured)
		{
			return;
		}
		_configured = doConfig(self);
	}
	
	public boolean isConfigured()
	{
		return _configured;
	}
	
	protected boolean doConfig(Resource self)
	{
		return true;
	}

}
