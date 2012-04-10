package ejava.examples.restintro.dmv.client;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import ejava.examples.restintro.dmv.dto.Application;
import ejava.examples.restintro.dmv.dto.Representation;
import ejava.rs.util.RESTHelper;
import ejava.rs.util.RESTHelper.Result;

/**
 * This class implements getting an existing DMV application.
 */
public class GetApplicationAction extends Action {
    protected Result<Application> result;
    
    public Application get() {
        try {
            HttpGet request = new HttpGet(link.getHref());
            request.addHeader("Accept", Representation.DMVLIC_MEDIA_TYPE);
    
            log.debug("calling {} {}", request.getMethod(), request.getURI());
            HttpResponse response=httpClient.execute(request);
            result = RESTHelper.getResult(Application.class, null, response);
            if (result.status >= 200 && result.status <= 299) {
                return result.entity;
            }
            else {
                log.warn(String.format("error calling %s %s, %d:%s",
                        request.getMethod(), link,
                        result.status, result.errorMsg));
                return null;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException("IO error reading stream", ex);
        } catch (JAXBException ex) {
            ex.printStackTrace();
            throw new RuntimeException("JAXB error demarshalling result", ex);
        } finally {}        
    }

    @Override
    protected Result<?> getResult() {
        return result;
    }
}
