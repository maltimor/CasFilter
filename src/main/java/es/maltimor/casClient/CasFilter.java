package es.maltimor.casClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class CasFilter implements Filter {
	private String casServerPrefix;
	private String logoutUrl;
	private String errorUrl;
	
	
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		System.out.println("------");
		System.out.println("CasFilter doFilter:");

		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		String url = request.getRequestURI();
		String ticket = request.getParameter("ticket");
		String service = request.getParameter("service");
		String resource = getResource(request);
		System.out.println("resource:" + resource);
		System.out.println("requestUri:" + request.getRequestURI());
		System.out.println("path:"+request.getContextPath()+" "+request.getServletPath());
		System.out.println("query:" + request.getQueryString());
		System.out.println("ticket:" + ticket);
		System.out.println("service:" + service);
		
		HttpSession session = request.getSession();
		/*Map<String,String> reqs = (Map<String,String>) session.getAttribute("reqs");
		if (reqs==null) {
			reqs = new HashMap<String,String>();
			session.setAttribute("reqs", reqs);
		}*/
		Boolean isAuthorized = (Boolean) session.getAttribute("isAuthorized");
		String login = (String) session.getAttribute("login");

		//System.out.println("reqs:" + reqs);
		System.out.println("isAuthorized:" + isAuthorized);
		System.out.println("LOGIN:" + login);
		System.out.println("------ cookies:");

		//Pinto las cookies;
		System.out.println("SessioID="+session.getId());
		Cookie[] cookies = request.getCookies();
		if (cookies!=null) {
			for(Cookie cookie:cookies){
				System.out.println(getCookieString(cookie));
			}
		}
		System.out.println("------");
		
		//pagina de error
		if (errorUrl.equals(request.getServletPath())){
			chain.doFilter(req, res);
			return;
		}

		//logout
		if (logoutUrl.equals(request.getServletPath())){
			//logout service
			session.setAttribute("isAuthorized", null);
			session.setAttribute("login", null);
			CasFilterManager.remove();
			response.sendRedirect(casServerPrefix+"/logout?service="+request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath());
			return;
		}
		
		
		if (isAuthorized == null || !isAuthorized) {
			// veo el path si tiene ticket
			if (ticket == null) {
				System.out.println("No Authorized, ticket==null -> redirect a: "+casServerPrefix+"/login?service="+resource);
				CasFilterManager.setUser(null);
				response.sendRedirect(casServerPrefix+"/login?service="+resource);
			} else {
				// valido el ticket
				
				System.out.println("No Authorized, ticket -> valido el ticket a: "+casServerPrefix+"/serviceValidate?service="+resource+"&ticket="+ticket);
				URL urlService = new URL(casServerPrefix+"/serviceValidate?service="+resource+"&ticket="+ticket);
				URLConnection uc = urlService.openConnection();
				
				Map<String, List<String>> map = uc.getHeaderFields();
				if (map != null) {
					for (String key : map.keySet()) {
						System.out.print(key + "=[");
						for (String v : map.get(key)) {
							System.out.print(v + " | ");
						}
						System.out.println("]");
					}
				}		

				
				BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
				String inputLine;
				String cad = "";
				while ((inputLine = in.readLine()) != null) cad += inputLine+"\n";
				in.close();

				System.out.println("res="+cad);

				int i1 = cad.indexOf("<cas:authenticationSuccess>");
				if (i1 > 0) {
					int i2 = cad.indexOf("<cas:user>");
					if (i2 > 0) {
						int i3 = cad.indexOf("</cas:user>", i2);
						if (i3 > i2 + 10) {
							login = cad.substring(i2 + 10, i3);
							session.setAttribute("isAuthorized", true);
							session.setAttribute("login", login);
							CasFilterManager.setUser(login);
							System.out.println("SI Authorized, login="+login);
							response.sendRedirect(resource);
							//chain.doFilter(req, res);
							return;
						}
					}
				}

				System.out.println("NO Authorized, redirect a error" );
				
				CasFilterManager.setUser(null);
				session.setAttribute("isAuthorized", null);
				session.setAttribute("login", null);
				response.sendRedirect(request.getContextPath()+errorUrl);
			}
		} else {
			System.out.println("Authorized, no hago nada" );
			CasFilterManager.setUser(login);
			chain.doFilter(req, res);
		}
	}

	private String getCookieString(Cookie cookie) {
		return cookie.getName()+": domain="+cookie.getDomain()
			+" path="+cookie.getPath()
			+" value="+cookie.getValue()
			+" version="+cookie.getVersion()
			+" secure="+cookie.getSecure()
			+" maxAge="+cookie.getMaxAge();
	}

	private String getResource(HttpServletRequest req) {
		int port = req.getServerPort();
		String query = req.getQueryString();
		String res = req.getScheme()+"://"+req.getServerName()+((port!=80)&&(port!=443)?":"+port:"")
				+req.getRequestURI();//+(query!=null?"?"+query:"");
		return res;
	}

	public CasFilter() {
		System.out.println("new CasFilter");
	}
	public void init(FilterConfig config) throws ServletException {
	}
	public void destroy() {
	}
	public String getCasServerPrefix() {
		return casServerPrefix;
	}
	public void setCasServerPrefix(String casServerPrefix) {
		this.casServerPrefix = casServerPrefix;
	}
	public String getLogoutUrl() {
		return logoutUrl;
	}
	public void setLogoutUrl(String logoutUrl) {
		this.logoutUrl = logoutUrl;
	}
	public String getErrorUrl() {
		return errorUrl;
	}
	public void setErrorUrl(String errorUrl) {
		this.errorUrl = errorUrl;
	}
}
