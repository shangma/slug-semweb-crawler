package com.ldodds.slug.http.storage;

import java.util.logging.Level;

import com.hp.hpl.jena.rdf.model.Resource;
import com.ldodds.slug.http.Response;

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
