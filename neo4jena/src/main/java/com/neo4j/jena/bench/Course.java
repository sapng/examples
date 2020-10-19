/*
 * Copyright 2014 NUST. All rights reserved.
 * Use is subject to license terms.
 */
package com.neo4j.jena.bench;

import com.neo4j.jena.graph.NeoGraph;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.FileManager;
import org.apache.log4j.Logger;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.schema.Schema;

import java.io.File;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Test Class for Course dataset.
 * 
 * @author Khalid Latif, Mahek Hanfi (2014-03-10)
 */

public class Course {
//	private static final String NEO_STORE = "neo4jena";
	private static final String NEO_STORE = "/home/alvaro.guerrero/sapng/data/databases/test.db";

	private static final String inputFileName = "course.ttl" ;
	
	 static Logger log= Logger.getLogger(LUBM.class);

	public static void main(String[] args) {
//		GraphDatabaseService njgraph = new GraphDatabaseFactory().newEmbeddedDatabase(NEO_STORE);

//		GraphDatabaseService njgraph = new GraphDatabaseFactory().newEmbeddedDatabase(NEO_STORE);
//		GraphDatabaseService njgraph = new EmbeddedGraphDatabase( "helloworld" );

		DatabaseManagementService managementService = new DatabaseManagementServiceBuilder(new File(".")).build();
		GraphDatabaseService graphdb = managementService.database("default");

		log.info("Connection created");
		Course.write(graphdb);
		Course.search(graphdb);
		log.info("Connection closed");
	}
	
	public static void search(GraphDatabaseService njgraph) {
		NeoGraph graph = new NeoGraph(njgraph);
		Model njmodel = ModelFactory.createModelForGraph(graph);
		
		String s2 = "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
                	"PREFIX uni: <http://seecs.edu.pk/db885#>" +
                	"PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
                "SELECT ?X ?Z ?Y "+
                "WHERE" +
                "{ ?X ?Z ?Y ." +
                "}"; 
       	          
        Query query = QueryFactory.create(s2); 
        QueryExecution qExe = QueryExecutionFactory.create(query, njmodel);
        StopWatch watch = new StopWatch();
        ResultSet results = qExe.execSelect();
        log.info("Query took (ms): "+ watch.stop());
        System.out.println("Query took (ms): "+ watch.stop());
       // ResultSetFormatter.out(System.out, results);
        
        int count=0;
        while(results.hasNext()){
        	//System.out.println("in while"+count);
        	QuerySolution sol = results.next();
        	System.out.println(sol.get("?Z"));
        	count++;
        }
       
       log.info("Record fetched:"+ count);
       System.out.println("Record fetched:"+ count);
	}
	
	public static void ensureIndex(GraphDatabaseService njgraph) {
		IndexDefinition indexDefinition;
        try ( Transaction tx = njgraph.beginTx() ) {
            Schema schema = tx.schema();
            indexDefinition = schema.indexFor( Label.label( NeoGraph.LABEL_URI ) )
                    .on( NeoGraph.PROPERTY_URI )
                    .create();
            tx.commit();
            System.out.println( "Index definition" );
        }
        try ( Transaction tx = njgraph.beginTx() ) {
            Schema schema = tx.schema();
            schema.awaitIndexOnline( indexDefinition, 10, TimeUnit.SECONDS );
            System.out.println( "Index loading" );
        }
	}
	
	public static void write(GraphDatabaseService njgraph) {
		InputStream in = FileManager.get().open( inputFileName );
		if (in == null) {
            throw new IllegalArgumentException( "File: " + inputFileName + " not found");
        }
        
		Model model = ModelFactory.createDefaultModel();
        model.read(in,"","TTL");
        double triples = model.size();
        log.info("Model loaded with " +  triples + " triples");
        System.out.println("Model loaded with " +  triples + " triples");
        Map<String, String> prefixMap = model.getNsPrefixMap();
       // System.out.println("Prefix Mapping: " + prefixMap);
        
		NeoGraph graph = new NeoGraph(njgraph);
		graph.getPrefixMapping().setNsPrefixes(prefixMap);
		graph.startBulkLoad();
		log.info("Connection created");
		Model njmodel = ModelFactory.createModelForGraph(graph);
		log.info("NeoGraph Model initiated");
		System.out.println("NeoGraph Model initiated");
		
		//log.info(njmodel.add(model));
		//njmodel.add(model);
		StmtIterator iterator = model.listStatements();
		StopWatch watch = new StopWatch();
		int count = 0;
		while(iterator.hasNext()){
			njmodel.add(iterator.next());
			count++;
		}
		System.out.println("Total triples loaded are:"+ count);
		graph.stopBulkLoad();
		//log.info("Storing completed (ms): " + watch.stop());
		System.out.println("Storing completed (ms): " + watch.stop());
	}
}
