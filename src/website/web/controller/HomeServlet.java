package website.web.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.jena.iri.IRIFactory;
import org.apache.log4j.Logger;

import website.web.foaf.FoafProcessorJena;
import website.web.model.Person;
import website.web.util.Utility;

public class HomeServlet extends HttpServlet {

	private RequestDispatcher homeJsp;
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		ServletContext context = config.getServletContext();
		homeJsp = context.getRequestDispatcher("/WEB-INF/jsp/home.jsp");
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		logger.debug("HomeServlet.doGet()");
		homeJsp.forward(req, resp);

	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		processInput(req, resp);
		
		homeJsp.forward(req, resp);
	}
	
	private void computeFoafConnections(HttpServletRequest req,
			HttpServletResponse resp, Person person) throws UnsupportedEncodingException {
		FoafProcessorJena fp = new FoafProcessorJena(6); //get the connection degree from input/config
		fp.constructFoafNetwork(person);
		//fp.constructFirstDegreeConnections();
		//fp.constructSecondDegreeConnections();
	}

	private void processInput(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		logger.debug("HomeServlet.processInput()");
		String iriStr = req.getParameter("uri");
		logger.debug("Person URI:"+iriStr);
		
		if(Utility.validIRI(iriStr)==false) {
			req.setAttribute("errorMessage", "Please insert a valid URL");
			
		}
		else {
			req.setAttribute("errorMessage", "");
		}
		
		//StringEscapeUtils.escapeHtml4(uri)
		Person person = new Person();
		person.setUri(iriStr);
		
		
		req.setAttribute("person", person);
		req.setAttribute("friendsList", person.getFriends());
		/*List<String> list = new ArrayList<String>();
		list.add("test1");
		list.add("test2");
		req.setAttribute("friendsList", list);*/
		
		computeFoafConnections(req, resp, person);
	}
}
