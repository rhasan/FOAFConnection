package website.web.foaf;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openjena.atlas.logging.Log;

import website.web.util.Utility;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.impl.StatementImpl;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.RDFS;


public class FoafProcessorJena {
	
	private Dataset dataset = DatasetFactory.createMem();
	private Logger logger = Logger.getLogger(this.getClass());
	private int connectionDegree = 6;
	
	private String foafConNS = "http://ns.inria.fr/ratio4ta/others/foafc.owl#";
	
	private String givenFoaUri = null;
	private Model givenFoafModel = null;
	private List<String> ignore = null;
	
	public FoafProcessorJena(int connectionDegree) {
		
		this.connectionDegree = connectionDegree;
		initIgnore();
	}
	
	private void initIgnore(){
		ignore = new ArrayList<String>();
		ignore.add("http://www-sop.inria.fr/members/Olivier.Corby/");
	}
	
	public void processFoafConnections(String foafUri, Model model, int depth) {
		
		if(depth == connectionDegree) return;
		//System.out.println("Start Processing "+(depth+1)+" degree connections");
		NodeIterator givenFoafFriends = model.listObjectsOfProperty( FOAF.knows);
		
		while(givenFoafFriends.hasNext()) {
			try {
				Resource friend = (Resource) givenFoafFriends.next();
				//System.out.println(friend.isAnon());
				String friendURI = friend.getURI();
				if(friend.isAnon()) {
					StmtIterator stmt = friend.listProperties(RDFS.seeAlso);
					
					if(stmt.hasNext()) {
						Resource fof = (Resource)stmt.next().getObject();
						if(fof.isAnon()==false) {
							friendURI = fof.getURI();
							

						}
						else {
							continue;
						}
					}
					else {
						continue;
					}
				}
				//special cases
				if(ignore.contains(friendURI)) {
					continue;
				}
				
				if(dataset.containsNamedModel(friendURI)==false) {
					

					
					//remove triple insertions after implementing sparql 
					//construct triple insertions in the constructFoafNetwor method 
					/*Resource currentPerson = givenFoafModel.createResource(givenFoaUri);
					Resource friendPerson = givenFoafModel.createResource(friendURI);
					Property firendDegree = null;
					if(depth==0) {
						firendDegree = model.createProperty(foafConNS,"firstDegreeAway");
					} else if(depth==1) {
						firendDegree = model.createProperty(foafConNS,"secondDegreeAway");
					} else {
						firendDegree = model.createProperty(foafConNS,"thirdDegreeAway");
					}
					currentPerson.addProperty(firendDegree, friendPerson);*/
					
					//System.out.println((depth+1)+" degree friend :"+friendURI);
					logger.debug((depth+1)+" degree friend :"+friendURI);
					Model friendModel = ModelFactory.createDefaultModel();

					Resource currentPerson = model.createResource(foafUri);
					Resource friendPerson = model.createResource(friendURI);
					//Property foafKnows = model.createProperty(FOAF.knows.getURI());
					currentPerson.addProperty(FOAF.knows, friendPerson);					
					
					
					dataset.addNamedModel(friendURI, friendModel);
					//friendModel.read(friendURI,"RDF/XML");
					friendModel.read(friendURI,"RDF/XML");

					
					//Literal l = getFoafProperty(friendModel, FOAF.name);
					
					//System.out.println(l.getString());
					//friendModel.write(System.out,"N-TRIPLE");
					//Utility.logJenaModel(model, logger);
				
					//logger.debug("Name: "+l.getString());
					processFoafConnections(friendURI, friendModel,depth+1);

				}
			} catch (Exception e) {
				//e.printStackTrace();
			}
			
		}
		
	}
	
	public Literal getFoafProperty(Model m, Property p){
		NodeIterator i = m.listObjectsOfProperty(p);
		if(i.hasNext()) {
			return i.next().asLiteral();
		}
		return null;
		
	}
	
	public void constructFoafNetwork(String foafUri) throws UnsupportedEncodingException {
		givenFoaUri = foafUri;
		
		givenFoafModel = ModelFactory.createDefaultModel();
				
		dataset.addNamedModel(foafUri, givenFoafModel);
		givenFoafModel.read(foafUri);
		processFoafConnections(foafUri, givenFoafModel, 0);
		
		//logger
		//Utility.logJenaModel(givenFoafModel, logger);
		//create a working copy of the model with removing the blank nodes and unnecessary things such as graph names
		
	
	}
	
	public void constructFirstDegreeConnections() {
		String query = "select * where { GRAPH "+"<"+givenFoaUri+">"+" {"+"<"+givenFoaUri+">"+" <http://xmlns.com/foaf/0.1/knows> ?f1}. " +
				"  FILTER( !isBlank(?f1))}";
		
		QueryExecution qExec = QueryExecutionFactory.create(query, dataset);
		ResultSet results = qExec.execSelect();
		logger.debug(ResultSetFormatter.asText(results));
		
	}

	public void constructSecondDegreeConnections() {
		String query = "select * where { GRAPH "+"<"+givenFoaUri+">"+" {"+"<"+givenFoaUri+">"+" <http://xmlns.com/foaf/0.1/knows> ?f1}. " +
				" GRAPH ?source1 {?f1 <http://xmlns.com/foaf/0.1/knows> ?f2}." +
				" FILTER( !isBlank(?f1) && !isBlank(?f2))}";
		//String query = "select * where { GRAPH ?source {?s <http://xmlns.com/foaf/0.1/knows> ?o}}";
		logger.debug(query);
		QueryExecution qExec = QueryExecutionFactory.create(query, dataset);
		ResultSet results = qExec.execSelect();
		logger.debug(ResultSetFormatter.asText(results));
		
	}
}

