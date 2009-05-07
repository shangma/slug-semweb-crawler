package com.ldodds.slug.http.rdf;

import java.util.HashSet;
import java.util.Set;

import java.net.URL;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.ldodds.slug.framework.Controller;
import com.ldodds.slug.framework.Task;
import com.ldodds.slug.framework.config.Memory;
import com.ldodds.slug.http.URLTask;
import com.ldodds.slug.http.URLTaskImpl;
import com.ldodds.slug.http.scanner.Scanner;
import com.ldodds.slug.http.scanner.SeeAlsoScanner;
import com.ldodds.slug.vocabulary.CONFIG;

import junit.framework.TestCase;

import static org.easymock.EasyMock.*;

public class TaskGeneratingConsumerTest extends TestCase {

	private TaskGeneratingConsumer consumer;
	
	public TaskGeneratingConsumerTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testFindWorkItemsAddsAll() throws Exception {
		
		URL origin = new URL("http://www.example.com/a/b/c");
		URL first = new URL("http://www.example.org/a.rdf" );
		URL second = new URL("http://www.example.org/b.rdf" );
		URL third = new URL("http://www.example.org/c.rdf" );
		
		final Set<URL> urls = new HashSet<URL>();
		urls.add( first );
		urls.add( second );
		urls.add( third );
		
		Scanner scanner = new Scanner() {

			public Set<java.net.URL> findURLs(Model toScan, java.net.URL origin) {
				return urls;
			}
			
		};
		consumer = new TaskGeneratingConsumer(scanner);
		
		Controller controller = org.easymock.classextension.EasyMock.createMock(Controller.class);		
		org.easymock.classextension.EasyMock.checkOrder(controller, false);
		
		org.easymock.classextension.EasyMock.expect( controller.getStarted() ).andReturn( null ).times(3);
		org.easymock.classextension.EasyMock.expect( controller.addWorkItem( (Task)anyObject() ) ).andReturn(true).anyTimes();
		
		org.easymock.classextension.EasyMock.replay(controller);
		
		controller.addWorkItem( new URLTaskImpl(first, 1) ) ;
		controller.addWorkItem( new URLTaskImpl(second, 1) );
		controller.addWorkItem( new URLTaskImpl(third, 1) );
		
		consumer.setController( controller );
		
		Memory memory = createMock(Memory.class);
		expect( memory.getOrCreateRepresentation(first, origin) ).andReturn(null);
		expect( memory.getOrCreateRepresentation(second, origin) ).andReturn(null);
		expect( memory.getOrCreateRepresentation(third, origin) ).andReturn(null);
		
		expect( memory.canBeFetched(null, null)).andReturn(Boolean.TRUE).times(3);
		
		consumer.setMemory(memory);
		
		replay(memory);
		URLTask task = new URLTaskImpl( origin );		
		consumer.findWorkItems(task, ModelFactory.createDefaultModel() );
		verify(memory);
		org.easymock.classextension.EasyMock.verify(controller);
	}

	public void testFindWorkItemsSkipsIfCannotBeFetched() throws Exception {
		
		URL origin = new URL("http://www.example.com/a/b/c");
		URL first = new URL("http://www.example.org/a.rdf" );
		URL second = new URL("http://www.example.org/b.rdf" );
		URL third = new URL("http://www.example.org/c.rdf" );
		
		final Set<URL> urls = new HashSet<URL>();
		urls.add( first );
		urls.add( second );
		urls.add( third );
		
		Scanner scanner = new Scanner() {

			public Set<java.net.URL> findURLs(Model toScan, java.net.URL origin) {
				return urls;
			}
			
		};
		consumer = new TaskGeneratingConsumer(scanner);
		
		Controller controller = org.easymock.classextension.EasyMock.createMock(Controller.class);
		org.easymock.classextension.EasyMock.checkOrder(controller, false);
		
		org.easymock.classextension.EasyMock.expect( controller.getStarted() ).andReturn( null ).times(3);
		
		consumer.setController( controller );
		
		Memory memory = createMock(Memory.class);
		expect( memory.getOrCreateRepresentation(first, origin) ).andReturn(null);
		expect( memory.getOrCreateRepresentation(second, origin) ).andReturn(null);
		expect( memory.getOrCreateRepresentation(third, origin) ).andReturn(null);
		
		expect( memory.canBeFetched(null, null)).andReturn(Boolean.FALSE).times(3);
		
		consumer.setMemory(memory);
		
		org.easymock.classextension.EasyMock.replay(controller);
		replay(memory);
		URLTask task = new URLTaskImpl( origin );		
		consumer.findWorkItems(task, ModelFactory.createDefaultModel() );
		verify(memory);
		org.easymock.classextension.EasyMock.verify(controller);
	}

	public void testDefaultScannerIsSeeAlso() throws Exception {
	
		Resource resource = createMock(Resource.class);
		expect( resource.hasProperty(CONFIG.scanner) ).andReturn(Boolean.FALSE);
		
		replay(resource);
		consumer = new TaskGeneratingConsumer();
		consumer.doConfig(resource);
		
		verify(resource);
		
		Scanner scanner = consumer.getScanner();
		assertNotNull( scanner );
		assertTrue( scanner instanceof SeeAlsoScanner );		
	}
	
}
