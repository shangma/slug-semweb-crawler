package com.ldodds.slug.framework.config;

import java.io.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.RDFSyntax;
import com.ldodds.slug.vocabulary.SCUTTERVOCAB;

class FileMemory extends MemoryImpl 
{
	private String _fileName;
	public FileMemory(String fileName) 
	{
		_fileName = fileName;
		_model = null;
	}

	public Model load() throws Exception
	{
		if (_model != null)
		{
			return _model;
		}
		_model = ModelFactory.createDefaultModel();
		File file = new File(_fileName);
		if (! (file.length() == 0L))
		{
			_model.read( new FileInputStream(_fileName), "" );	
		}		
		return _model;
	}

	public void save() throws Exception 
	{
        RDFWriter writer = _model.getWriter("RDF/XML-ABBREV");
        _model.getGraph().getPrefixMapping().setNsPrefix("dc", DC.getURI());
        _model.getGraph().getPrefixMapping().setNsPrefix("scutter", SCUTTERVOCAB.getURI());
                        
        //Switch off serializing non-repeating properties as attributes
        writer.setProperty("blockRules", new Resource[] {RDFSyntax.propertyAttr});
        
        writer.setProperty("prettyTypes", new Resource[] { SCUTTERVOCAB.Fetch,
                                                           SCUTTERVOCAB.Reason,
                                                           SCUTTERVOCAB.Representation});       
        writer.write(_model, new FileOutputStream(_fileName), "");                        
        
		_model.close();
	}

}
