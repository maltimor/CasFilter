package es.maltimor.casClient;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import es.maltimor.genericUser.UserDao;

@Path("/pruebaService/")
@Consumes("application/json")
@Produces("application/json")
public class PruebaService {
	private UserDao userDao;

	public UserDao getUserDao() {
		return userDao;
	}

	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}
	
	@GET
	@Path("/prueba/{id}")
	public Response prueba(@PathParam("id") String id,@QueryParam("name") String name){
		System.out.println("Prueba:"+id+" name="+name);

		String login="???";
		try {
			login = userDao.getLogin()+" "+id+" "+name;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return Response.ok(login).build();
	}
}
