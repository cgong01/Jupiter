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
 * Servlet implementation class Signup
 */
@WebServlet("/signup")
public class Signup extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Signup() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		DBConnection conn = DBConnectionFactory.getDBConnection();
		try {
			String userId = request.getParameter("email");
			String pwd = request.getParameter("pwd");
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.update(pwd.getBytes());
			byte[] digest = md.digest();
//			String myHash = DatatypeConverter.printHexBinary(digest);
			String myHash = (new BASE64Encoder()).encode(digest);
			System.out.println(myHash);
			String firstName = request.getParameter("firstname");
			String lastName = request.getParameter("lastname");
			
			if (conn.verifySignup(userId)) {
				conn.signup(userId, myHash, firstName, lastName);
				response.sendRedirect("success.html");
			}
			else {
				response.sendRedirect("error.html");
			}
			
			
//			JSONObject obj = new JSONObject();
//			if (conn.verifySignup(userId)) {
//				HttpSession session = request.getSession();
//				session.setAttribute("user_id", userId);
//				session.setAttribute("password", pwd);
//				session.setAttribute("first_name", firstName);
//				session.setAttribute("last_name", lastName);
//				// setting session to expire in 10 minutes.
//				session.setMaxInactiveInterval(10 * 60);
//				
//				obj.put("status", "OK");
//				obj.put("user_id", userId);
//				obj.put("password", pwd);
//				obj.put("first_name", firstName);
//				obj.put("last_name", lastName);
//			}
//			else {
//				response.setStatus(401);
//			}
//			RpcHelper.writeJsonObject(response, obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
