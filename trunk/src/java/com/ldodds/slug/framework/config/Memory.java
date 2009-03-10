package com.ldodds.slug.framework.config;

import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResIterator;

/**
 * Interace describing operations required in a 
 * Scutter memory.
 * 
 * Provides convenience methods for accessing and 
 * updating memory using terms from the ScutterVocab.
 * 
 * @author Leigh Dodds
 */
public interface Memory 
{
  Model load() throws Exception;
  void save() throws Exception;
  
  ResIterator getAllRepresentations();
  
  Resource getRepresentation(URL url);
  Resource getOrCreateRepresentation(URL url);
  Resource getOrCreateRepresentation(URL url, URL origin);
  boolean canBeFetched(Resource representation, Date date);
  void addRawTripleCount(Resource representation, long size);
  Resource makeFetch(Resource representation);
  void annotateFetch(Resource fetch, int code, Map<String, List<String>> headers);
  Resource makeReasonAndSkip(Resource representation, String msg);
  Resource makeReasonAndError(Resource fetch, String msg);
  Resource makeReasonAndError(Resource fetch, Exception e);

  void store(Resource resource, StringBuffer content, URL requestURL) throws Exception;

}
