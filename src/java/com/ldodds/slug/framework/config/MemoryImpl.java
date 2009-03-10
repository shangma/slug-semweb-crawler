package com.ldodds.slug.framework.config;

import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public abstract class MemoryImpl implements Memory 
{

  protected Model _model;
  protected Logger _logger;
  
  public MemoryImpl() {
    _logger = Logger.getLogger(getClass().getPackage().getName());
  }
  
  public Model getModel() 
  {
    return _model;
  }

  public Resource getRepresentation(URL url) 
  {
        Statement s = null;
        _model.enterCriticalSection(Lock.READ);
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
            if (rep.hasProperty(SCUTTERVOCAB.latestFetch))
            {
                Resource latest = (Resource)rep.getProperty(SCUTTERVOCAB.latestFetch).getObject();
                if (latest.hasProperty(DC.date))
                {
                    Date last = DateUtils.getDate(latest.getProperty(DC.date).getObject().toString());
                    if (last == null || last.before( date ) )
                    {
                        return true;                                                   
                    }
                }
            }
            else
            {
                return true;                   
            }
            return false;
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
            if (representation.hasProperty(SCUTTERVOCAB.latestFetch))
            {
                Resource fetch = (Resource)representation.getProperty(SCUTTERVOCAB.latestFetch).getObject();
                //FIXME
                fetch.addProperty(SCUTTERVOCAB.rawTripleCount, size + "" );
            }
        } finally
        {
            _model.leaveCriticalSection();
        }
    }
    
  public Resource makeRepresentation(URL url)
  {
    Resource rep = _model.createResource(SCUTTERVOCAB.Representation);
    _model.enterCriticalSection(Lock.WRITE);
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
        Resource fetch = _model.createResource(SCUTTERVOCAB.Fetch);

        _model.enterCriticalSection(Lock.WRITE);
        try
        {
        fetch.addProperty(DC.date, 
            _model.createTypedLiteral( DateUtils.getNow(), XSDDatatype.XSDdateTime ) );
            
        representation.addProperty(SCUTTERVOCAB.fetch, fetch);
        
        if (representation.hasProperty(SCUTTERVOCAB.latestFetch))
        {
          representation.removeAll(SCUTTERVOCAB.latestFetch);
        }
        representation.addProperty(SCUTTERVOCAB.latestFetch, fetch);
        } finally
        {
            _model.leaveCriticalSection();
        }
    return fetch;
  }

  public void annotateFetch(Resource fetch, int code, Map<String,List<String>> headers) 
    {
        _model.enterCriticalSection(Lock.WRITE);
        try
        {
        fetch.addProperty(SCUTTERVOCAB.status, code + "");
        if (headers.containsKey("Content-Type"))
        {
          List<String> values = (List<String>)headers.get("Content-Type");
          fetch.addProperty(SCUTTERVOCAB.contentType, values.get(0) );      
        }
        if (headers.containsKey("Last-Modified"))
        {
        	List<String> values = (List<String>)headers.get("Last-Modified");
          fetch.addProperty(SCUTTERVOCAB.lastModified, values.get(0) );
        }
        if (headers.containsKey("ETag"))
        {
        	List<String> values = (List<String>)headers.get("ETag");
          fetch.addProperty(SCUTTERVOCAB.etag, values.get(0) );      
        }
        } finally
        {
           _model.leaveCriticalSection(); 
        }
  }

  public Resource makeReasonAndSkip(Resource representation, String msg) 
    {
    Resource reason = _model.createResource(SCUTTERVOCAB.Reason);
        _model.enterCriticalSection(Lock.WRITE);
        try
        {
        reason.addProperty(DC.description, msg);
        reason.addProperty(DC.date, 
            _model.createTypedLiteral( DateUtils.getNow(), XSDDatatype.XSDdateTime ) );       
        representation.addProperty(SCUTTERVOCAB.skip, reason);
        } finally {
            _model.leaveCriticalSection();
        }
    return reason;
  }

  public Resource makeReasonAndError(Resource fetch, String msg) 
    {
        Resource reason = _model.createResource(SCUTTERVOCAB.Reason);
        _model.enterCriticalSection(Lock.WRITE);
        try
        {
        reason.addProperty(DC.description, msg);
        reason.addProperty(DC.date, 
            _model.createTypedLiteral( DateUtils.getNow(), XSDDatatype.XSDdateTime ) );       
        
        fetch.addProperty(SCUTTERVOCAB.error, reason);
        } finally
        {
            _model.leaveCriticalSection();
        }
    return reason;
  }

  public Resource makeReasonAndError(Resource fetch, Exception e) 
  {
    return makeReasonAndError(fetch, e.getClass().getName() + " " + e.getMessage());  
  }


  public ResIterator getAllRepresentations() {
    return _model.listSubjectsWithProperty(RDF.type, SCUTTERVOCAB.Representation);      
  }

  public void store(Resource resource, StringBuffer content, URL requestURL) throws Exception {
    _model.begin();
    try {
      _model.read( new StringReader(content.toString()), "");
      _model.commit();
      _logger.log(Level.FINEST, "Written " + requestURL + " to model, size= " + _model.size());      
    } catch (Exception e)
    {
      _logger.log(Level.SEVERE, "Unable to store response from " + requestURL, e);
      _model.abort();
    }
  } 

}
