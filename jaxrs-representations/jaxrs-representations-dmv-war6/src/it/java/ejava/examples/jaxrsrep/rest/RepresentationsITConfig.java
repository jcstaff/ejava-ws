package ejava.examples.jaxrsrep.rest;

import java.net.MalformedURLException;

import java.net.URI;
import java.net.URL;

import java.net.URISyntaxException;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

/**
 * This class provides the Spring Integration Test configuration. It will
 * be used to override or augment the unit test configuration.
 */
@Configuration
@PropertySource(value="classpath:it.properties")
public class RepresentationsITConfig {
    static final Logger log = LoggerFactory.getLogger(RepresentationsITConfig.class);
    
    protected @Inject Environment env;
    
    @Bean
    public URI appURI() {
        try {
            String host=env.getProperty("host", "localhost");
            int port=Integer.parseInt(env.getProperty("port", "8080"));
            String path=env.getProperty("servletContext", "/");
            URL url=new URL("http", host, port, path);
            log.debug("server URI={}", url.toURI());
            return url.toURI();
        } catch (MalformedURLException ex) {
            throw new RuntimeException("error creating URL:" + ex, ex);
        } catch (URISyntaxException ex) {
            throw new RuntimeException("error creating URI:" + ex, ex);
        }
    }
}