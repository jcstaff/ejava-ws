package ejava.examples.restintro.dmv.resources;

import java.io.IOException;

import java.net.URI;
import java.util.Date;

import javax.inject.Inject;
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
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBException;

import org.jboss.resteasy.annotations.providers.jaxb.Formatted;

import ejava.examples.restintro.dmv.dto.Application;
import ejava.examples.restintro.dmv.dto.Applications;
import ejava.examples.restintro.dmv.dto.ResidentIDApplication;
import ejava.examples.restintro.dmv.svc.ApplicationsService;
import ejava.examples.restintro.dmv.svc.BadArgument;
import ejava.util.xml.JAXBHelper;

/**
 * This class implements hypermedia access to Applications.
 */
@Path("hlinks/applications")
public class ApplicationsHM {
    @Inject
    private ApplicationsService service;
    
    @Context
    private UriInfo uriInfo;

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Formatted
    public Response createApplication(ResidentIDApplication app) {
        try {
            Application createdApp = service.createApplication(app);
            URI cancel = uriInfo.getAbsolutePathBuilder()
                    .path(ApplicationsHM.class, "cancelApplication")
                    .build(createdApp.getId());
            URI approve = uriInfo.getAbsolutePathBuilder()
                    .path(ApplicationsHM.class, "approveApplication")
                    .build(createdApp.getId());
            URI reject = uriInfo.getAbsolutePathBuilder()
                    .path(ApplicationsHM.class, "rejectApplication")
                    .build(createdApp.getId());
            URI uri=uriInfo.getAbsolutePathBuilder()
                    .path(ApplicationsHM.class, "getApplicationById")
                    .build(createdApp.getId());
                //set the resource's next state transitions
            createdApp.resetLinks();
            createdApp.setApprove(approve);
            createdApp.setCancel(cancel);
            createdApp.setReject(reject);
            return Response
                    .created(uri)   //201-Created and a Location header of what was created
                    .entity(createdApp) //marshals the representation in response
                    .contentLocation(uri) //Content-Location header of representation
                    .type(MediaType.APPLICATION_XML) //Content-Type header of representation
                    .lastModified(createdApp.getUpdated()) //Last-Modified header of the representation
                    .build();
        } 
        catch (BadArgument ex) {
            return Response.status(Status.BAD_REQUEST)
                    .entity("client error:" + ex.getLocalizedMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }
        catch (Exception ex) {
            return Response.serverError()
                    .entity("server error:" + ex.getLocalizedMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }
    }

    @Path("{id}")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Formatted
    public Response getApplicationById(
            @PathParam("id") long id) {
        Application app = service.getApplication(id);
        if (app == null) {
            return Response.status(Status.NOT_FOUND)
                    .entity("unable to locate id:" + id)
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }
        
        URI uri=uriInfo.getAbsolutePath();
        return Response
            .ok(app, MediaType.APPLICATION_XML)
            .contentLocation(uri) //Content-Location header of representation
            .lastModified(app.getUpdated()) //Last-Modified header of the representation
            .build();
    }

    @Path("{id}")
    @PUT
    @Consumes(MediaType.APPLICATION_XML)
    @Formatted
    public Response updateApplication(String appString) {
        //marshal to string; demarshal locally to have more control over transform 
        try {
            Application app = JAXBHelper.unmarshall(appString, Application.class, null, 
                    Application.class,
                    ResidentIDApplication.class);
            int status=0;
            if ((status=service.updateApplication(app))<0) {
                return Response.status(Status.BAD_REQUEST)
                        .entity("unable to update application - client error")
                        .type(MediaType.TEXT_PLAIN)
                        .build();
            }
            else if (status > 0) {
                Application existingApp = service.getApplication(app.getId());
                return Response.status(Status.CONFLICT)
                        .entity(existingApp)
                        .type(MediaType.APPLICATION_XML)
                        .contentLocation(uriInfo.getAbsolutePath())
                        .lastModified(existingApp.getUpdated())
                        .build();
            }
            return Response.noContent().build();
        } catch (JAXBException ex) {
            ex.printStackTrace();
            return Response.serverError()
                           .entity("JAXBException handling updateApplication:" + ex)
                           .type(MediaType.TEXT_PLAIN)
                           .build();
        } catch (IOException ex) {
            ex.printStackTrace();
            return Response.serverError()
                    .entity("IOException handling updateApplication:" + ex)
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        } finally {}
    }

    @Path("{id}")
    @DELETE
    public Response deleteApplication(
            @PathParam("id") long id) {
        int status=0;
        if ((status=service.deleteApplication(id)) < 0) {
            return Response.status(Status.NOT_FOUND)
                    .entity("unable to locate application:" + id)
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        } 
        else if (status > 0) {
            return Response.status(405)
                    .header("Allow", "GET, HEAD")
                    .entity("completed application cannot be deleted")
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }
            //application deleted
        return Response.noContent()
                    .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Formatted
    public Response getApplications(
            @QueryParam("active") Boolean active, 
            @QueryParam("start") int start, 
            @QueryParam("count") int count) {
        
        //get the requested resource
        Applications apps=service.getApplications(active, start, count);
        
        //determine lastModified application or current time if 
        Date lastModified = null;
        if (apps.size() > 0) {
            for (Application app : apps) {
                if (lastModified == null || app.getUpdated().getTime() > lastModified.getTime()) {
                    lastModified=app.getUpdated();
                }
            }
        }
        else {
            lastModified=new Date();
        }
        
        URI uri=uriInfo.getAbsolutePath();
        return Response
            .ok(apps, MediaType.APPLICATION_XML)
            .contentLocation(uri) //Content-Location header of representation
            .lastModified(lastModified) //Last-Modified header of the representation
            .build();
    }
    
    @DELETE
    public void purgeApplications() {
        service.purgeApplications();
    }
    
    @Path("{id}/cancel")
    @POST
    public Response cancelApplication(
            @PathParam("id")long id) {
        return null;
    }

    @Path("{id}/approve")
    @POST
    public Response approveApplication(
            @PathParam("id")long id) {
        return null;
    }
    
    @Path("{id}/reject")
    @POST
    public Response rejectApplication(
            @PathParam("id")long id) {
        return null;
    }
    
}
