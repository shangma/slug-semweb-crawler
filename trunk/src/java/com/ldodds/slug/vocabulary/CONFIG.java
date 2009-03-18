package com.ldodds.slug.vocabulary; import com.hp.hpl.jena.rdf.model.*;
 /* Code generated by schemagen using Ant at 24-01-2006 12:22 AM */     
public class CONFIG {
    /** <p>The RDF model that holds the vocabulary terms</p> */
    private static Model m_model = ModelFactory.createDefaultModel();
    
    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://purl.org/NET/schemas/slug/config/";
    
    /** <p>The namespace of the vocabulary as a string</p>
     *  @see #NS */
    public static String getURI() {return NS;}
    
    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );
    
    public static final Property impl = m_model.createProperty( "http://purl.org/NET/schemas/slug/config/impl" );
    
    public static final Property file = m_model.createProperty( "http://purl.org/NET/schemas/slug/config/file" );
    
    public static final Property consumers = m_model.createProperty( "http://purl.org/NET/schemas/slug/config/consumers" );
    
    public static final Property dbURL = m_model.createProperty( "http://purl.org/NET/schemas/slug/config/dbURL" );
    
    public static final Property cache = m_model.createProperty( "http://purl.org/NET/schemas/slug/config/cache" );
    
    public static final Property user = m_model.createProperty( "http://purl.org/NET/schemas/slug/config/user" );
    
    public static final Property filter = m_model.createProperty( "http://purl.org/NET/schemas/slug/config/filter" );
    
    public static final Property driver = m_model.createProperty( "http://purl.org/NET/schemas/slug/config/driver" );
    
    public static final Property hasMemory = m_model.createProperty( "http://purl.org/NET/schemas/slug/config/hasMemory" );
    
    public static final Property workers = m_model.createProperty( "http://purl.org/NET/schemas/slug/config/workers" );
    
    public static final Property depth = m_model.createProperty( "http://purl.org/NET/schemas/slug/config/depth" );
    
    public static final Property modelURI = m_model.createProperty( "http://purl.org/NET/schemas/slug/config/modelURI" );
    
    public static final Property filters = m_model.createProperty( "http://purl.org/NET/schemas/slug/config/filters" );

    public static final Property scanner = m_model.createProperty( "http://purl.org/NET/schemas/slug/config/scanner" );
    
    public static final Property pass = m_model.createProperty( "http://purl.org/NET/schemas/slug/config/pass" );
    
    public static final Property dbName = m_model.createProperty( "http://purl.org/NET/schemas/slug/config/dbName" );

    public static final Property replacementRegex = m_model.createProperty( "http://purl.org/NET/schemas/slug/config/replacementRegex" );
    public static final Property replacementValue = m_model.createProperty( "http://purl.org/NET/schemas/slug/config/replacementValue" );
    
    public static final Property storer = m_model.createProperty( "http://purl.org/NET/schemas/slug/config/storer" );
    
    public static final Property userAgent = m_model.createProperty( "http://purl.org/NET/schemas/slug/config/userAgent" );
    
    public static final Resource MemoryHolder = m_model.createResource( "http://purl.org/NET/schemas/slug/config/MemoryHolder" );
    
    public static final Resource Scutter = m_model.createResource( "http://purl.org/NET/schemas/slug/config/Scutter" );
    
    public static final Resource Memory = m_model.createResource( "http://purl.org/NET/schemas/slug/config/Memory" );
    
    public static final Resource FileMemory = m_model.createResource( "http://purl.org/NET/schemas/slug/config/FileMemory" );
    
    public static final Resource DatabaseMemory = m_model.createResource( "http://purl.org/NET/schemas/slug/config/DatabaseMemory" );
    
    public static final Resource Component = m_model.createResource( "http://purl.org/NET/schemas/slug/config/Component" );
    
    public static final Resource Scanner = m_model.createResource( "http://purl.org/NET/schemas/slug/config/Scanner" );
    
}
