package rpc;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.bind.DatatypeConverter;

import org.json.JSONException;
import org.json.JSONObject;

import db.DBConnection;
import db.DBConnectionFactory;
import sun.misc.BASE64Encoder;

import java.security.MessageDigest;

/**
 * Servlet implementation class Login
 */
@WebServlet("/login")
public class Login extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Login() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
    // check the login status.
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		DBConnection conn = DBConnectionFactory.getDBConnection();
		try {
			JSONObject obj = new JSONObject();
			HttpSession session = request.getSession(false);
			if (session == null) {
				response.setStatus(403);
				obj.put("status", "Session Invalid");
			} else {
				String userId = (String) session.getAttribute("user_id");
				String name = conn.getFullname(userId);
				obj.put("status", "OK");
				obj.put("user_id", userId);
				obj.put("name", name);
			}
			RpcHelper.writeJsonObject(response, obj);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	// finish the login operation.
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		DBConnection conn = DBConnectionFactory.getDBConnection();
		try {
			JSONObject input = RpcHelper.readJsonObject(request);
			String userId = input.getString("user_id");
			String pwd = input.getString("password");
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.update(pwd.getBytes());
			byte[] digest = md.digest();
//			String myHash = DatatypeConverter.printHexBinary(digest);
			String myHash = (new BASE64Encoder()).encode(digest);
			System.out.println(myHash);
			JSONObject obj = new JSONObject();
			if (conn.verifyLogin(userId, myHash)) {
				HttpSession session = request.getSession();
				session.setAttribute("user_id", userId);
				// setting session to expire in 10 minutes.
				session.setMaxInactiveInterval(10 * 60);
				// Get user name
				String name = conn.getFullname(userId);
				obj.put("status", "OK");
				obj.put("user_id", userId);
				
				obj.put("name", name);
			}
			else {
				response.setStatus(401);
			}
			RpcHelper.writeJsonObject(response, obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}








