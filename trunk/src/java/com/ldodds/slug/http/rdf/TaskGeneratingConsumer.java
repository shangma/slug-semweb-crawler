package com.ldodds.slug.http.rdf;

import java.net.URL;
import java.util.Iterator;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.ldodds.slug.framework.Result;
import com.ldodds.slug.http.URLTask;
import com.ldodds.slug.http.URLTaskImpl;
import com.ldodds.slug.http.scanner.Scanner;
import com.ldodds.slug.http.scanner.SeeAlsoScanner;
import com.ldodds.slug.vocabulary.CONFIG;

/**
 * Scans parsed RDF models to look for URLS to be added as new tasks
 * 
 * @author ldodds
 *
 */
public class TaskGeneratingConsumer extends ModelDependentConsumer {

	protected Scanner scanner;

	public TaskGeneratingConsumer() {
		this(null);		
	}
	
	public TaskGeneratingConsumer(Scanner scanner) {
		this.scanner = scanner;
	}
	
	@Override
	protected void processModel(URLTask task, Result result, Model model) {
		findWorkItems(task, model);
	}
	
	/**
	 * Find additional work items in the generated model. For each 
	 * URL found in the model, this implementation will check whether 
	 * the url can be fetched before adding it as a work item.
	 * 
	 * @param origin
	 * @param model
	 */
	protected void findWorkItems(URLTask origin, Model model) {
		Set<URL> newURLs = findURLs(model, origin.getURL());
		Iterator<URL> iter = newURLs.iterator();
		int addedTasks = 0;
		while (iter.hasNext()) {
			URL next = iter.next();

			Resource rep = memory.getOrCreateRepresentation(next,
					origin.getURL());
		
			//TODO: this is now redundant as the Controller will never redo any task in a single run
			//leaving for now, to allow for additional behaviour to be hooked in
			if ( memory.canBeFetched(rep, controller.getStarted()) ) {
				boolean added = controller.addWorkItem(new URLTaskImpl(origin, next));
				if (added) addedTasks++;
			}

		}
		getLogger().fine("QUEUED " + addedTasks + " tasks from " + origin.getURL() );
	}

	/**
	 * Find URLs in the provided Model. These will be used to generate 
	 * new work items.
	 * 
	 * @param model
	 *            the model to scan for urls
	 * @return
	 */
	protected Set<URL> findURLs(Model model, URL origin) {
		return scanner.findURLs(model, origin);
	}

	/**
	 * Configure the component. Sub-classes should make sure they invoke the 
	 * super-class configuration in order to properly configure the url scanner.
	 * 
	 */
	@Override
	protected boolean doConfig(Resource self) {
		if (self.hasProperty(CONFIG.scanner)) {
			scanner = (Scanner) instantiateReferenced(self, CONFIG.scanner);
		} else {
			scanner = new SeeAlsoScanner();
		}
		return super.doConfig(self);
	}

	protected Scanner getScanner() {
		return scanner;
	}
	
}
