package com.ldodds.slug.framework;

import java.util.*;
import java.util.logging.*;

/**
 * A Controller is responsible for handing out a list of 
 * work items to a number of {@link Worker} objects which it 
 * created using a designated {@link WorkerFactory}. Tracking 
 * progress against the task list is delegated to a {@link Monitor} 
 * 
 * The Controller is a generic class which can be used for performing 
 * any co-ordinating a number of different types of task: it's not 
 * specific to web crawling so a Controller instance could be used 
 * in other scenarios.  
 * 
 * The Controller exposes a very basic management interface in the 
 * form of its {@link start}, {@link stop}, {@link addWorkers} and 
 * {@link reduceWorkers} methods. These could be used to build a 
 * graphical utility for managing a running Controller instance.
 * 
 * @author ldodds
 */
public class Controller implements Runnable
{
  private List<Task> _workItems;
  private WorkerFactory _factory;
  private int _numberOfWorkers;
  private List<Worker> _workers;
  protected Logger _logger;
  private Monitor _monitor;
  private ThreadGroup _workerThreadGroup;
  private Date _started;
      
    public Controller(List<Task> workItems, WorkerFactory factory, int numberOfWorkers, Monitor monitor)
    {
        _workItems = new ArrayList<Task>(workItems);
        _factory = factory;
        _numberOfWorkers = numberOfWorkers;
        _workers = new ArrayList<Worker>();
        _monitor = monitor;                
        _logger = Logger.getLogger( getClass().getPackage().getName() );
       
        factory.setController(this);
        factory.setMonitor(_monitor);
        _monitor.setController(this);
        
        _workerThreadGroup = new ThreadGroup("Workers");
    }
    
    public void run()
    {        
      _started = Calendar.getInstance().getTime();
        
        _logger.log(Level.INFO, "Controller thread starting");
        
        _logger.fine("Starting " + _numberOfWorkers + " Workers");
        for (int i=1; i<=_numberOfWorkers; i++)
        {
            Worker worker = _factory.getWorker("Worker-" + i );
            _workers.add(worker);
            Thread thread = new Thread(_workerThreadGroup, worker);
            thread.start();            
        }        
        
        _logger.log(Level.INFO, "Workers created");
        
        //now loop
        while (true)
        {
            try
            {
                Thread.sleep(1000);
                if (_workItems.size() == 0 && _monitor.getNumberOfActiveWorkers() == 0)
                {
                  break;
                }
            } catch (InterruptedException ie)
            {
                //continue loop
            }
        }
        
        _logger.log(Level.INFO, "Controller thread stopping. Task list completed");
        stop();
    }
    
    public void stop()
    {
        _logger.fine("Stopping " + _numberOfWorkers + " Workers");        
        for (Iterator<Worker> iter = _workers.iterator(); iter.hasNext();)
        {
        	Worker element = iter.next();
        	element.stop();
    }
        while (_monitor.getNumberOfActiveWorkers() > 0)
        {
            ;
        }
        _logger.log(Level.INFO, "Controller thread halted");
    }
    
    public void addWorkers(int increment)
    {
        _logger.fine("Adding " + increment + " extra Workers");
        for (int i=_numberOfWorkers; 
               i<= _numberOfWorkers + increment;
               i++)
               {
                   Worker worker = _factory.getWorker("Worker-" + _numberOfWorkers + i);
                   _workers.add(worker);
                   Thread thread = new Thread(worker);
                   thread.start();
               }
        _numberOfWorkers += increment;               
    }
    
    public void reduceWorkers(int decrement)
    {
        _logger.fine("Removing " + decrement + " Workers");
        for (int i=_numberOfWorkers;
               i >= _numberOfWorkers - decrement;
               i--)
               {
                   Worker worker = (Worker)_workers.remove(i);
                   worker.stop();
               }
    }
    
    public synchronized Task popWorkItem(Worker worker)
    {
      if (_workItems.isEmpty())
      {
        return null;
      }
      
      Object obj = _workItems.remove(0);
      //System.out.println(obj.getClass() + ", " + obj.toString());
        Task workItem = (Task)obj;
        _logger.finest("Popping work item: " + workItem);
        _monitor.startingTask(worker, workItem);
        return workItem;
    }
    
    public synchronized void completedTask(Worker worker, Task task)
    {
      _monitor.completedTask(worker, task);
    }
    
    public synchronized void addWorkItem(Task workItem)
    {
        _logger.fine("Adding new work item: " + workItem);        
        _workItems.add(workItem);
        //let the workers know
        notifyAll();
    }

  public Date getStarted()
  {
    return _started;        
  }
  
  public int getQueueSize() {
	  return _workItems.size();
  }
  
  public int getNumberOfWorkers() {
	  return _numberOfWorkers;
  }
}
