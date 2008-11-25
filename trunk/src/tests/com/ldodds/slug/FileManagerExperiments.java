package com.ldodds.slug;

import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.LocationMapper;

import junit.framework.TestCase;

public class FileManagerExperiments extends TestCase
{

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(FileManagerExperiments.class);
    }

    public FileManagerExperiments(String arg0)
    {
        super(arg0);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testLoadFileViaManager() throws Exception
    {
        FileManager manager = FileManager.get();
        Model model = manager.loadModel("c:\\Documents and Settings\\ldodds\\slug-cache\\usefulinc.com\\edd\\blog\\rss");
        assertNotNull( model );
        assertTrue( model.size() > 0 );
    }

    public void testLoadFileWithPrefixViaManager() throws Exception
    {
        FileManager manager = FileManager.get();
        Model model = manager.loadModel("file:c:\\Documents and Settings\\ldodds\\slug-cache\\usefulinc.com\\edd\\blog\\rss");
        assertNotNull( model );
        assertTrue( model.size() > 0 );

    }
    
    public void testLoadURLViaManager() throws Exception
    {
        FileManager manager = FileManager.get();
        Model model = manager.loadModel("http://www.ldodds.com/ldodds.rdf");
        assertNotNull( model );
        assertTrue( model.size() > 0 );
    }
    
    public void testConfigureLocationMapper() throws Exception
    {
        FileManager manager = FileManager.get();
        LocationMapper mapper = manager.getLocationMapper();
        
        Model memory = manager.loadModel("file:c:\\projects\\slug\\memory.rdf");

        List rules = Rule.rulesFromURL( "file:c:\\projects\\slug\\rules.rl" );
        GenericRuleReasoner reasoner = new GenericRuleReasoner(rules);        
        InfModel inference = ModelFactory.createInfModel(reasoner, memory);

        Model mappings = inference.getDeductionsModel();
        
        mappings.write(System.out, "N3");
        
        mapper.processConfig( mappings );
        
        for (Iterator iter = mapper.listAltEntries(); iter.hasNext();)
        {
            System.out.println(iter.next());
        }
    }
    
    public void testMapper() throws Exception
    {
        FileManager manager = FileManager.get();
        LocationMapper mapper = manager.getLocationMapper();
        
        Model mappings = manager.loadModel("file:c:\\projects\\slug\\lm.n3");
        mapper.processConfig( mappings );
        for (Iterator iter = mapper.listAltEntries(); iter.hasNext();)
        {
            System.out.println(iter.next());
        }
        
    }
    
}
