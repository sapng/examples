/*
 * Copyright 2014 NUST. All rights reserved.
 * Use is subject to license terms.
 */
package com.neo4j.jena.bench;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.schema.Schema;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;
import com.neo4j.jena.graph.NeoGraph;

/**
 * Test Class for LUBM dataset.
 * 
 * @author Khalid Latif, Mahek Hanfi (2014-03-10)
 */

public class LUBM {
	private static final String NEO_STORE = "YOUR_NEOSTORE_PATH";
	
	private static final String inputFileName = "University.rdf" ;
	
	 static Logger log= Logger.getLogger(LUBM.class);

	public static void main(String[] args) {
		DatabaseManagementService managementService = new DatabaseManagementServiceBuilder(new File(".")).build();
		GraphDatabaseService graphdb = managementService.database("default");
		//GraphDatabaseService njgraph = new GraphDatabaseFactory().newEmbeddedDatabase(NEO_STORE);
		log.info("Connection created");
		LUBM.write(graphdb);
		LUBM.search(graphdb);
//		graphdb.shutdown();
		log.info("Connection closed");
	}
	
	public static void search(GraphDatabaseService njgraph) {
		NeoGraph graph = new NeoGraph(njgraph);
		Model njmodel = ModelFactory.createModelForGraph(graph);
		
		//long start = System.currentTimeMillis();
		String s2 = "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
                	"PREFIX ub: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#>" +
                "SELECT ?X ?name "+
                "WHERE" +
                "{ ?X ub:name ?name ." +
                 "FILTER regex(?name,\"^Publication\") ."+
                "}"; 
       	          
        Query query = QueryFactory.create(s2);
        QueryExecution qExe = QueryExecutionFactory.create(query, njmodel);
        StopWatch watch = new StopWatch();
        ResultSet results = qExe.execSelect();
        log.info("Query took (ms): "+ watch.stop());
        System.out.println("Query took (ms): "+ watch.stop());
        //ResultSetFormatter.out(System.out, results);
        
        int count=0;
        while(results.hasNext()){
        	//System.out.println("in while"+count);
        	QuerySolution sol = results.next();
        	System.out.println(sol.get("name"));
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
		Logger log= Logger.getLogger(LUBM.class);
		InputStream in = FileManager.get().open( inputFileName );
		if (in == null) {
            throw new IllegalArgumentException( "File: " + inputFileName + " not found");
        }
        
		Model model = ModelFactory.createDefaultModel();
        model.read(in,"","RDF");
        double triples = model.size();
        log.info("Model loaded with " +  triples + " triples");
        System.out.println("Model loaded with " +  triples + " triples");
        
		NeoGraph graph = new NeoGraph(njgraph);
		graph.startBulkLoad();
		log.info("Connection created");
		Model njmodel = ModelFactory.createModelForGraph(graph);
		log.info("NeoGraph Model initiated");
		System.out.println("NeoGraph Model initiated");
		StopWatch watch = new StopWatch();
		//log.info(njmodel.add(model));
		njmodel.add(model);
		log.info("Storing completed (ms): " + watch.stop());
		graph.stopBulkLoad();
		System.out.println("Storing completed (ms): " + watch.stop());
	}
}
