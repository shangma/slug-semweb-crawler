package com.ldodds.slug.http;

import java.net.*;
import java.util.logging.Level;
import java.io.*;

import com.hp.hpl.jena.rdf.model.*;

import com.ldodds.slug.framework.Consumer;
import com.ldodds.slug.vocabulary.CONFIG;
import com.ldodds.slug.vocabulary.SCUTTERVOCAB;

/**
 * Saves stuff to disk.
 * 
 * @author ldodds
 */
public class ResponseStorer extends AbstractResponseStorer implements Consumer
{
	private File _cache;
    
	public ResponseStorer()
	{
		super();
	}
	
	public ResponseStorer(File cache)
	{
		super();	    
		_cache = cache;
		_logger.log(Level.INFO, "Cache directory:", _cache);
	}

	private File getFileName(URL url)
	{
		String fileName = url.getFile();
		
		fileName = fileName.replace('?','_').replace('&', '_');
		
		File domain = new File(_cache, url.getHost());
		if (!domain.exists())
		{
			domain.mkdir();
		}
		File representation = new File(domain, fileName);
		File parent = representation.getParentFile();
		parent.mkdirs();
		return representation;
	}
	
	void store(Resource representation, Response response) throws Exception
	{
		File localCopy = getFileName(response.getRequestURL());			
		store(response, localCopy);
		
		//just in case
		if (representation.hasProperty(SCUTTERVOCAB.localCopy)
			&& 
			!localCopy.toString().equals(
				representation.getProperty(SCUTTERVOCAB.localCopy).getObject().toString() 
				) 
			)
		{
			
			representation.removeAll(SCUTTERVOCAB.localCopy);
		}			
					
		representation.addProperty(SCUTTERVOCAB.localCopy, localCopy.toString());
		
	}
	
	private void store(Response response, File toFile) throws Exception
	{
		BufferedWriter out = 
			new BufferedWriter( new FileWriter(toFile) ); 

		out.write(response.getContent().toString());
		
		out.flush();
		out.close();		
	}

    protected boolean doConfig(Resource self) 
    {
    	//if we don't already have a cache, then look for one
    	//in the config.
    	if (self.hasProperty(CONFIG.cache))
    	{
    		String directory = self.getProperty(CONFIG.cache).getString();
    		_cache = new File(directory);
    	}    	
    	else
    	{
    		_cache = getDefaultCacheDir();
    	}
    	return true;
    }
    
	/**
     * Get the directory into which offline copies of documents will be written
     */
    private File getDefaultCacheDir()
    {
        File cache = new File(System.getProperty("user.home"), "slug-cache");
		
		if (!cache.exists())
		{
		    cache.mkdir();
		}
		return cache;
    }
}
