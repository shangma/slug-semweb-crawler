package com.ldodds.slug.http;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Trivial {@link URLTaskFilter} that simply ensures that 
 * URLs are only visited once within a particular crawl.
 * 
 * Note that this is slightly redundant as {@link RDFConsumer} 
 * already checks to see whether a URL has been fetched since 
 * the crawl started.
 * 
 * @author ldodds
 */
public class SingleFetchFilter extends URLTaskFilter
{
    private Set _visited;
    
    public SingleFetchFilter()
    {
        _visited = new HashSet(20);        
    }
    
    public boolean acceptURL(URLTask task)
    {
        URL url = task.getURL();
        if (_visited.contains(url))
        {
            return false;
        }
        _visited.add(url);
        return true;
    }
}
