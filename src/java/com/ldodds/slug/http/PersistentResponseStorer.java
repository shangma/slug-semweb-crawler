package com.ldodds.slug.http;

import java.util.logging.Level;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.ldodds.slug.framework.config.Memory;
import com.ldodds.slug.framework.config.MemoryFactory;

public class PersistentResponseStorer extends AbstractResponseStorer
{
  //HACK use a memory?
  private Memory _store;
  
  public PersistentResponseStorer() 
  {
    super();
  }

  synchronized void store(Resource resource, Response response) throws Exception 
  {
    _store.store(resource, response.getContent(), response.getRequestURL());
  }
  
  public boolean doConfig(Resource self) 
  {
    //HACK, reuse memory stuff
    MemoryFactory factory = new MemoryFactory();
    _store = factory.getMemoryFor(self);
    try
    {
      _store.load();      
    } catch (Exception e)
    {
      _logger.log(Level.SEVERE, "Unable to load memory", e);
    }
    return true;
  }
}
