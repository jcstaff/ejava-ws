package ejava.examples.jaxrsscale.rest;

import org.junit.runner.RunWith;


import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ejava.examples.jaxrsscale.jaxrs.CachingTest;
import ejava.examples.jaxrsscale.jaxrs.ScaleTestConfig;

/**
 * This class implements a remote test of the service deployed to server. 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={ScaleTestConfig.class, RepresentationsITConfig.class})
public class CachingIT extends CachingTest {    
	//the @Tests are defined in the parent class
}
