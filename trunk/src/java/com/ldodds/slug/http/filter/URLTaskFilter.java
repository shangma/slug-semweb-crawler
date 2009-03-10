package com.ldodds.slug.http.filter;

import com.ldodds.slug.framework.Task;
import com.ldodds.slug.framework.TaskFilter;
import com.ldodds.slug.framework.config.ComponentImpl;
import com.ldodds.slug.http.URLTask;

public abstract class URLTaskFilter extends ComponentImpl implements TaskFilter
{
    public boolean accept(Task task)
    {
        if (! (task instanceof URLTask) )
        {
            return true;
        }
        
        return acceptURL( (URLTask) task);
    }

    protected abstract boolean acceptURL(URLTask task);
}
