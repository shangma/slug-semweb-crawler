/*
 * This file is in the Public Domain
 */
package com.ldodds.slug.http;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.hp.hpl.jena.rdf.model.RDFErrorHandler;

/**
 * JAVADOC -- description of LoggingErrorHandler
 * 
 * @author ldodds
 */
public class LoggingErrorHandler implements RDFErrorHandler
{
    private Logger _logger;
    private String _name;
    
    public LoggingErrorHandler(String name)
    {
        _logger = Logger.getLogger(getClass().getPackage().getName());
        _name = name;
    }
    /** 
     * @see com.hp.hpl.jena.rdf.model.RDFErrorHandler#warning(java.lang.Exception)
     */
    public void warning(Exception thrown)
    {
        _logger.log(Level.WARNING, "Warning for " + _name, thrown);
    }

    /** 
     * @see com.hp.hpl.jena.rdf.model.RDFErrorHandler#error(java.lang.Exception)
     */
    public void error(Exception thrown)
    {
        _logger.log(Level.SEVERE, "Warning for " + _name, thrown);
    }

    /** 
     * @see com.hp.hpl.jena.rdf.model.RDFErrorHandler#fatalError(java.lang.Exception)
     */
    public void fatalError(Exception arg0)
    {
        // TODO Auto-generated method stub

    }

}
