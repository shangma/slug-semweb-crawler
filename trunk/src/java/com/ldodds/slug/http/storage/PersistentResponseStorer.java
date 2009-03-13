package com.ldodds.slug.http.storage;

import java.util.logging.Level;

import com.hp.hpl.jena.rdf.model.Resource;
import com.ldodds.slug.framework.config.Memory;
import com.ldodds.slug.framework.config.MemoryFactory;
import com.ldodds.slug.http.Response;

/**
 * Initial implementation of a storage component that puts data into 
 * a persistent RDF database.
 * 
 * The current implementation is a bit of a hack that uses the Memory 
 * code in order to get access to persistent Jena models. This should be 
 * re-implemented to use the Jena Assembler API.
 * 
 * @author ldodds
 * FIXME this is a hack
 */
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
      getLogger().log(Level.SEVERE, "Unable to load memory", e);
    }
    return true;
  }
}
