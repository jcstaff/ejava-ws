package ejava.examples.ejb31.rest;

import static org.junit.Assert.*;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ejava.examples.ejb31.rest.HelloResource;

/**
 * This class provides a local unit test of the service facade. All 
 * configuration is expressed by the ContextConfiguration classes.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={HelloTestConfig.class})
public class HelloResourceTest {
	protected static final Logger log = LoggerFactory.getLogger(HelloResourceTest.class);

	@Inject
	protected HelloResource restImpl;
	
	@Before
	public void setUp() throws Exception {
	    log.debug("restImpl={}", restImpl);
        log.debug("restImpl.impl={}", restImpl.impl);
	}
	
	@Test
	public void testHelloEJB31() {
		log.info("*** testHelloEJB31 ***");
		assertEquals("unexpected reply", 
				"Hello world, ctx.identity=anonymous\n", 
				restImpl.sayHelloREST("world"));
	}
}