#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.rest;

import java.net.URI;

import javax.inject.Inject;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ${package}.rest.HelloTestConfig;
import ${package}.rest.HelloResourceTest;

/**
 * This class provides an integration test of the deployed service. It runs
 * during the integration-test phase after the application has been built 
 * and deployed to the server.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={
        HelloTestConfig.class, 
        HelloITConfig.class})
public class HelloResourceIT extends HelloResourceTest {
    @Inject
	protected URI serviceURI;  
	
	@Override
	public void setUp() throws Exception {
		log.info("serviceURI={}", serviceURI);
	}
}
