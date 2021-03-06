/*
 * Copyright 2014 NUST. All rights reserved.
 * Use is subject to license terms.
 */
package com.neo4j.jena.graph;

import org.apache.http.MethodNotSupportedException;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

import org.apache.jena.graph.Capabilities;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphEventManager;
import org.apache.jena.graph.GraphStatisticsHandler;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.TransactionHandler;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.SimpleEventManager;
import org.apache.jena.shared.AddDeniedException;
import org.apache.jena.shared.DeleteDeniedException;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.DC_11;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;

import java.io.File;

/**
 * Jena graph wrapper for Neo4J graph database service.
 * 
 * @author Khalid Latif, Mahek Hanfi (2014-02-14)
 */
public class NeoGraph implements Graph {

	public static String DEFAULT_DATABASE_NAME = "default";
	public static String PATH_TO_CONFIG = "";

	public static final String PROPERTY_DATATYPE = "type";
	public static final String PROPERTY_KIND = "kind";
	public static final String PROPERTY_LANGUAGE = "lang";
	public static final String PROPERTY_NG = "ng";
	public static final String PROPERTY_URI = "uri";
	public static final String PROPERTY_VALUE = "value";
	
	public static final String LABEL_BNODE = "bnode";
	public static final String LABEL_LITERAL = "literal";
	public static final String LABEL_URI = "uri";

	/** Neo4J databaseMgmt  */
	DatabaseManagementService managementService;

	/** Neo4J graph database */
	GraphDatabaseService graphdb;
	
	/** Graph event manager */
	private GraphEventManager eventManager;
	
	/** Factory of unique nodes */
	UniqueNodeFactory nodeFactory;
	
	/** Factory for unique relationships */
	UniqueRelationshipFactory relationshipFactory;
	
	/** Prefix mappings */
	PrefixMapping mapping;
	
	/**
	 * Loads Neo4J graph from given directory.
	 *
	 */
	public NeoGraph(final String directory) {
		
		//this(new GraphDatabaseFactory().newEmbeddedDatabase(directory));

		managementService = new DatabaseManagementServiceBuilder(new File(directory)).build();
        graphdb = managementService.database(DEFAULT_DATABASE_NAME);
        registerShutdownHook( managementService );

	//	managementService = new DatabaseManagementServiceBuilder( new File(directory) )
	//			.loadPropertiesFromFile( PATH_TO_CONFIG + "neo4j.conf" ).build();
	//	graphdb = managementService.database( DEFAULT_DATABASE_NAME );


		// conf without conf file
		//managementService = new DatabaseManagementServiceBuilder( new File(directory) )
		//		.setConfig( GraphDatabaseSettings.pagecache_memory, "512M" )
		//		.setConfig( GraphDatabaseSettings.transaction_timeout, Duration.ofSeconds( 60 ) )
		//		.setConfig( GraphDatabaseSettings.preallocate_logical_logs, true ).build();
		//graphdb = managementService.database( DEFAULT_DATABASE_NAME );

	}


	private static void registerShutdownHook( final DatabaseManagementService managementService ) {
		// Registers a shutdown hook for the Neo4j instance so that it
		// shuts down nicely when the VM exits (even if you "Ctrl-C" the
		// running application).
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				managementService.shutdown();
			}
		});
	}

		/**
         * Initializes this graph.
         */
	public NeoGraph(final GraphDatabaseService graphdb) {
		this.graphdb = graphdb;
		try(Transaction tx = graphdb.beginTx()) {
			nodeFactory = new UniqueNodeFactory(graphdb, this);
			relationshipFactory = new UniqueRelationshipFactory(graphdb, this);
		}
	}
	
	/**
	 * Add triple in the graph. 
	 * First convert Jena triple to Neo4j node and relationship and then add it in the neo4j graph
	 */
	@Override
	public void add(Triple triple) throws AddDeniedException {
		try (Transaction tx = graphdb.beginTx()) {
			// Get or create a node for subject
			org.neo4j.graphdb.Node subject = nodeFactory.getOrCreate(triple.getSubject());
			// Get or create a node for object
			org.neo4j.graphdb.Node object = nodeFactory.getOrCreate(triple.getObject());
			// Don't add triple if relationship already exists
			relationshipFactory.getOrCreate(subject, triple.getPredicate().getURI(), object);
			tx.commit();
		}
	}
	
	@Override
	public void clear() {
		throw new RuntimeException(new MethodNotSupportedException("clear"));
	}

	/**
	 * Shutdown the graph.
	 */
	@Override
	public void close() {
		registerShutdownHook( managementService );
		//graphdb.shutdown();
	}
	
	/**
	 * Checks if the given triple exist in the graph.
	 */
	@Override
	public boolean contains(Triple triple) {
		return contains(triple.getSubject(), triple.getPredicate(), triple.getObject());
	}
	
	/**
	 * Checks if the given triple exist in the graph.
	 */
	@Override
	public boolean contains(Node subject, Node predicate, Node object) {
		try(Transaction tx = graphdb.beginTx()) {
			// Check subject
			org.neo4j.graphdb.Node sub= nodeFactory.get(subject);
			if(sub!=null) {
				// Check object
				org.neo4j.graphdb.Node obj = nodeFactory.get(object);
				if(obj!=null) {
					// Check relationship
					Relationship relation = relationshipFactory.get(sub, predicate.getURI(), obj);
					if(relation!=null) {
						tx.commit();
						return true;
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * Delete the given triple from the graph.
	 */
	@Override
	public void delete(Triple triple) throws DeleteDeniedException {
		Transaction tx=graphdb.beginTx();
		org.neo4j.graphdb.Node subject = nodeFactory.get(triple.getSubject());
		System.out.println("Subject node:" + subject.getProperty("uri"));
		if(subject!=null) {
			org.neo4j.graphdb.Node object = nodeFactory.get(triple.getObject());
			System.out.println("Object node:" + object.getProperty("uri"));
			if(object!=null) {
				Relationship relation = relationshipFactory.get(subject, triple.getPredicate().getURI(), object);
				System.out.println("Relationship:" +relation.getProperty("uri"));
				if(!subject.hasRelationship())
					subject.delete();
				if(triple.getObject().isLiteral())
					object.delete();
				else if(!object.hasRelationship())
					object.delete();
			}
			tx.commit();
		}	
	}

	@Override
	public boolean dependsOn(Graph arg0) {
		throw new RuntimeException(new MethodNotSupportedException("dependsOn"));
	}
	
	/**
	 * Find the given triple(s) from the graph. 
	 */
	@Override
	public ExtendedIterator<Triple> find(Triple match) {
		return find(match.getMatchSubject(),match.getMatchPredicate(), match.getMatchObject());
	}

	/**
	 * Find the given triple(s) from the graph.
	 * @return
	 */
	@Override
	public ExecutionResultIterator find(Node subject, Node predicate, Node object) {
		//System.out.println("NeoGraph#find");
		try(Transaction tx = graphdb.beginTx()) {
			StringBuffer query = new StringBuffer("MATCH triple=");
			
			query.append("(subject");
			if(subject.equals(Node.ANY)) {
				query.append(":"+ LABEL_URI);
				//System.out.println("NeoGraph#find#Any:"+subject);
			} else if(subject.isURI()){
				query.append(":"+LABEL_URI+" {uri:'");
				query.append(subject.getURI());
				query.append("'}");
				//System.out.println("NeoGraph#find#URI:"+subject+subject.getURI());
			} else {
				query.append(":" + LABEL_BNODE);
				//System.out.println("NeoGraph#find#"+subject);
			}
			query.append(")-[predicate]->(object");
			
			//query.append("]->(object");
			if(object.equals(Node.ANY)) {
				//query.append(" ");
			} else if(object.isURI()){
				query.append(":"+LABEL_URI+" {uri:'");
				query.append(object.getURI());
				query.append("'}");
			} else {
				query.append(":"+LABEL_LITERAL+" {value:'");
				query.append(object.getLiteralValue());
				query.append("'}");
			}
			query.append(")");
			
			//System.out.println("Predicate in query: " +getPrefixMapping().shortForm(predicate.getURI()));
			if(predicate.isURI()) {
				query.append("WHERE type(predicate)='");
				query.append(getPrefixMapping().shortForm(predicate.getURI()));
				query.append("'");
			}
			
			query.append("\nRETURN subject, type(predicate), object");
			//System.out.println(query.toString());

			Result result = tx.execute(query.toString());

			//ExecutionEngine engine = new ExecutionEngine(graphdb);
			//ExecutionResult results = engine.execute(query.toString());
			//System.out.println(results.dumpToString());
			//System.out.println("NeoGraph#find#"+predicate);
			//System.out.println("NeoGraph#find#"+object);
			//System.out.println("NeoGraph#find#DONE");
			return new ExecutionResultIterator(result, graphdb);
		}
	}
	
	@Override
	public Capabilities getCapabilities() {
		return NeoCapabilities.DEFAULT;
	}
	
	@Override
	public GraphEventManager getEventManager() {
		if(eventManager==null)
			eventManager = new SimpleEventManager(this);
		return eventManager;
	}
	
	@Override
	public PrefixMapping getPrefixMapping() {
		if(mapping==null)  {
			PrefixMapping standard = PrefixMapping.Factory.create()
				.setNsPrefix( "rdfs", RDFS.getURI() )
				.setNsPrefix( "rdf", RDF.getURI() )
				.setNsPrefix( "dc", DC_11.getURI() )
				.setNsPrefix( "owl", OWL.getURI() )
				.setNsPrefix( "xsd", XSD.getURI() );
			mapping = new NeoPrefixMapping(null, standard);
		}
		return mapping;
	}
	
	@Override
	public GraphStatisticsHandler getStatisticsHandler() {
		throw new RuntimeException(new MethodNotSupportedException("getStatsHandler"));
	}

	@Override
	public TransactionHandler getTransactionHandler() {
		throw new RuntimeException(new MethodNotSupportedException("getTransactionHandler"));
	}
	
	/**
	 * Checks if the graph is closed or not. 
	 */
	@Override
	public boolean isClosed() {
		if(graphdb.isAvailable(0))
			return true;
		else
			return false;
	}
	
	/**
	 * Checks if the grpah is empty or not
	 */
	@Override
	public boolean isEmpty() {
		Transaction tx = graphdb.beginTx();
		Iterable<org.neo4j.graphdb.Node> nodes = tx.getAllNodes();

//		Iterable<org.neo4j.graphdb.Node> nodes= GlobalGraphOperations.at(graphdb).getAllNodes();
		boolean empty = nodes.iterator().hasNext();
		tx.commit();
		return !empty;
	}

	@Override
	public boolean isIsomorphicWith(Graph arg0) {
		throw new RuntimeException(new MethodNotSupportedException("isIsomorphic"));
	}

	@Override
	public void remove(Node arg0, Node arg1, Node arg2) {
		throw new RuntimeException(new MethodNotSupportedException("remove"));
	}

	@Override
	public int size() {
		throw new RuntimeException(new MethodNotSupportedException("size"));
	}

	public void startBulkLoad() {
		nodeFactory.startBulkLoad();
	}
	
	public void stopBulkLoad() {
		nodeFactory.stopBulkLoad();
	}
}