package eu.role_project.service.dui;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shindig.common.servlet.InjectedServlet;


public class DuiHttpServlet extends InjectedServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1728714752377922055L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		System.err.println("KELI servlet GET ----------------------------------");
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		System.err.println("KELI servlet POST ----------------------------------");;
	}

}
