package ejava.rs.util;

import java.io.IOException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.HttpHeaders;
import javax.xml.bind.JAXBException;
import javax.xml.validation.Schema;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ejava.util.rest.HttpResult;
import ejava.util.xml.JAXBHelper;

/**
 * This class provides helper methods to turn the HttpClient library into 
 * an ready-to-use raw REST client.
 */
public class RESTHelper {
    private static final Logger log = LoggerFactory.getLogger(RESTHelper.class);
    
    
	/**
	 * This helper function will return a new args list.
	 * @return
	 */
	public static List<NameValuePair> createArgsList() {
		return new ArrayList<NameValuePair>();
	}
	
	/**
	 * This helper function will add the key/value to the args if the value
	 * of the key is non-null. This is useful in eliminating optional args
	 * that have not been specified.
	 * @param args
	 * @param key
	 * @param value
	 */
	public static void add(List<NameValuePair> args, String key, Object value) {
		if (value != null) {
			args.add(new BasicNameValuePair(key, value.toString()));
		}
	}	

	/**
	 * This helper method converts a NameValuePair list to an array.
	 * @param params
	 * @return
	 */
	public static NameValuePair[] toArray(List<NameValuePair> params) {
	    NameValuePair nvp[] = new NameValuePair[params.size()];
	    return params.toArray(nvp);
    }
	
	/**
	 * This helper function will perform a GET to the provided URI with
	 * provided query args.
	 * @param clazz
	 * @param httpClient
	 * @param uri
	 * @return
	 * @throws IOException
	 * @throws JAXBException 
	 * @throws URISyntaxException 
	 */
	public static <T> HttpResult<T> get(
	        Class<T> clazz, HttpClient httpClient, URI uri, 
	        Schema schema, Header headers[], NameValuePair...params) 
			throws IOException, JAXBException, URISyntaxException {
	    uri = getURI(uri, params);
	    
        //make the service call
        HttpGet request = new HttpGet(uri);
        if (headers != null) {
            for (Header header : headers) {
                request.addHeader(header);
            }
        }
        log.debug("calling GET {}",uri);
        HttpResponse response=httpClient.execute(request);
        return HttpResult.getResult(clazz, schema, response);
	}
	
    public static <T> HttpResult<T> put(
            Class<T> clazz, HttpClient httpClient, URI uri, 
            Schema schema, Header[] headers, byte[] entity, NameValuePair...params) 
            throws IOException, JAXBException, URISyntaxException {
        uri = getURI(uri, params);
        
        //make the service call
        HttpPut request = new HttpPut(uri);
        if (headers != null) {
            for (Header header : headers) {
                request.addHeader(header);
            }
        }
        if (entity != null) {
            HttpEntity httpEntity = new ByteArrayEntity(entity); 
            request.setEntity(httpEntity);
        }
        log.debug("calling PUT {}\n{}",uri,IOUtils.toString(request.getEntity().getContent()));
        HttpResponse response=httpClient.execute(request);
        return HttpResult.getResult(clazz, schema, response);
    }

    public static <T> HttpResult<T> delete(
            Class<T> clazz, HttpClient httpClient, URI uri, 
            Schema schema, Header[] headers, NameValuePair...params) 
            throws IOException, JAXBException, URISyntaxException {
        uri = getURI(uri, params);
        
        //make the service call
        HttpDelete request = new HttpDelete(uri);
        if (headers != null) {
            for (Header header : headers) {
                request.addHeader(header);
            }
        }
        log.debug("calling DELETE {}",uri);
        HttpResponse response=httpClient.execute(request);
        return HttpResult.getResult(clazz, schema, response);
    }

    public static <T> HttpResult<T> post(
            Class<T> clazz, HttpClient httpClient, URI uri, 
            Schema schema, Header headers[], NameValuePair...params) 
            throws IOException, JAXBException {
        
        //make the service call        
        HttpPost request = new HttpPost(uri);
        if (headers != null) {
            request.setHeaders(headers);
        }
        request.setEntity(new UrlEncodedFormEntity(Arrays.asList(params)));
        log.debug("calling POST {}\n{}",uri,IOUtils.toString(request.getEntity().getContent()));
        HttpResponse response=httpClient.execute(request);
        return HttpResult.getResult(clazz, schema, response);
    }

    public static <T> HttpResult<T> postXML(
            Class<T> clazz, HttpClient httpClient, URI uri, 
            Schema schema, Header headers[], String entityXML) 
            throws IOException, JAXBException, URISyntaxException {
        
        //make the service call        
        HttpPost request = new HttpPost(uri);
        request.addHeader("Content-Type", "application/xml");
        if (headers != null) {
            request.setHeaders(headers);
        }
        request.setEntity(new StringEntity(entityXML, "UTF-8"));
        log.debug("calling POST {}\n{}",uri,IOUtils.toString(request.getEntity().getContent()));
        HttpResponse response=httpClient.execute(request);
        if (response.getStatusLine().getStatusCode() == 302) {
            String sslUri=response.getFirstHeader(HttpHeaders.LOCATION).getValue();
            EntityUtils.consume(response.getEntity());
            request.setURI(new URI(sslUri));
            response=httpClient.execute(request);
        }
        return HttpResult.getResult(clazz, schema, response);
    }
    
    /**
	 * This helper function will form a URI with the optional args 
	 * URLEncoded into the query string.
	 * @param uri
	 * @param args
	 * @return
	 * @throws URISyntaxException
	 */
	protected static final URI getURI(URI uri, NameValuePair...args) 
	        throws URISyntaxException {
        return URIUtils.createURI(
                uri.getScheme(), 
                uri.getHost(), 
                uri.getPort(), 
                uri.getPath(),
                URLEncodedUtils.format(Arrays.asList(args),"UTF-8"),
                null);	    
	}
	

	/**
	 * This helper method defines a wrapper around the core get() method but
	 * replaces all checked exceptions thrown with a RuntimeException.
	 * @param clazz
	 * @param httpClient
	 * @param uri
	 * @param schema
	 * @param params
	 * @return
	 */
	public static final <T> HttpResult<T> getX(
            Class<T> clazz, HttpClient httpClient, String uri, 
            Schema schema, Header[] headers, NameValuePair...params) {
        try {
            return get(clazz, httpClient, new URI(uri), schema, headers, params);
        } catch (IOException ex) {
            throw new RuntimeException("IOException:" + ex.getLocalizedMessage(), ex);
        } catch (JAXBException ex) {
            throw new RuntimeException("JAXBException:" + ex.getLocalizedMessage(), ex);
        } catch (URISyntaxException ex) {
            throw new RuntimeException("URISyntaxException:" + ex.getLocalizedMessage(), ex);
        } finally {}
    }

    public static final <T> HttpResult<T> putX(
            Class<T> clazz, HttpClient httpClient, String uri, Header[] headers, 
            Schema schema, byte[] entity, NameValuePair...params) {
        try {
            return put(clazz, httpClient, new URI(uri), schema, headers, entity, params); 
        } catch (IOException ex) {
            throw new RuntimeException("IOException:" + ex.getLocalizedMessage(), ex);
        } catch (JAXBException ex) {
            throw new RuntimeException("JAXBException:" + ex.getLocalizedMessage(), ex);
        } catch (URISyntaxException ex) {
            throw new RuntimeException("URISyntaxException:" + ex.getLocalizedMessage(), ex);
        } finally {}
    }
    
    public static final <T> HttpResult<T> putXML(
            Class<T> clazz, HttpClient httpClient, String uri, 
            Schema schema, Object entity, NameValuePair...params) {
        try {
            Header[] headers = new Header[] { new BasicHeader("Content-Type", "application/xml") };
            return put(clazz, httpClient, new URI(uri), schema, headers, 
                    JAXBHelper.marshall(entity, schema), params); 
        } catch (IOException ex) {
            throw new RuntimeException("IOException:" + ex.getLocalizedMessage(), ex);
        } catch (JAXBException ex) {
            throw new RuntimeException("JAXBException:" + ex.getLocalizedMessage(), ex);
        } catch (URISyntaxException ex) {
            throw new RuntimeException("URISyntaxException:" + ex.getLocalizedMessage(), ex);
        } finally {}
    }

    public static final <T> HttpResult<T> deleteX(Class<T> clazz, HttpClient httpClient,
            String uri, Schema schema, Header headers[], NameValuePair...params) {
        try {
            return delete(clazz, httpClient, new URI(uri), schema, headers, params);
        } catch (IOException ex) {
            throw new RuntimeException("IOException:" + ex.getLocalizedMessage(), ex);
        } catch (JAXBException ex) {
            throw new RuntimeException("JAXBException:" + ex.getLocalizedMessage(), ex);
        } catch (URISyntaxException ex) {
            throw new RuntimeException("URISyntaxException:" + ex.getLocalizedMessage(), ex);
        } finally {}
    }
    
    public static final <T> HttpResult<T> postX(Class<T> clazz, HttpClient httpClient,
            String uri, Schema schema, Header headers[], NameValuePair...params) {
        try {
            return post(clazz, httpClient, new URI(uri), schema, headers, params);
        } catch (IOException ex) {
            throw new RuntimeException("IOException:" + ex.getLocalizedMessage(), ex);
        } catch (JAXBException ex) {
            throw new RuntimeException("JAXBException:" + ex.getLocalizedMessage(), ex);
        } catch (URISyntaxException ex) {
            throw new RuntimeException("URISyntaxException:" + ex.getLocalizedMessage(), ex);
        } finally {}
    }

    public static <T> HttpResult<T> postXMLX(
            Class<T> clazz, HttpClient httpClient, URI uri, 
            Schema schema, Header headers[], Object entity) {
        try {
            String xml = JAXBHelper.toString(entity);
            return postXML(clazz, httpClient, uri, schema, headers, xml);
        } catch (IOException ex) {
            throw new RuntimeException("IOException:" + ex.getLocalizedMessage(), ex);
        } catch (JAXBException ex) {
            throw new RuntimeException("JAXBException:" + ex.getLocalizedMessage(), ex);
        } catch (URISyntaxException ex) {
            throw new RuntimeException("URISyntaxException:" + ex.getLocalizedMessage(), ex);
        } finally {}
    }
}
