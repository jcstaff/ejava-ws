package ejava.examples.ejbear6.dmv.client;

import ejava.examples.ejbear6.dmv.lic.dto.Application;

/**
 * This class implements the rejection of a DMV application.
 */
public class RejectApplicationAction extends PutApplicationAction {
    public Application approve() {
        return super.put(null);
    }
}
