package com.ldodds.slug.http;

import java.io.StringReader;
import java.util.logging.Level;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.ldodds.slug.framework.config.Memory;
import com.ldodds.slug.framework.config.MemoryFactory;

public class PlatformResponseStorer extends AbstractResponseStorer
{
  
  public PlatformResponseStorer() 
  {
    super();
  }

  synchronized void store(Resource resource, Response response) throws Exception 
  {
     _logger.log(Level.FINE, "Written " + response.getRequestURL() + " to store");      
  }
  
  public boolean doConfig(Resource self) 
  {

    return true;
  }
}
