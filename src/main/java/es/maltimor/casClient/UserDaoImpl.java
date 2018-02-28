package es.maltimor.casClient;

import javax.security.auth.Subject;

import es.maltimor.genericUser.User;
import es.maltimor.genericUser.UserDao;

public class UserDaoImpl implements UserDao {

	public String getLogin() throws Exception {
		return CasFilterManager.getUser();
	}

	public User getUser(String login, String app) throws Exception {
		User user = new User();
		user.setLogin(login);
		if (login.equals("admin")) user.addRol("ADMIN");
		return user;
	}

	public User initUser(String login, String app) throws Exception {
		return getUser(login,app);
	}

}
