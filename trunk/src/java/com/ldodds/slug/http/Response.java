package com.ldodds.slug.http;

import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * @author ldodds
 */
public class Response
{
    private URL _url;
    private Map<String,List<String>> _responseHeaders;
    private StringBuffer _content;
    
    public Response(URL url, Map<String,List<String>> headers, StringBuffer content)
    {
        _url = url;
        _responseHeaders = headers;
        _content = content;
    }
    
    public URL getRequestURL()
    {
        return _url;
    }
    
    public Map<String,List<String>> getHeaders()
    {
        return _responseHeaders;
    }
    
    public StringBuffer getContent()
    {
        return _content;
    }    
}
