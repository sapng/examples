package com.neo4j.jena.graph;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
//import org.neo4j.graphdb.index.UniqueFactory;

import org.apache.jena.graph.Graph;


/**
 * Jena graph wrapper for Neo4J graph database service.
 * 
 * @author Khalid Latif, Mahek Hanfi (2014-02-14)
 */

public class UniqueNodeFactory  {

	private final GraphDatabaseService graphdb;

	/** Holds nodes in memory during bulk loading */
	private Map<org.apache.jena.graph.Node, Node> bulkNodes;
	
	/** Reference to Jena Graph */
	private final Graph graph;

	/**
	 * Initialize the factory.
	 * 
	 * @param graphdb A GraphDatabaseService instance to store newly create node.
	 * @param graph A Jena graph to process new node creation.
	 */
	public UniqueNodeFactory(GraphDatabaseService graphdb, Graph graph) {
		//super(graphdb, "Resources");
		this.graphdb = graphdb;
		this.graph = graph;
	}

	/**
	 * Initializes the node.
	 */
	//@Override
	protected void initialize(Node created, Map<String, Object> properties) {
		created.addLabel(Label.label(NeoGraph.LABEL_URI));
		created.setProperty(NeoGraph.PROPERTY_URI, properties.get(NeoGraph.PROPERTY_URI));
	}
	
	/**
	 * Get an existing node or return null.
	 */
	public Node get(org.apache.jena.graph.Node node) {
		Node neoNode = null;
		
		//StopWatch watch = new StopWatch();
		
		if(bulkNodes!=null) {
			neoNode = bulkNodes.get(node);
			if(neoNode!=null) return neoNode;
		}
		if(node.isLiteral()) {
			Label label = Label.label(NeoGraph.LABEL_LITERAL);
			try {
				ResourceIterator<org.neo4j.graphdb.Node> nodes = graphdb.beginTx().findNodes( label, NeoGraph.PROPERTY_VALUE, node.getLiteralValue());

	            if ( nodes.hasNext() ) {
	            	neoNode = nodes.next();
	            }
	        }catch (Exception ex) {

			}

		} else if(node.isBlank()) {
			Label label = Label.label(NeoGraph.LABEL_BNODE);
			//FIXME Blank node id might not be unique across multiple loads of same RDF data
			try {
				ResourceIterator<org.neo4j.graphdb.Node> nodes = graphdb.beginTx().findNodes( label, NeoGraph.PROPERTY_VALUE, node.getBlankNodeId().toString());

	            if ( nodes.hasNext() ) {
	            	neoNode = nodes.next();
	            }
	        } catch (Exception ex) {

			}
		} else {
			Label label = Label.label(NeoGraph.LABEL_URI);
			String prefixed = graph.getPrefixMapping().shortForm(node.getURI());
			try {
				ResourceIterator<org.neo4j.graphdb.Node> nodes = graphdb.beginTx().findNodes( label, NeoGraph.PROPERTY_URI, prefixed);
            	if ( nodes.hasNext() ) {
            		neoNode = nodes.next();
	        	}
			} catch (Exception ex) {

			}
		}
		
		//System.out.println("Get " + node + " took: " + watch.stop());
		if(neoNode!=null && bulkNodes!=null)
			bulkNodes.put(node, neoNode);
		return neoNode;
	}
	
	/**
	 * Get or create a Neo4J node for a given Jena node object.
	 * 
	 * @return Neo4J node
	 */
	public Node getOrCreate(org.apache.jena.graph.Node node) {
		Node created;
		
		if(bulkNodes!=null) {
			Node neoNode = bulkNodes.get(node);
			if(neoNode!=null){
				//System.out.println(node + " already exist");
				return neoNode;
			}
		}
		else
			System.out.println("Bulk node is null");
		
		//StopWatch watch = new StopWatch();
		
		// Literals get special treatment (duplicates allowed)
		if (node.isLiteral()) {
			Label label = Label.label(NeoGraph.LABEL_LITERAL);
			created = graphdb.beginTx().createNode(label);
			created.setProperty(NeoGraph.PROPERTY_VALUE, node.getLiteralValue().toString());
			// Add data-type tag
			if(node.getLiteralDatatype()!=null)
				created.setProperty(NeoGraph.PROPERTY_DATATYPE, node.getLiteralDatatype().toString());
			// Add language tag
			if(node.getLiteralLanguage()!=null)
				created.setProperty(NeoGraph.PROPERTY_LANGUAGE, node.getLiteralLanguage());
			return created;
		} else if(node.isBlank()) {
				Label label = Label.label(NeoGraph.LABEL_BNODE);
				created = graphdb.beginTx().createNode(label);
				created.setProperty(NeoGraph.PROPERTY_URI, node.getBlankNodeId().toString());
			
		} else {
			String prefixed = graph.getPrefixMapping().shortForm(node.getURI());
			// Back to normal procedure for resources
			created = getOrCreate(NeoGraph.PROPERTY_URI, prefixed);
		}
		
		//System.out.println("Get or create " + node + " took:" + watch.stop());
		
		if(bulkNodes!=null)
			bulkNodes.put(node,  created);
		return created;
	}

	private Node getOrCreate(String propertyUri, String prefixed) {
		// TODO
		return null;
	}

	public void startBulkLoad() {
		bulkNodes = new HashMap<org.apache.jena.graph.Node, Node>();
	}
	
	public void stopBulkLoad() {
		bulkNodes = null;
	}
}