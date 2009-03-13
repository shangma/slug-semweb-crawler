package com.ldodds.slug.http.scanner;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Finds all HTTP Resource URIs in a Jena model
 * 
 * @author ldodds
 *
 */
public class ResourceScanner extends ScannerImpl {

	@Override
	protected Set<URL> scan(Model toScan, URL origin) {
		
		Set<URL> found = new HashSet<URL>(5);
		
		NodeIterator objects = toScan.listObjects();
		while ( objects.hasNext() ) {
			//System.out.println(System.currentTimeMillis() + " node");
			RDFNode node = objects.nextNode();
			if ( node.isResource() && !node.isAnon() ) {
				String uri = ((Resource)node).getURI();
				if ( isAcceptable(uri) ) {
					//System.out.println(System.currentTimeMillis() + " create url");					
					URL url = createURL(uri);
					//System.out.println(System.currentTimeMillis() + " create url finsihed");
					if (url != null) {
						//System.out.println(System.currentTimeMillis() + " adding");
						found.add( url );
						//System.out.println(System.currentTimeMillis() + " added");
					}
				}
				
			}
			//System.out.println(System.currentTimeMillis() + " completed");
		}
		
		return found;
	}

	protected URL createURL(String url) {
		try {
			return new URL(url);
		} catch (MalformedURLException e) {
			getLogger().log(Level.WARNING, "Unable to create url from " + url, e);
		}
		return null;
	}

	protected boolean isAcceptable(String uri) {
		return uri.startsWith("http");
	}

}
