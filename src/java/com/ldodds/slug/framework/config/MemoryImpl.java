package com.ldodds.slug.framework.config;

import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.File;
import java.io.StringReader;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.RDF;
import com.ldodds.slug.vocabulary.SCUTTERVOCAB;

/**
 * FIXME transactions!
 * 
 * Default implementation of the Memory interface.
 * @author Leigh Dodds
 */
public abstract class MemoryImpl extends AbstractMemoryImpl 
{
  
  public MemoryImpl() {
    _logger = Logger.getLogger(getClass().getPackage().getName());
  }
  
  public Resource getRepresentation(URL url) 
  {
        _model.enterCriticalSection(Lock.READ);
        Statement s = null;        
        try
        {
        StmtIterator iter = _model.listStatements(null, SCUTTERVOCAB.source, 
            _model.createResource(url.toString()) );
          if (!iter.hasNext())
          {
            return null;  
          }
          s = iter.nextStatement();
        }
        finally
        {
            _model.leaveCriticalSection();
        }
        return s.getSubject();    
  }

  public Resource getOrCreateRepresentation(URL url) 
  {
    Resource rep = getRepresentation(url);
    if (rep == null)
    {
      rep = makeRepresentation(url);
    }
    return rep;
  }
    
    public Resource getOrCreateRepresentation(URL url, URL origin) 
    {        
        Resource rep = getOrCreateRepresentation(url);
        if (rep == null) {
        	return null;
        }
        _model.enterCriticalSection(Lock.WRITE);
        try
        {
            _model.add(rep, SCUTTERVOCAB.origin, _model.createResource( origin.toString() ) );
        } finally
        {
            _model.leaveCriticalSection();
        }
        return rep;
        
    }
    
    public boolean canBeFetched(Resource rep, Date date)
    {
        _model.enterCriticalSection(Lock.READ);
        try
        {
        	return super.canBeFetched(rep, date);
        } finally
        {
            _model.leaveCriticalSection();
        }
    }

    public void addRawTripleCount(Resource representation, long size)
    {
        _model.enterCriticalSection(Lock.WRITE);
        try
        {
        	super.addRawTripleCount(representation, size);
        } finally
        {
            _model.leaveCriticalSection();
        }
    }
    
  public Resource makeRepresentation(URL url)
  {
    _model.enterCriticalSection(Lock.WRITE);
    Resource rep = _model.createResource(SCUTTERVOCAB.Representation);    
    try {
      _model.add(rep, SCUTTERVOCAB.source, 
      _model.createResource(url.toString()) );
    } 
    finally {
      _model.leaveCriticalSection();
    }
    return rep;
  }
  
  public Resource makeFetch(Resource representation) 
  {
        _model.enterCriticalSection(Lock.WRITE);        
        try
        {
        	return super.makeFetch(representation);
        } finally
        {
            _model.leaveCriticalSection();
        }
  }

  public void annotateFetch(Resource fetch, int code, Map<String,List<String>> headers) 
    {
        _model.enterCriticalSection(Lock.WRITE);
        try
        {
        	super.annotateFetch(fetch, code, headers);
        } finally
        {
           _model.leaveCriticalSection(); 
        }
  }

  public void makeReasonAndSkip(Resource representation, String msg) 
    {    
        _model.enterCriticalSection(Lock.WRITE);
        try
        {
        	super.makeReasonAndSkip(representation, msg);
        } finally {
            _model.leaveCriticalSection();
        }
  }

  public void makeReasonAndError(Resource fetch, String msg) 
    {        
        _model.enterCriticalSection(Lock.WRITE);
        try
        {
        	super.makeReasonAndError(fetch, msg);
        } finally
        {
            _model.leaveCriticalSection();
        }
  }



  public void addLocalCopy(Resource representation, File localCopy) {
	  _model.enterCriticalSection(Lock.WRITE);
	  try {
		  super.addLocalCopy(representation, localCopy);
	  } finally {
		  _model.leaveCriticalSection();
	  }
	}
  
}
