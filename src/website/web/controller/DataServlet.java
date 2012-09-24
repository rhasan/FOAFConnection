package website.web.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;

import website.web.foaf.FoafProcessorJena;

import com.hp.hpl.jena.rdf.model.Model;

public class DataServlet extends HttpServlet {
	private Logger logger = Logger.getLogger(this.getClass());
	private FoafProcessorJena fp = null;
	private RequestDispatcher dataJsp;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		//fp = fp.getInstance();
		
		ServletContext context = config.getServletContext();
		fp = (FoafProcessorJena) context.getAttribute("FoafProcessor");
		
		dataJsp = context.getRequestDispatcher("/WEB-INF/jsp/data.jsp");
		logger.debug(context.getRealPath(context.getContextPath()));

	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		String headerAccept = req.getHeader("Accept"); 
		
		logger.debug(req.getRequestURL().toString());
		logger.debug(headerAccept);
		Model m = fp.getExpNamedGraphStatements(req.getRequestURL().toString());
		if(headerAccept.equals("application/rdf+xml")==true) {
			logger.debug("rdf+xml accept");
			OutputStream os = resp.getOutputStream();
			resp.setContentType("application/rdf+xml");
			m.write(os);
		} else if(headerAccept.equals("text/n3")==true) {
			logger.debug("n3 accept");
			OutputStream os = resp.getOutputStream();
			resp.setContentType("text/n3");
			m.write(os,"N3");
		} else {
			
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			m.write(out,"N-TRIPLE");
			byte[] charData = out.toByteArray();
			String str = new String(charData, "ISO-8859-1");			
			String outStr = StringEscapeUtils.escapeHtml4(str);
			String outFinal = outStr.replace("\n","<br/>");
			req.setAttribute("expString", outFinal);
			dataJsp.forward(req, resp);
		}
	}

}
