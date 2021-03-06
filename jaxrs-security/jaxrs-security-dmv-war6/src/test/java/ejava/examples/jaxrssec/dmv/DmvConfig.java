package ejava.examples.jaxrssec.dmv;

import java.io.File;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;

import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;

import javax.ejb.SessionContext;
import javax.inject.Inject;

import javax.inject.Singleton;
import javax.ws.rs.core.UriBuilder;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.client.cache.CachingHttpClient;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;

import ejava.common.test.stub.SessionContextStub;
import ejava.examples.jaxrssec.dmv.client.ProtocolClient;
import ejava.examples.jaxrssec.dmv.rs.ApplicationsRS;
import ejava.examples.jaxrssec.dmv.rs.ApplicationsRSEJB;
import ejava.examples.jaxrssec.dmv.rs.DmvRSEJB;
import ejava.examples.jaxrssec.dmv.rs.PhotosRS;
import ejava.examples.jaxrssec.dmv.rs.ResidentsRS;
import ejava.examples.jaxrssec.dmv.svc.ApplicationsService;
import ejava.examples.jaxrssec.dmv.svc.ApplicationsServiceStub;
import ejava.examples.jaxrssec.dmv.svc.PhotosService;
import ejava.examples.jaxrssec.dmv.svc.PhotosServiceStubEJB;
import ejava.examples.jaxrssec.dmv.svc.ResidentsService;
import ejava.examples.jaxrssec.dmv.svc.ResidentsServiceStubEJB;

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
    public DmvRSEJB dmvRS() {
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
    
    /**
     * HttpClient, by default, redirects GET and HEAD to new Locations with
     * the previous method and GET for all other types. We provide this class
     * to configure HttpClient to be more liberal with re-directs and to
     * re-issue the same method at the new Location since that is what has 
     * to happen when encountering an HTTP->HTTPS redirect. 
     */
    private class FollowRedirectStrategy extends DefaultRedirectStrategy {
        /**
         * Without a rule like this in place -- the caller will get a 302 back
         * for a DELETE when issued to an HTTP URL and being re-directed to
         * an HTTPS URL. With this rule in place -- the DELETE will be 
         * automatically redirected to the new HTTP(S) URL.
         */
        @Override
        public boolean isRedirected(HttpRequest request,
                HttpResponse response, HttpContext context)
                throws ProtocolException {
            boolean isRedirect=false;
            try {
                isRedirect=super.isRedirected(request, response, context);
            } catch (ProtocolException ex) {
                throw new RuntimeException("ProtocolException durint isRedirected:" 
                        + ex.getLocalizedMessage());
            }
            //add a rule that will cause DELETE to redirect
            if (!isRedirect) {
                String method = request.getRequestLine().getMethod();
                if (method.equalsIgnoreCase(HttpDelete.METHOD_NAME)) {
                    int status = response.getStatusLine().getStatusCode();
                    return (status == 301 || status == 302);
                }
            }
            return false;
        }
        
        /**
         * Without this rule in place, HttpClient will issue a GET for re-issued
         * POST, PUT, and DELETE calls.
         */
        @Override
        public HttpUriRequest getRedirect(
                final HttpRequest request,
                final HttpResponse response,
                final HttpContext context) throws ProtocolException {
            URI uri = getLocationURI(request, response, context);
            String method = request.getRequestLine().getMethod();            
            if (method.equalsIgnoreCase(HttpHead.METHOD_NAME)) {
                return new HttpHead(uri);
            }
            else if (method.equalsIgnoreCase(HttpDelete.METHOD_NAME)) {
                return new HttpDelete(uri);
            }
            //these types will have payloads that we cannot pickup here
            //else if (method.equalsIgnoreCase(HttpPut.METHOD_NAME)) {
            //    return new HttpPut(uri);
            //}
            //else if (method.equalsIgnoreCase(HttpPost.METHOD_NAME)) {
            //    return new HttpPost(uri);
            //}
            else {
                return new HttpGet(uri);
            }
        }
    }
    
    public HttpClient createClient(String username, String password) 
            throws KeyStoreException, IOException, GeneralSecurityException {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        httpClient.setRedirectStrategy(new FollowRedirectStrategy());
        
        //setup SSL
        
        //there is an issue where some of the protocol exchange attempts to use
        //TLSv2 -- this post provided a quick solution
        //http://stackoverflow.com/questions/9828414/receiving-sslhandshakeexception-handshake-failure-despite-my-client-ignoring-al
        //System.setProperty("https.protocols", "TLSv1");
        
        //get the truststore with the server's cert
        String trustStorePath=env.getProperty("javax.net.ssl.trustStore");        
        SSLSocketFactory socketFactory = null;
        if (trustStorePath != null) {
            log.info("using truststore: {}", trustStorePath);
            String trustStorePassword=env.getProperty("javax.net.ssl.trustStorePassword");
            KeyStore trustStore  = KeyStore.getInstance(KeyStore.getDefaultType());
            FileInputStream instream = new FileInputStream(new File(trustStorePath));
            try {
                trustStore.load(instream, 
                        trustStorePassword==null?null : trustStorePassword.toCharArray());
                //register the truststore with the networking
                socketFactory = new SSLSocketFactory(trustStore);
            } finally {
                try { instream.close(); } catch (Exception ignore) {}
            }
        }
        else {
            log.info("*****************************************");
            log.info("not using truststore, accepting all certs");
            log.info("*****************************************");
            socketFactory = new SSLSocketFactory(
                    new TrustSelfSignedStrategy(), 
                    SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        }
        Scheme sch = new Scheme("https", 443, socketFactory); //default SSL port=443
        httpClient.getConnectionManager().getSchemeRegistry().register(sch);
        
        

        //Add user identity and credentials
        if (username != null) {
            httpClient.getCredentialsProvider().setCredentials(
                    new AuthScope(null, -1, "ApplicationRealm"), 
                    new UsernamePasswordCredentials(username, password));
        }

        //Add the cache decorator
        CacheConfig cacheConfig = new CacheConfig();  
        cacheConfig.setMaxCacheEntries(1000);
        cacheConfig.setMaxObjectSize(8192);
        HttpClient httpClientCached = new CachingHttpClient(httpClient, cacheConfig);
        return httpClientCached;
        /*
        return httpClient;
        */
    }
    
    @Bean @Singleton
    public HttpClient httpClient() throws KeyStoreException, IOException, GeneralSecurityException {
        log.info("creating anonymous HttpClient");
        return createClient(null, null);
    }
    
    
    @Bean @Singleton
    public HttpClient adminClient() throws KeyStoreException, IOException, GeneralSecurityException {        
        log.info("creating admin HttpClient");
        String username = env.getProperty("admin.username", "admin1");
        String password = env.getProperty("admin.password", "password");
        return createClient(username, password);
    }
    
    @Bean @Singleton
    public HttpClient userClient() throws KeyStoreException, IOException, GeneralSecurityException {
        log.info("creating user HttpClient");
        log.info("creating admin HttpClient");
        String username = env.getProperty("user.username", "user1");
        String password = env.getProperty("user.password", "password");
        return createClient(username, password);
    }
    
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
                env.getProperty("http.server.port")
                ));
            String scheme=env.getProperty("scheme","http");
            String path=env.getProperty("servletContext", "/");
            URI uri = new URI(scheme, null, host, port, path, null, null);
            return uri;
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
            throw new RuntimeException("error creating URI:" + ex, ex);
        }
    }

    @Bean 
    public URI dmvURI() {
        return UriBuilder.fromUri(appURI())
                .path("rest")
                .path(DmvRSEJB.class)
                .build();
    }

    /**
     * Return full URI to the applications REST service
     * @return
     */
    @Bean 
    public URI dmvlicURI() {
        URI uri = UriBuilder.fromUri(appURI())
                .path("rest")
                .path(ApplicationsRS.class)
                .build(); 
        return uri;
    }


    @Bean
    public ProtocolClient dmv() {
        return new ProtocolClient();
    }
}
