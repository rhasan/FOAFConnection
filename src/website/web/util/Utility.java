package website.web.util;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import org.apache.jena.iri.IRI;
import org.apache.jena.iri.IRIFactory;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;

public class Utility {
	private static Logger logger = Logger.getLogger("website.web.util.Utility");
	public static boolean validIRI(String iriStr) {
		boolean includeWarnings = false;
		IRI iri;
		iri = IRIFactory.semanticWebImplementation().create(iriStr); // always works
		if (iri.hasViolation(includeWarnings)) {
			logger.debug("In valid IRI:"+includeWarnings);
			return false;
		}
		return true;
		
	}
	
	public static void logJenaModelN3(Model model, Logger log) throws UnsupportedEncodingException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		model.write(out, "N3");
		byte[] charData = out.toByteArray();
		String str = new String(charData, "ISO-8859-1");
		log.debug(str);		
	}
	
	
	
}
