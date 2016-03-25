package edu.csus.ecs.pc2.services.web;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;



@Path("/problems")
@Produces(MediaType.APPLICATION_JSON)
public class ProblemService {
	
	
	public ProblemService(){
		super();
	}
	
	/**
	 * This method ...
	 * 
	 * @param 
	 * @return 
	 */
	@GET
//	@Path("getScoreboard")
//	@Produces(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
	public String getProblems() {
		
		
		//output the response to the requester (note that this actually returns it to Jersey, 
		// which converts it to JSON and forwards that to the caller as the HTTP.response).
//		return Response.status(Response.Status.OK).build(); 
	    
	    return "Problem list <here>" ;

	}
	

}