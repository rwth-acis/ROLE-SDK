package se.kth.csc.kmr.conserve.util;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

public class WebAppServlet extends HttpServlet {

	private static final long serialVersionUID = -312135393959165966L;

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		RequestDispatcher requestDispatcher = getServletContext()
				.getNamedDispatcher("default");
		HttpServletRequest requestWrapper = new HttpServletRequestWrapper(
				request) {
			public String getServletPath() {
				return "";
			}
		};
		requestDispatcher.forward(requestWrapper, response);
	}

}
