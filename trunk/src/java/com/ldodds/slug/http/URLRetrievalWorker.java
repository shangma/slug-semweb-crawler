package com.ldodds.slug.http;

import java.util.logging.*;
import java.io.*;
import java.net.*;

import com.hp.hpl.jena.rdf.model.*;

import com.ldodds.slug.framework.*;
import com.ldodds.slug.framework.config.Memory;
import com.ldodds.slug.vocabulary.SCUTTERVOCAB;

/**
 * @author ldodds
 */
public class URLRetrievalWorker extends ProducerWorkerImpl
{
  //used to get etags, lastModified, add skips and Reasons
  private Memory _memory;
  
    public URLRetrievalWorker(Memory memory, String name)
    {
        super(name);
        _memory = memory;
    }
        
  /**
   * @see com.ldodds.slug.framework.ProducerWorkerImpl#doTask(java.lang.Object)
   */
  protected Object doTask(Task workItem) {
    URLTask urlTask = (URLTask)workItem;
    Response response = null;
        
    try {
      Resource rep = _memory.getOrCreateRepresentation( urlTask.getURL() );

      if (rep.hasProperty(SCUTTERVOCAB.skip)) {
        return null;                    
      }
      
      HttpURLConnection connection = urlTask.openConnection();
      
      configureConnection(rep, connection);
            
      Resource fetch = _memory.makeFetch(rep);
                  
            try
            {
        connection.connect();
        _memory.annotateFetch(fetch, connection.getResponseCode(), connection.getHeaderFields());
        
            } catch (UnknownHostException uhe)
            {
              _memory.makeReasonAndSkip(rep, "UnknownHostException: " + uhe.getMessage());              
              return null;            
            } catch (IOException ie)
            {
              _logger.warning("IOException on " + urlTask.getURL());
              _memory.makeReasonAndError(fetch, ie);
              return null;
            }
            
      if (connection.getResponseCode()== HttpURLConnection.HTTP_NOT_FOUND)
      {
        _memory.makeReasonAndSkip(rep, "404, Not Found");
        return null;
      }

      if (connection.getResponseCode()== HttpURLConnection.HTTP_FORBIDDEN)
      {
        _memory.makeReasonAndSkip(rep, "403, Not Authorized");
        return null;
      }

      if (connection.getResponseCode()== HttpURLConnection.HTTP_GONE)
      {
        _memory.makeReasonAndSkip(rep, "410, Gone");
        return null;
      }     
      
      if (connection.getResponseCode() == HttpURLConnection.HTTP_NOT_MODIFIED)
      {
        return null;
      }
            
            response = new Response(
                        urlTask.getURL(), 
                        connection.getHeaderFields(), 
                        readContent(connection) );
             
        } catch (Exception e)
        {
            _logger.log(Level.SEVERE, "Can't generate results for " + urlTask.getURL(), e);
        }
        
    return response;
  }

    private StringBuffer readContent(HttpURLConnection connection) 
        throws IOException
    {
        BufferedReader in = new BufferedReader( 
                new InputStreamReader(connection.getInputStream()), 1024);
            
        StringBuffer content = new StringBuffer();
        String line = in.readLine();
        while (line != null)
        {
            content.append(line + "\n");
            line = in.readLine();
        }            
        return content; 
    }
    
    /**
     * @param rep
     * @param connection
     */
    private void configureConnection(Resource rep, HttpURLConnection connection)
    {
        connection.setInstanceFollowRedirects(true);
        
        if (rep.hasProperty(SCUTTERVOCAB.latestFetch))
        {
                        
          Resource latestFetch = rep.getProperty(SCUTTERVOCAB.latestFetch).getResource();
          if (latestFetch.hasProperty(SCUTTERVOCAB.etag))
          {
            //System.out.println(latestFetch.getProperty(SCUTTERVOCAB.etag).getObject().toString());                
            connection.addRequestProperty("If-None-Match", 
              latestFetch.getProperty(SCUTTERVOCAB.etag).getObject().toString() );
          }
          if (latestFetch.hasProperty(SCUTTERVOCAB.lastModified))
          {
            //System.out.println(latestFetch.getProperty(SCUTTERVOCAB.lastModified).getObject().toString());
            connection.addRequestProperty("If-Modified-Since",
            latestFetch.getProperty(SCUTTERVOCAB.lastModified).getObject().toString() );
          }

        }
    }
    
}
