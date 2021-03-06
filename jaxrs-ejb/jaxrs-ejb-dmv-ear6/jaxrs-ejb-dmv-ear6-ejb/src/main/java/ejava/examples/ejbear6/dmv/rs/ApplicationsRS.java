package ejava.examples.ejbear6.dmv.rs;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.jboss.resteasy.annotations.providers.jaxb.Formatted;

import ejava.examples.ejbear6.dmv.lic.dto.DrvLicRepresentation;
import ejava.examples.ejbear6.dmv.lic.dto.ResidentIDApplication;

@Path("applications")
public interface ApplicationsRS {

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Formatted
    public abstract Response createApplication(
            ResidentIDApplication app,
            @Context UriInfo uriInfo);

    /**
     * This method implements an initial version of the hypermedia protocol
     * using flat XML reference links. Note the version# in the produces.
     */
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces("application/dmvlic.ejava.0+xml")
    @Formatted
    public abstract Response createApplicationHM(
            ResidentIDApplication app,
            @Context UriInfo uriInfo);

    /**
     * This method represents another step closer to the targeted hypermedia
     * protocol. It generates complex link structures that can express more 
     * metadata about the transition.
     * @param app
     * @return
     */
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces("application/dmvlic.ejava.2+xml")
    @Formatted
    public abstract Response createApplicationHM2(
            ResidentIDApplication app,
            @Context UriInfo uriInfo);

    @Path("{id}")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Formatted
    public abstract Response getApplicationById(
            @PathParam("id") long id,
            @Context UriInfo uriInfo);

    @Path("{id}")
    @PUT
    @Consumes(MediaType.APPLICATION_XML)
    @Formatted
    public abstract Response updateApplication(
            String appString,
            @Context UriInfo uriInfo);

    @Path("{id}")
    @DELETE
    public abstract Response deleteApplication(
            @PathParam("id") long id,
            @Context UriInfo uriInfo);

    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Formatted
    public abstract Response getApplications(
            @QueryParam("active") Boolean active,
            @QueryParam("start") int start, 
            @QueryParam("count") int count,
            @Context UriInfo uriInfo);

    @DELETE
    public abstract void purgeApplications();

    @POST
    @Consumes(DrvLicRepresentation.DRVLIC_MEDIA_TYPE)
    @Produces(DrvLicRepresentation.DRVLIC_MEDIA_TYPE)
    @Formatted
    public abstract Response createApplicationRep(
            ResidentIDApplication app,
            @Context UriInfo uriInfo);

    @Path("{id}")
    @GET
    @Produces(DrvLicRepresentation.DRVLIC_MEDIA_TYPE)
    @Formatted
    public abstract Response getApplication(
            @PathParam("id") long id,
            @Context UriInfo uriInfo);

    @Path("{id}/cancel")
    @DELETE
    public abstract Response cancelApplication(
            @PathParam("id") long id,
            @Context UriInfo uriInfo);

    @Path("{id}/approve")
    @PUT
    @Produces(DrvLicRepresentation.DRVLIC_MEDIA_TYPE)
    @Formatted
    public abstract Response approveApplication(
            @PathParam("id") long id,
            @Context UriInfo uriInfo);

    @Path("{id}/reject")
    @PUT
    @Produces(DrvLicRepresentation.DRVLIC_MEDIA_TYPE)
    @Formatted
    public abstract Response rejectApplication(
            @PathParam("id") long id,
            @Context UriInfo uriInfo);

    @Path("{id}/payment")
    @PUT
    @Produces(DrvLicRepresentation.DRVLIC_MEDIA_TYPE)
    @Formatted
    public abstract Response payApplication(
            @PathParam("id") long id,
            @Context UriInfo uriInfo);

    @Path("{id}/refund")
    @PUT
    @Produces(DrvLicRepresentation.DRVLIC_MEDIA_TYPE)
    @Formatted
    public abstract Response refundApplicationPayment(
            @PathParam("id") long id,
            @Context UriInfo uriInfo);
}