package ejava.examples.jaxrsscale.dmv.client;

import ejava.examples.jaxrsscale.dmv.lic.dto.Application;

/**
 * This class implements the refund of a DMV application payment.
 */
public class RefundApplicationAction extends PutApplicationAction {
    public Application refund() {
        return super.put(null);
    }
}
