package com.ldodds.slug;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.*;
import java.net.*;

import com.ldodds.slug.framework.*;
import com.ldodds.slug.framework.config.ComponentFactory;
import com.ldodds.slug.framework.config.Memory;
import com.ldodds.slug.framework.config.MemoryFactory;
import com.ldodds.slug.http.*;
import com.ldodds.slug.vocabulary.CONFIG;
import com.ldodds.slug.vocabulary.SCUTTERVOCAB;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;

/**
 * @author ldodds
 *
 */
public class Scutter implements Runnable
{
  private static final String USER_AGENT = "Slug Semantic Web Crawler alpha-2 (http://www.ldodds.com/projects/slug)";
  
  private List _urls;
  private Memory _memory;
  private FilteringController _controller;
  private int _workers;
  private String _configFile;
  private String _scutterId;
  private boolean _freshen;
  
  public Scutter() throws Exception
  {
    _urls = new ArrayList();
    _workers = 5;
    _scutterId = "slug";
    _freshen = false;
  }
  
  /**
     * @param scutterplan
     * @param urls
     * @throws FileNotFoundException
     * @throws MalformedURLException
     */
  private void addScutterPlan(String scutterplan) throws FileNotFoundException, MalformedURLException {
    Set urls = new HashSet();
        
    Model plan = ModelFactory.createDefaultModel();
    if (scutterplan != null && scutterplan.startsWith("http://")) {
      plan.read(scutterplan, scutterplan);
    }
    else if (scutterplan != null) {
      plan.read(new FileInputStream(scutterplan), "");    
    }
    
    //get all seeAlso links from scutter plan, and add to work plan
    ModelUtils.getSeeAlsoAsTasks(plan, urls);
    
    _urls.addAll(urls);
  }

  public void run() {
    try {
      Model config = readConfig();
      Resource me = getSelf(config);
      
      _memory = new MemoryFactory().getMemoryFor(me);
      _memory.load();
      
      //create the worker factory, TODO make this configurable 
      URLRetrievalWorkerFactory factory = new URLRetrievalWorkerFactory();
          
      //configure the shared memory
      factory.setMemory( _memory );
      
      ComponentFactory componentFactory = new ComponentFactory();
      List consumers = componentFactory.instantiateComponentsFor(me, CONFIG.consumers);
      
      //what to do with the results   
      DelegatingConsumerImpl consumer = new DelegatingConsumerImpl(consumers);      
      consumer.setMemory(_memory);
      factory.setConsumer( consumer );

      //how to filter tasks
      List filters = componentFactory.instantiateComponentsFor(me, CONFIG.filters);
      DelegatingTaskFilterImpl filter = new DelegatingTaskFilterImpl(filters);

      //how to monitor progress, TODO make this configurable
      Monitor monitor = new MonitorImpl();
      factory.setMonitor( monitor );
          
      //how many workers?
      _workers = me.getProperty(CONFIG.workers).getInt();
      
      if (_freshen) {
        _urls.addAll( readURLsFromMemory() );       
      }
      
      _controller = new FilteringController(_urls, factory , _workers, monitor);
      
      _controller.addFilter( filter );
          
      _controller.run();
    } catch (Exception e)
    {
      //HACK!
      throw new RuntimeException(e);
    }
  }
  
  private List readURLsFromMemory() throws MalformedURLException
  {
    List urls = new ArrayList();
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
      //FIXME read this from the scutter profile instead
      System.setProperty("http.agent", USER_AGENT);
      
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
