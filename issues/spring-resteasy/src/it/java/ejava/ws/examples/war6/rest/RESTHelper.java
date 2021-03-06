package ejava.ws.examples.war6.rest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	 * of the key is non-null.
	 * @param args
	 * @param key
	 * @param value
	 */
	public static void add(List<NameValuePair> args, String key, Object value) {
		if (value != null) {
			args.add(new BasicNameValuePair(key, value.toString()));
		}
	}	
	
	public static class Result<T> {
	    public int status;
	    public T entity;
	    public Result(int status, T entity) {
	        this.status = status;
	        this.entity = entity;
	    }
	}
	
	/**
	 * This helper function will perform a 
	 * @param clazz
	 * @param httpClient
	 * @param uri
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public static <T> Result<T> get(
	        Class<T> clazz, HttpClient httpClient, URI uri, NameValuePair...args) 
			throws IOException {
	    try {
            uri = URIUtils.createURI(
                    uri.getScheme(), 
                    uri.getHost(), 
                    uri.getPort(), 
                    uri.getPath(),
                    URLEncodedUtils.format(Arrays.asList(args),"UTF-8"),
                    null);
            log.debug(String.format("calling url=%s",uri));
            
            //make the service call
            HttpGet request = new HttpGet(uri);
            HttpResponse response=httpClient.execute(request);
            int status=response.getStatusLine().getStatusCode();
            log.debug(String.format("result=%d", status));
            
            //process the result
            InputStream is = response.getEntity().getContent();
            if (clazz.equals(String.class)) {
    			return new Result<T>(status, (T) IOUtils.toString(is));
    		}
    		else if (clazz.equals(byte[].class)) {
    			return new Result<T>(status, (T) IOUtils.toByteArray(is));
    		}
    		else {
    			//TODO: integrate in the JAXB demarshalling
    			return null;
    		}
	    } catch (URISyntaxException ex) {
	        throw new RuntimeException("error forming uri", ex);
        }
	}
}
