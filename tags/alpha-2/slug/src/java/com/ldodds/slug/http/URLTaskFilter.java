package com.ldodds.slug.http;

import com.ldodds.slug.framework.Task;
import com.ldodds.slug.framework.TaskFilter;
import com.ldodds.slug.framework.config.ComponentImpl;

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

    abstract boolean acceptURL(URLTask task);
}
