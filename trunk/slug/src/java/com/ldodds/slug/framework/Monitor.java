package com.ldodds.slug.framework;

/**
 * Observes the progress of a web crawl, noting when 
 * tasks start and finish.
 * 
 * @author ldodds
 */
public interface Monitor
{
    public void startingTask(Worker worker, Task workItem);
    public void completedTask(Worker worker, Task workItem);
    
    public int getNumberOfActiveWorkers();
        
    //TODO need metrics
    //TODO need to know how many workers there are
    
}
