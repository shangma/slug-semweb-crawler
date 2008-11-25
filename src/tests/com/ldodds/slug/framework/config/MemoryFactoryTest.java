package com.ldodds.slug.framework.config;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.DoesNotExistException;
import com.hp.hpl.jena.vocabulary.RDF;
import com.ldodds.slug.vocabulary.CONFIG;
import java.io.File;
import junit.framework.TestCase;

public class MemoryFactoryTest extends TestCase 
{
  private Model _config;
  
  public static void main(String[] args) 
  {
    junit.textui.TestRunner.run(MemoryFactoryTest.class);
  }

  public MemoryFactoryTest(String arg0) 
  {
    super(arg0);
  }

  protected void setUp() throws Exception 
  {
    super.setUp();
        Model config = ModelFactory.createDefaultModel();   
    config.read( this.getClass().getResourceAsStream("test-config.rdf"), "");
        
        Model schema = ModelFactory.createDefaultModel();
        schema.read( this.getClass().getResourceAsStream("/config.rdfs"), "");
        
        _config = ModelFactory.createRDFSModel(schema, config);
  }

  protected void tearDown() throws Exception 
  {
    super.tearDown();
    _config.close();
        _config = null;
  }

  /*
   * Test method for
   * 'com.ldodds.slug.framework.config.MemoryFactory.getMemoryFor(Resource)'
   */
  public void testGetMemoryFor() 
    {
        Resource resource = _config.getResource("file-memory-scutter");
        Memory memory = new MemoryFactory().getMemoryFor(resource);
        assertNotNull(memory);
        assertTrue( memory instanceof FileMemory);
        
        resource = _config.getResource("db-memory-scutter");
        memory = new MemoryFactory().getMemoryFor(resource);
        assertNotNull(memory);
        assertTrue( memory instanceof DatabaseMemory);

  }

  /*
   * Test method for
   * 'com.ldodds.slug.framework.config.MemoryFactory.createMemory(Resource)'
   */
  public void testCreateMemory() 
  {
        Resource resource = _config.getResource("file-memory");
        Memory memory = new MemoryFactory().createMemory(resource);
        assertNotNull(memory);
        assertTrue( memory instanceof FileMemory);
        
        resource = _config.getResource("db-memory");
        memory = new MemoryFactory().createMemory(resource);
        assertNotNull(memory);
        assertTrue( memory instanceof DatabaseMemory);        
  }

  /*
   * Test method for
   * 'com.ldodds.slug.framework.config.MemoryFactory.createFileMemory(Resource)'
   */
  public void testCreateFileMemoryResource() throws Exception
  {
        Resource resource = _config.getResource("file-memory");
        assertNotNull(resource);
        assertTrue(resource.hasProperty(RDF.type, CONFIG.Memory));
    Memory memory = new MemoryFactory().createFileMemory(resource);
    assertNotNull(memory);
    assertTrue( memory instanceof FileMemory );
    assertNull( memory.getModel() );
    memory.load();
    assertNotNull( memory.getModel() );
          
  }

  /*
   * Test method for
   * 'com.ldodds.slug.framework.config.MemoryFactory.createFileMemory(String)'
   */
  public void testCreateFileMemoryString() throws Exception
  {
    File tmpFile = File.createTempFile("memory", "rdf");
    Memory memory = new MemoryFactory().createFileMemory(tmpFile.getAbsolutePath());
    assertNotNull(memory);
    assertTrue( memory instanceof FileMemory );
    assertNull( memory.getModel() );
    memory.load();
    assertNotNull( memory.getModel() );
    assertEquals( 0L,  memory.getModel().size() );    
  }

  /*
   * Test method for
   * 'com.ldodds.slug.framework.config.MemoryFactory.createTemporaryFileMemory()'
   */
  public void testCreateTemporaryFileMemory() throws Exception
  {
    Memory memory = new MemoryFactory().createTemporaryFileMemory();
    assertNotNull(memory);
    assertTrue( memory instanceof FileMemory );
    assertNull( memory.getModel() );
    memory.load();
    assertNotNull( memory.getModel() );
  }

  /*
   * Test method for
   * 'com.ldodds.slug.framework.config.MemoryFactory.createDatabaseMemory(Resource)'
   */
  public void ignored_testCreateDatabaseMemoryResource() throws Exception
  {
        Resource resource = _config.getResource("db-memory");
        assertNotNull(resource);
        assertTrue(resource.hasProperty(RDF.type, CONFIG.DatabaseMemory));
        Memory memory = new MemoryFactory().createDatabaseMemory(resource);
        assertNotNull(memory);
        assertTrue( memory instanceof DatabaseMemory );
        assertNull( memory.getModel() );
        try
        {
            memory.load();
        } catch (DoesNotExistException e)
        {
            //fine, at least we know its opened the db connection
        }
  }

  /*
   * Test method for
   * 'com.ldodds.slug.framework.config.MemoryFactory.createDatabaseMemory(String,
   * String, String, String, String, String)'
   */
  public void testCreateDatabaseMemoryStringStringStringStringStringString() 
    {

  }

}
