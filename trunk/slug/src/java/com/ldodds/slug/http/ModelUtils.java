package com.ldodds.slug.http;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;

/**
 * @author ldodds
 *
 */
public class ModelUtils
{
	public static Set getSeeAlsos(Model model, Set set)
	{
		NodeIterator iter = model.listObjectsOfProperty(RDFS.seeAlso);
		while (iter.hasNext())
		{
			RDFNode node = iter.nextNode();
			String seeAlso = node.toString(); 
			try
			{
				set.add( new URL(seeAlso) );
			} catch (MalformedURLException e)
			{
			}
					
		}
		return set;				
	}
	
	public static Set getSeeAlsoAsTasks(Model model, Set set)
	{
		NodeIterator iter = model.listObjectsOfProperty(RDFS.seeAlso);
		while (iter.hasNext())
		{
			RDFNode node = iter.nextNode();
			String seeAlso = node.toString(); 
			try
			{
				set.add( new URLTaskImpl( new URL(seeAlso) ) );
			} catch (MalformedURLException e)
			{
			}
					
		}
		return set;				
	}
	
}
