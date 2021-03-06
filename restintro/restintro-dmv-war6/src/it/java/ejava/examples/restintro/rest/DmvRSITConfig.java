package ejava.examples.restintro.rest;

import java.net.URI;


import java.net.URISyntaxException;

import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;

import org.mortbay.jetty.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import ejava.examples.restintro.dmv.rs.ApplicationsRS;
import ejava.examples.restintro.dmv.svc.ApplicationsService;

/**
 * This class provides the Spring Integration Test configuration. It will
 * be used to override or augment the unit test configuration.
 */
@Configuration
@PropertySource(value="classpath:it.properties")
public class DmvRSITConfig {
    static final Logger log = LoggerFactory.getLogger(DmvRSITConfig.class);
    
    protected @Inject Environment env;
    
    /**
     * Return the full URI to the base servlet context
     * @return
     */
    @Bean 
    public URI appURI() {
        try {
            //this is the URI of the local jetty instance for unit testing
            String host=env.getProperty("host", "localhost");
            //default to http.server.port and allow a http.client.port override
            int port=Integer.parseInt(env.getProperty("http.client.port",
                env.getProperty("http.server.port", "8080")
                ));
            String path=env.getProperty("servletContext", "/");
            URI uri = new URI("http", null, host, port, path, null, null);
            return uri;
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
            throw new RuntimeException("error creating URI:" + ex, ex);
        }
    }

    /**
     * Create a primary URI to the service under test.
     * @return
     */
    @Bean
    public URI serviceURI() {
        URI uri = UriBuilder.fromUri(appURI())
                .path("rest")
                .build();
        return uri;
    }
    
    /**
     * Tells the proxy which JAX-RS implementation to contact.
     */
    @Bean String implContext() { return ""; }

    /**
     * Defines the protocol types allowed.
     * @return
     */
    @Bean String protocol() { return "application/xml"; }

    @Bean 
    public ApplicationsService applicationsService() {
        return new ApplicationsServiceProxy();
    }

    @Bean 
    public URI dmvlicURI() {
        URI uri = UriBuilder.fromUri(appURI())
                .path("rest")
                .path(ApplicationsRS.class)
                .build();
        return uri;
    }
    
    //turn off the unit test HTTP server
    @Bean
    public Server server() throws Exception {
        return null;
    }
}
