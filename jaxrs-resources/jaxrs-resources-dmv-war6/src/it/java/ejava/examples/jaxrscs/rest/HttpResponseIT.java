package ejava.examples.jaxrscs.rest;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ejava.examples.jaxrscs.jaxrs.HttpResponseTest;
import ejava.examples.jaxrscs.jaxrs.ResourcesTestConfig;

/**
 * This class implements a remote test of the service deployed to server. 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={ResourcesTestConfig.class, ResourcesITConfig.class})
public class HttpResponseIT extends HttpResponseTest {    
	//the @Tests are defined in the parent class
}
