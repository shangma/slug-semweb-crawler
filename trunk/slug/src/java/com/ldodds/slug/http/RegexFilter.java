package com.ldodds.slug.http;

import java.util.regex.*;

import com.hp.hpl.jena.rdf.model.Resource;
import com.ldodds.slug.vocabulary.CONFIG;

public class RegexFilter extends URLTaskFilter
{
    private Pattern _pattern;

    public RegexFilter()
    {
    	super();
    }
    
    public RegexFilter(String regex)
    {
        _pattern = Pattern.compile(regex);
    }
    
    public RegexFilter(Pattern pattern)
    {
        _pattern = pattern;
    }
    
    boolean acceptURL(URLTask task)
    {
        Matcher matcher = _pattern.matcher(task.getURL().toString());
        return !matcher.find();
    }

    protected boolean doConfig(Resource self) 
    {
    	if (self.hasProperty(CONFIG.filter))
    	{
    		String regex = self.getProperty(CONFIG.filter).getString();
    		_pattern = Pattern.compile(regex);
    		return true;
    	}
    	return false;
    }
    
    public Pattern getPattern()
    {
    	return _pattern;
    }
}
