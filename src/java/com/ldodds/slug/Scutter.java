package com.ldodds.slug;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.*;
import java.net.*;

import com.ldodds.slug.framework.*;
import com.ldodds.slug.framework.config.Component;
import com.ldodds.slug.framework.config.ComponentFactory;
import com.ldodds.slug.framework.config.Memory;
import com.ldodds.slug.framework.config.MemoryFactory;
import com.ldodds.slug.http.*;
import com.ldodds.slug.vocabulary.CONFIG;
import com.ldodds.slug.vocabulary.SCUTTERVOCAB;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * @author ldodds
 *
 */
public class Scutter implements Runnable
{
  private static final String DEFAULT_USER_AGENT = "Slug Semantic Web Crawler (http://www.ldodds.com/projects/slug)";
  
  private List<URLTask> _urls;
  private Memory _memory;
  private FilteringController _controller;
  private int _workers;
  private String _configFile;
  private String _scutterId;
  private boolean _freshen;
  
  private static Logger logger = Logger.getLogger(Scutter.class.getPackage().getName());
  
  public Scutter() throws Exception
  {
    _urls = new ArrayList<URLTask>();
    _workers = 5;
    _scutterId = "default";
    _freshen = false;
  }
  
	/**
	 * Retrieve all rdfs:seeAlso links from a Model, and add each link to 
	 * the provided set as a URLTask.
	 * 
	 * @param model the model to scan
	 * @param set the Set to which URLs will be added
	 * @return the provided set
	 */
	public Set<URLTask> getSeeAlsoAsTasks(Model model, Set<URLTask> set)
	{
		NodeIterator iter = model.listObjectsOfProperty(RDFS.seeAlso);
		while (iter.hasNext())
		{
			RDFNode node = iter.nextNode();
			String seeAlso = node.toString(); 
			try
			{
				set.add( new URLTaskImpl( new URL(seeAlso) ) );
			} catch (MalformedURLException e)
			{
				logger.log(Level.WARNING, "Unable to build URL", e);				
			}
					
		}
		return set;				
	}  
	
  /**
     * @param scutterplan
     * @param urls
     * @throws FileNotFoundException
     * @throws MalformedURLException
     */
  private void addScutterPlan(String scutterplan) throws FileNotFoundException, MalformedURLException {
    Set<URLTask> urls = new HashSet<URLTask>();
        
    Model plan = ModelFactory.createDefaultModel();
    //FIXME other formats
    FileManager.get().readModel(plan, scutterplan, "TURTLE");
    
    //get all seeAlso links from scutter plan, and add to work plan
    getSeeAlsoAsTasks(plan, urls);
    
    _urls.addAll(urls);
  }

  public void run() {
    try {
      Model config = readConfig();
      Resource me = getSelf(config);
      
      //create and load memory of previous crawls
      _memory = new MemoryFactory().getMemoryFor(me);
      _memory.load();
      
      //create the worker factory, TODO make this configurable 
      URLRetrievalWorkerFactory factory = new URLRetrievalWorkerFactory();
          
      //configure the shared memory
      factory.setMemory( _memory );      
      
      //what to do with fetched data      
      ComponentFactory componentFactory = new ComponentFactory();
      List<Component> consumers = componentFactory.instantiateComponentsFor(me, CONFIG.consumers);         
      DelegatingConsumerImpl consumer = new DelegatingConsumerImpl(consumers);      
      
      //wire everything up
      consumer.setMemory(_memory);      
      factory.setConsumer( consumer );

      //how to monitor progress, TODO make this configurable
      Monitor monitor = new MonitorImpl();
      factory.setMonitor( monitor );
                
      //be polite and set JVM user agent
      setUserAgent(me);
      
      if (_freshen) {
        _urls.addAll( readURLsFromMemory() );       
      }
      
      //how many workers?
      _workers = me.getProperty(CONFIG.workers).getInt();
      
      _controller = new FilteringController(_urls, factory , _workers, monitor);

      //how to filter tasks
      List<Component> filters = componentFactory.instantiateComponentsFor(me, CONFIG.filters);
      DelegatingTaskFilterImpl filter = new DelegatingTaskFilterImpl(filters);      
      _controller.addFilter( filter );
          
      _controller.run();
    } catch (Exception e)
    {
      //HACK!
      throw new RuntimeException(e);
    }
  }
  
  private void setUserAgent(Resource me) {
	  String userAgent = DEFAULT_USER_AGENT;
	  if ( me.hasProperty(CONFIG.userAgent) ) {
		  userAgent = me.getProperty(CONFIG.userAgent).getObject().toString();
	  }
      System.setProperty("http.agent", userAgent);	
  }

private List<URLTask> readURLsFromMemory() throws MalformedURLException
  {
    List<URLTask> urls = new ArrayList<URLTask>();
    //loop through existing memory and add all previously found urls to work plan
    ResIterator reps = _memory.getAllRepresentations();
    while (reps.hasNext())
    {
      Resource representation = reps.nextResource();
      if (!representation.hasProperty(SCUTTERVOCAB.skip)
        && representation.hasProperty(SCUTTERVOCAB.source))
      {
        RDFNode node = representation.getProperty(SCUTTERVOCAB.source).getObject();
        urls.add( new URLTaskImpl( new URL( node.toString() ) ) );
      }
    }
    return urls;
  }
  
  private Model readConfig() throws FileNotFoundException
  {
    Model config = ModelFactory.createDefaultModel();
    config.read( new FileInputStream(_configFile), "");
    
    Model schema = ModelFactory.createDefaultModel();
    schema.read( this.getClass().getResourceAsStream("/config.rdfs"), "");
    
    return ModelFactory.createRDFSModel(schema, config);
  }
  
  private Resource getSelf(Model config)
  {
    return config.getResource(_scutterId);
  }
  
  public void stop()
  {
    if (_controller != null)
    {
        _controller.stop();
    }
  }
  
  public void save() throws Exception
  {
    System.out.println("Saving memory");
    _memory.save();
  }
  
  private void setConfig(String config) 
  {
    _configFile = config;
  }
  
  private void setId(String id)
  {
    _scutterId = id;
  }
  
  private void setFreshen(boolean freshen)
  {
    _freshen = freshen;
  }
  
  /**
   * Run the scutter from the command-line.
   * 
   * Arguments are:
   * 
   * -config : RDF file to use as Scutters config
   * -id : name of scutter, as identified in config
   * -plan : initial scutter plan
   */
    public static void main(String[] args) throws Exception
    {      
      final Scutter scutter = new Scutter();
      
      System.out.println("Configuring...");     
      configure(scutter, args );
      
      System.out.println("finished configuring, registering shutdown hook...");
    
      Runtime.getRuntime().addShutdownHook(new Thread()
            {
            public void run()
            {
                System.out.println("HALT!");
                Logger logger = Logger.getLogger( getClass().getPackage().getName() );
                logger.log(Level.INFO, "Scutter manually aborted, stopping run");
                scutter.stop();
                try
                {
                    scutter.save();
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            });
    
    System.out.println("starting scutter");
    Thread scutterThread = new Thread(scutter);
    scutterThread.setDaemon(false);
    scutterThread.start();
    System.out.println("scutter thread started");
      //scutter.run();

    }
    
    private static void configure(Scutter scutter, String[] args) throws Exception
    {
        if (args.length < 1) {
          printHelp();  
          return;
        }

        try {
	        for (int i=0; i<args.length; i++)
	        {
	            String arg = args[i];
	            if (arg.equals("-config"))
	            {
	                System.out.println("Found Config");
	                scutter.setConfig( args[++i] );
	            }
	            else if ( arg.equals("-id") )
	            {
	                System.out.println("Found Id");
	                scutter.setId( args[++i] );             
	            }
	            else if (arg.equals("-plan"))
	            {
	                System.out.println("Found plan");
	                scutter.addScutterPlan(args[++i]);
	            }
	            else if (arg.equals("-freshen"))
	            {
	                System.out.println("Found freshen");
	              scutter.setFreshen(true);
	              ++i;
	            }
	            else
	            {
	                System.out.println("Unknown argument '" + arg + "' ignored");
	            }
	        }
        } catch (Exception e) {
        	logger.log(Level.SEVERE, "Unexpected error", e);
        }
    }

    private static void printHelp() {
      System.out.println("Expected parameters");
      System.out.println("  -config <filename>");
      System.out.println("  -id <scutter id>");
      System.out.println("  -plan <plan uri>");
      System.out.println("  -freshen (optional)");
      System.exit(0);
    }
    
}
