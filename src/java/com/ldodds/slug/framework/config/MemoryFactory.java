package com.ldodds.slug.framework.config;

import java.io.*;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.ldodds.slug.vocabulary.CONFIG;

/**
 * Responsible for creating {@link Memory} objects for use 
 * by the {@link Scutter} and the {@link Consumer}s.
 * 
 * @author Leigh Dodds
 */
public class MemoryFactory 
{
  public Memory getMemoryFor(Resource forResource) {
    if (!forResource.hasProperty(CONFIG.hasMemory)) {
      // TODO: return a NullMemory?
      return null;
    }
    
    return createMemory( forResource.getProperty(CONFIG.hasMemory).getResource() );
  }
  
  public Memory createMemory(Resource memory)
  {
    if (!memory.hasProperty(RDF.type, CONFIG.Memory)) {
      // TODO: return a NullMemory?
      return null;
    }
    if (memory.hasProperty(RDF.type, CONFIG.FileMemory))  {
      return createFileMemory(memory);
    }
    if (memory.hasProperty(RDF.type, CONFIG.DatabaseMemory)) {
      return createDatabaseMemory(memory);
    }
    
    // TODO: return a NullMemory?
    return null; 
  }
  
  public Memory createFileMemory(Resource memory)
  {
    if (memory.hasProperty(CONFIG.file))
    {
      return createFileMemory(
          memory.getProperty(CONFIG.file).getString());
    }
    return null;
  }
  
  public Memory createFileMemory(String fileName)
  {
    return new FileMemory(fileName);
  }
  public Memory createTemporaryFileMemory() throws IOException
  {
    File temp = File.createTempFile("memory", "rdf");
    return createFileMemory(temp.getAbsolutePath());
  }
  
  public Memory createDatabaseMemory(Resource memory)
  {
    if (memory.hasProperty(RDF.type, CONFIG.DatabaseMemory))
    {
      //TODO make this more robust
      return new DatabaseMemory
          (
        memory.getProperty(CONFIG.user).getString(),
        memory.getProperty(CONFIG.pass).getString(),
        memory.getProperty(CONFIG.modelURI).getResource().getURI(),
        memory.getProperty(CONFIG.dbURL).getString(),
        memory.getProperty(CONFIG.dbName).getString(),
        memory.getProperty(CONFIG.driver).getString()
          );
    }
    return null;
  }
  
  public Memory createDatabaseMemory(String user, String pass, 
      String modelURI, String dbURL, String dbName, 
      String driver)
  {
    return new DatabaseMemory(user, pass, modelURI, dbURL, 
        dbName, driver);        
  }
}
