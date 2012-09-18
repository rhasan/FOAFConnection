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

import website.web.model.Person;
import website.web.util.Utility;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
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
	
	private String givenFoafUri = null;
	private Model givenFoafModel = null;
	private List<String> ignore = null;
	
	private HashMap<String, Person> allPerson = null;
	
	public FoafProcessorJena(int connectionDegree) {
		
		this.connectionDegree = connectionDegree;
		initIgnore();
	}
	
	private void initIgnore(){
		ignore = new ArrayList<String>();
		ignore.add("http://www-sop.inria.fr/members/Olivier.Corby/");
		
		allPerson = new HashMap<String, Person>();		
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
	
	public void constructFoafNetwork(Person person) throws UnsupportedEncodingException {
		allPerson.put(person.getUri(), person);
		constructFoafNetwork(person.getUri());
	}
	
	public void constructFoafNetwork(String foafUri) throws UnsupportedEncodingException {
		givenFoafUri = foafUri;
		
		givenFoafModel = ModelFactory.createDefaultModel();
				
		dataset.addNamedModel(foafUri, givenFoafModel);
		givenFoafModel.read(foafUri);
		processFoafConnections(foafUri, givenFoafModel, 0);
		
		//logger
		Utility.logJenaModel(givenFoafModel, logger);
		//create a working copy of the model with removing the blank nodes and unnecessary things such as graph names
		//refineModel();
		mapRDF2Object();
		
		constructCouldBeIntroduced();
	
	}
	
	private Model refineModel(){
		Model refinedModel = null;
		
		String query = "construct {?s ?p ?o} where { GRAPH ?source {?s ?p ?o}." +
				" FILTER( !isBlank(?s) && !isBlank(?o))}";
		logger.debug(query);
		QueryExecution qExec = QueryExecutionFactory.create(query, dataset);
		refinedModel = qExec.execConstruct();
		logger.debug(refinedModel.size());
		
		return refinedModel;
	}
	
	private void mapRDF2Object() {
		String query = "select * where { GRAPH ?source {?s ?p ?o}." +
				" FILTER( !isBlank(?s) && !isBlank(?o))}";
		logger.debug(query);
		QueryExecution qExec = QueryExecutionFactory.create(query, dataset);
		
		
		try {
			ResultSet results = qExec.execSelect();
			//logger.debug(ResultSetFormatter.asText(results));
		    
			for ( ; results.hasNext() ; )
		    {
			  
		      QuerySolution soln = results.nextSolution() ;
		      
		      //RDFNode x = soln.get("varName") ;      
		      Resource graph = soln.getResource("source"); 
		      Resource subject = soln.getResource("s");
		      Resource predicate = soln.getResource("p");
		      
		      Resource object = soln.getResource("p");
		      
		      Person currentPerson = personFromHash(graph.getURI());
		      
		      
		      
		      if(graph.getURI().equals(subject.getURI())){
		    	  //logger.debug("Graph Subject:" +graph.getURI()+" "+subject.getURI());
		    	  //logger.debug("pred:" + predicate.getURI());
		    	  //logger.debug("pred:" + predicate.getURI());
		    	  
			      if(predicate.getURI().equals(FOAF.name.getURI())) {
			      
			    	  
			    	  Literal lObj = soln.getLiteral("o");
			    	  //logger.debug(subject.getURI()+" has name: "+ lObj.getString());
			    	  currentPerson.setName(lObj.getString());
			    	  //logger.debug("From hashmap has name: "+ allPerson.get(graph.getURI()).getName());
			      } else if(predicate.getURI().equals(FOAF.knows.getURI())) {
			    	  Resource friendUri = soln.getResource("o");
			    	  Person friendPerson = personFromHash(friendUri.getURI());
			    	  
			    	  currentPerson.getFriends().add(friendPerson);
			      }
		      }
		      //Literal l = soln.getLiteral("VarL") ;   // Get a result variable - must be a literal
		    }
			
			
		} finally { qExec.close() ; }		
	}
	
	private Person personFromHash(String personUri) {
	      Person currentPerson = allPerson.get(personUri);
	      if(currentPerson == null) {
	    	  currentPerson = new Person();
	    	  currentPerson.setUri(personUri);
	    	  allPerson.put(personUri, currentPerson);
	      }
	      return currentPerson;
		
	}
	
	public void constructCouldBeIntroduced() {
		String query = "select * where { GRAPH "+"<"+givenFoafUri+">"+" {"+"<"+givenFoafUri+">"+" <http://xmlns.com/foaf/0.1/knows> ?f1}. " +
				" GRAPH ?source1 {?f1 <http://xmlns.com/foaf/0.1/knows> ?f2}." +
				" FILTER( !isBlank(?f1) && !isBlank(?f2))}";		
		logger.debug(query);
		QueryExecution qExec = QueryExecutionFactory.create(query, dataset);
		//ResultSet results = qExec.execSelect();
		//logger.debug(ResultSetFormatter.asText(results));
		
		try {
			ResultSet results = qExec.execSelect();
			//logger.debug(ResultSetFormatter.asText(results));
		    
			for ( ; results.hasNext() ; )
		    {
			  
		      QuerySolution soln = results.nextSolution() ;
		      
		      Person currentPerson = personFromHash(givenFoafUri);		      
		      
		      //RDFNode x = soln.get("varName") ;      
		      //Resource friend = soln.getResource("f1"); 
		      Resource couldBeIntro = soln.getResource("f2");
		      
		      Person couldBeIntroPerson = personFromHash(couldBeIntro.getURI());
		      currentPerson.getFriends().add(couldBeIntroPerson);
		    }
			
		} finally { qExec.close() ; }		

	}
	
	public void constructFirstDegreeConnections() {
		String query = "select * where { GRAPH "+"<"+givenFoafUri+">"+" {"+"<"+givenFoafUri+">"+" <http://xmlns.com/foaf/0.1/knows> ?f1}. " +
				"  FILTER( !isBlank(?f1))}";
		
		QueryExecution qExec = QueryExecutionFactory.create(query, dataset);
		ResultSet results = qExec.execSelect();
		logger.debug(ResultSetFormatter.asText(results));
		
	}

	public void constructSecondDegreeConnections() {
		String query = "select * where { GRAPH "+"<"+givenFoafUri+">"+" {"+"<"+givenFoafUri+">"+" <http://xmlns.com/foaf/0.1/knows> ?f1}. " +
				" GRAPH ?source1 {?f1 <http://xmlns.com/foaf/0.1/knows> ?f2}." +
				" FILTER( !isBlank(?f1) && !isBlank(?f2))}";
		//String query = "select * where { GRAPH ?source {?s <http://xmlns.com/foaf/0.1/knows> ?o}}";
		logger.debug(query);
		QueryExecution qExec = QueryExecutionFactory.create(query, dataset);
		ResultSet results = qExec.execSelect();
		logger.debug(ResultSetFormatter.asText(results));
		
	}
}

