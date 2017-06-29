package se.kth.csc.kmr.conserve.iface;

import se.kth.csc.kmr.conserve.Request;

import com.google.inject.servlet.RequestScoped;

@RequestScoped
public class RequestHandle {

	private Request request;

	public Request getRequest() {
		return request;
	}

	public void setRequest(Request request) {
		this.request = request;
	}

}
