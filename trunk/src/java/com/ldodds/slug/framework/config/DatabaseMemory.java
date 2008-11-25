package com.ldodds.slug.framework.config;

import com.hp.hpl.jena.db.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.DoesNotExistException;

class DatabaseMemory extends MemoryImpl 
{
  private String _user;
  private String _pass;
  private String _modelURI;
  private String _dbURL;
  private String _dbName;
  private String _driver;
  
  public DatabaseMemory(String user, String pass, 
      String modelURI, String dbURL, String dbName, 
      String driver) 
  {
    _user = user;
    _pass = pass;
    _modelURI = modelURI;
    _dbURL = dbURL;
    _dbName = dbName;
    _driver = driver;
  }

  public Model load() throws Exception 
  {
    if (_model != null)
    {
      return _model;
    }
    Class.forName(_driver);
    DBConnection dbConnection = 
      new DBConnection(_dbURL, _user, _pass, _dbName);
    
    try
    {
      _model = ModelRDB.open(dbConnection, _modelURI);
    } catch (DoesNotExistException e)
    {
      _model = ModelRDB.createModel(dbConnection, _modelURI);
    }
    
    return _model;
  }

  public void save() throws Exception 
  {
    _model.close();
  }

}
