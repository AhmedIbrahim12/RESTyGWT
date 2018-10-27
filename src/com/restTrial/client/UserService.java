package com.restTrial.client;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;

import com.restTrial.shared.User;

@Path("http://localhost:8088")
public interface UserService extends RestService {
	@POST
	@Path("/addUser")
	public void saveUser(@QueryParam("username") String username, MethodCallback<User> callback);

	@GET
	@Path("/fetchAllUsers")
	public void getAllUsers(MethodCallback<List<User>> callback);
}
