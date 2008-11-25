package com.ldodds.slug.framework.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Seq;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;
import com.ldodds.slug.vocabulary.CONFIG;

public class ComponentFactory 
{
  public ComponentFactory() 
  {
  }

  public Component instantiate(Model model, String id)
    throws ClassNotFoundException, IllegalAccessException, 
    InstantiationException
  {
    Resource resource = model.getResource(id);
    return instantiate(resource);
  }
  
  public Component instantiate(Resource resource)
    throws ClassNotFoundException, IllegalAccessException, 
      InstantiationException
  {
    if ( !resource.hasProperty(CONFIG.impl) )
    {
      return null;
    }
    
    Class clazz = Class.forName(
        resource.getProperty(CONFIG.impl).getString());
    Object obj = clazz.newInstance();
    if (! (obj instanceof Component) )
    {
      throw new RuntimeException("Configured class doesn't implement Component");
    }
    Component component = (Component)obj;
    component.configure(resource);
    return component;
  }
  
  public List instantiate(List resources)
  throws ClassNotFoundException, IllegalAccessException, 
    InstantiationException
  {
    if (resources == null)
    {
      return Collections.EMPTY_LIST;
    }
    List instantiated = new ArrayList();
    
    for (int i=0; i<resources.size(); i++)
    {
      Resource resource = (Resource)resources.get(i);
      instantiated.add( instantiate(resource) );
    }
    return instantiated;
  }
  
  public List instantiate(Seq sequence)
  throws ClassNotFoundException, IllegalAccessException, 
    InstantiationException  
  {
    if (sequence == null)
    {
      return Collections.EMPTY_LIST;
    }
    List instantiated = new ArrayList();
    for (int i=1; i<=sequence.size(); i++)
    {
      Resource resource = (Resource)sequence.getResource(i);
      instantiated.add( instantiate(resource) );
    }
    return instantiated;
  }
  
  public List instantiateComponentsFor(Resource resource, Property property)
  throws ClassNotFoundException, IllegalAccessException, 
    InstantiationException
  {
    if (!resource.hasProperty(property))
    {
      return Collections.EMPTY_LIST;
    }
    
    Statement statement = resource.getProperty(property);
    if (!statement.getObject().isResource())
    {
      throw new IllegalArgumentException("Property value is not a resource");
    }
    Resource value = (Resource)statement.getObject();
    if (value.hasProperty(RDF.type, RDF.Seq))
    {
      return instantiate( resource.getProperty(property).getSeq() );
    }
    else
    {
      Component component = instantiate(value);
      if (component != null)
      {
        return Collections.singletonList( component );
      }
      
    }
    return Collections.EMPTY_LIST;
  }
}
