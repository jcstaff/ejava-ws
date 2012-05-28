package ejava.examples.ejbear6.dmv;

import java.net.URI;



import java.net.URISyntaxException;

import javax.ejb.SessionContext;
import javax.inject.Inject;



import javax.inject.Singleton;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.client.cache.CachingHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;

import ejava.common.test.stub.SessionContextStub;
import ejava.examples.ejbear6.dmv.client.ProtocolClient;
import ejava.examples.ejbear6.dmv.rs.ApplicationsRS;
import ejava.examples.ejbear6.dmv.rs.ApplicationsRSEJB;
import ejava.examples.ejbear6.dmv.rs.DmvRS;
import ejava.examples.ejbear6.dmv.rs.DmvRSEJB;
import ejava.examples.ejbear6.dmv.rs.PhotosRS;
import ejava.examples.ejbear6.dmv.rs.ResidentsRS;
import ejava.examples.ejbear6.dmv.svc.ApplicationsService;
import ejava.examples.ejbear6.dmv.svc.ApplicationsServiceStub;
import ejava.examples.ejbear6.dmv.svc.PhotosService;
import ejava.examples.ejbear6.dmv.svc.PhotosServiceStubEJB;
import ejava.examples.ejbear6.dmv.svc.ResidentsService;
import ejava.examples.ejbear6.dmv.svc.ResidentsServiceStubEJB;

/**
 * This class provides a factory for POJOs used for unit testing.
 */
@Configuration
@PropertySource("classpath:/test.properties")
public class DmvConfig {
    protected static final Logger log = LoggerFactory.getLogger(DmvConfig.class);
    
    @Inject
    public Environment env;

    @Bean
    public static PropertySourcesPlaceholderConfigurer properties() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public SessionContext sessionContext() {
        SessionContext ctx = new SessionContextStub();
        return ctx;
    }
    
    @Bean
    public DmvRS dmvRS() {
        return new DmvRSEJB();
    }

    @Bean @Singleton
    public ApplicationsService applicationsService() {
        return new ApplicationsServiceStub();
    }
    
    @Bean @Singleton
    public ResidentsService residentsService() {
        return new ResidentsServiceStubEJB();
    }
    
    @Bean @Singleton
    public PhotosService photosService() {
        return new PhotosServiceStubEJB();
    }
    
    //the following beans are used within the Jetty development env and are
    //shared between resteasy and spring
    @Bean @Singleton
    public ApplicationsRS applicationsRS() {
        return new ApplicationsRSEJB();
    }
    
    @Bean @Singleton
    public ResidentsRS residentsRS() {
        return new ResidentsRS();
    }
    
    @Bean @Singleton
    public PhotosRS photosRS() {
        return new PhotosRS();
    }
    
    @Bean @Singleton
    public HttpClient httpClient() {
        HttpClient httpClient = new DefaultHttpClient();

        log.info("creating cached HttpClient");
        CacheConfig cacheConfig = new CacheConfig();  
        cacheConfig.setMaxCacheEntries(1000);
        cacheConfig.setMaxObjectSizeBytes(8192);
        HttpClient httpClientCached = new CachingHttpClient(httpClient, cacheConfig);
        
        return httpClientCached;
    }
    
    @Bean 
    public URI dmvURI() {
        try {
            //this is the URI of the local jetty instance for unit testing
            String host=env.getProperty("host", "localhost");
            int port=Integer.parseInt(env.getProperty("port", "9092"));
            String path=env.getProperty("servletContext", "/");
            return new URI("http", null, host, port, path + "/dmv", null, null);
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
            throw new RuntimeException("error creating URI:" + ex, ex);
        }
    }

    @Bean
    public ProtocolClient dmv() {
        return new ProtocolClient();
    }
}
