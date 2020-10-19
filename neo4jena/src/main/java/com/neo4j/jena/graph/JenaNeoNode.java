package com.neo4j.jena.graph;

import org.apache.jena.graph.BlankNodeId;
import org.neo4j.graphdb.*;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.NodeVisitor;
import org.apache.jena.graph.Node_Concrete;
import org.apache.jena.graph.Node_Literal;
import org.apache.jena.graph.impl.LiteralLabel;
import org.apache.jena.graph.impl.LiteralLabelFactory;
import org.apache.jena.rdf.model.AnonId;
import org.apache.jena.shared.PrefixMapping;

/**
 * Jena graph wrapper for Neo4J graph database service.
 * 
 * @author Khalid Latif, Mahek Hanfi (2014-02-14)
 */

public class JenaNeoNode extends Node_Concrete {
	
	/** Neo4J Property Container */
	private final Entity delegate;
	
	/** Neo4J Node */
	private final Node node;
	
	/** Neo4J graph database */
	private GraphDatabaseService graphDb;
	
	/**
	 * Initializes the graph and node.
	 */
	public JenaNeoNode(GraphDatabaseService graphDb, Entity node) {
		super("Test");
		delegate = node;
		this.graphDb = graphDb;
		if(node instanceof Node) {
			//if(node.hasProperty(NeoGraph.PROPERTY_VALUE))
			//System.out.println("Neo node:" + node.getProperty(NeoGraph.PROPERTY_VALUE)); 
			this.node = (Node)node;
			//this.graphDb = node.getGraphDatabase();
		}
		else
			this.node = null;
	}

	/**
	 * Check if the given node is a URI.
	 */
	@Override
	public boolean isURI() {
		Transaction tx = graphDb.beginTx();
		if(node!=null){
			//System.out.println("IS uri");
			return node.hasLabel(Label.label(NeoGraph.LABEL_URI));
		}
		tx.commit();
		return false;
	}
	
	/**
	 * Get the URI of node.
	 */
	@Override
	public String getURI() {
		Transaction tx = graphDb.beginTx();
		String uri = delegate.getProperty(NeoGraph.PROPERTY_URI).toString();
		tx.commit();
		return uri;
	}
	
	/**
	 * Checks if the node has given URI.
	 */
	@Override
	public boolean hasURI( String uri ) {
		if(isURI())
			return getURI().equals(uri);
		return false;
	 }
	
	/**
	 * Get the namespace of node.
	 */
	@Override
	public String getNameSpace(){
		 Transaction tx = graphDb.beginTx();
		 String uri = getURI();
		 String NS = uri.substring(0, org.apache.jena.rdf.model.impl.Util.splitNamespaceXML(uri));
		 tx.commit();
		 return NS;
	 }
	
	/**
	 * Get the local name of node.
	 */
	@Override
	public String getLocalName(){
		Transaction tx = graphDb.beginTx();
		String uri = getURI();
		String name = uri.substring(org.apache.jena.rdf.model.impl.Util.splitNamespaceXML(uri));
		tx.commit();
		return name;
	}
	
	/**
	 * Check if the node is a blank node.
	 */
	@Override
	 public boolean isBlank(){
		Transaction tx = graphDb.beginTx();
		if(node!=null){
			boolean check = node.hasLabel(Label.label(NeoGraph.LABEL_BNODE));
			tx.commit();
			return check;
		}
		return false;
	 }
	
	/**
	 * Get the id of blank node.
	 * @return
	 */
	 @Override
	 public BlankNodeId getBlankNodeId() {
		 Transaction tx = graphDb.beginTx();
		 //AnonId id = AnonId.create(String.valueOf(node.getId()));
		 AnonId id = AnonId.create(delegate.getProperty(NeoGraph.PROPERTY_URI).toString());
		 tx.commit();
		 return id.getBlankNodeId();
	 }

	 /**
	  * Check if the node is a literal.
	  */
	 @Override
	 public boolean isLiteral() {
		 Transaction tx = graphDb.beginTx();
		 if (node!=null) {
			 boolean check = node.hasLabel(Label.label(NeoGraph.LABEL_LITERAL));
			// System.out.println("node is literal");
			 tx.commit();
			 return check;
		 }
		 return false;
	 }
	 
	 @Override
	 public LiteralLabel getLiteral() {
		 graphDb.beginTx();
		 String value = delegate.getProperty(NeoGraph.PROPERTY_VALUE).toString();
		 String language = null;
		 RDFDatatype datatype = null;

		 if(delegate.hasProperty(NeoGraph.PROPERTY_LANGUAGE))
			language = delegate.getProperty(NeoGraph.PROPERTY_LANGUAGE).toString();

		 TypeMapper mapper = new TypeMapper();
		 if(delegate.hasProperty(NeoGraph.PROPERTY_DATATYPE))
			 datatype = mapper.getTypeByName( delegate.getProperty(NeoGraph.PROPERTY_DATATYPE).toString());
		
		 LiteralLabel label = LiteralLabelFactory.create(value, language, true); //datatype);
		 //System.out.println("Label: " +label);
		 return label;
	 }
	 
	 @Override
	 public String getLiteralLexicalForm(){
		 graphDb.beginTx();
		 String value = delegate.getProperty(NeoGraph.PROPERTY_VALUE).toString();
		 return value;
	 }
	 
	 /**
	  * Get the value of literal.
	  */
	 @Override
	 public Object getLiteralValue() {
		 graphDb.beginTx();
		 return delegate.getProperty(NeoGraph.PROPERTY_VALUE);
	 }
	 
	 /**
	  * Get the language of literal.
	  */
	 @Override
	 public String getLiteralLanguage() {
		 graphDb.beginTx();
		 if(delegate.hasProperty(NeoGraph.PROPERTY_LANGUAGE))
			 return delegate.getProperty(NeoGraph.PROPERTY_LANGUAGE).toString();
		 else
			 return null;
	 }
	 
	 /**
	  * Get the data type of literal .
	  */
	 @Override
	 public String getLiteralDatatypeURI(){
		 graphDb.beginTx();
		 if(delegate.hasProperty(NeoGraph.PROPERTY_DATATYPE))
			 return delegate.getProperty(NeoGraph.PROPERTY_DATATYPE).toString();
		 else
			 return null;
	 }
	 
	 @Override
	 public RDFDatatype getLiteralDatatype(){
		 graphDb.beginTx();
		 RDFDatatype datatype = null;
		 TypeMapper mapper = new TypeMapper();
		 if(delegate.hasProperty(NeoGraph.PROPERTY_DATATYPE))
			 datatype = mapper.getTypeByName( delegate.getProperty(NeoGraph.PROPERTY_DATATYPE).toString());
		return datatype;
	 }
	 
	 /**
	  * Checks if the it is a variable .
	  */
	 @Override
	 public boolean isVariable(){ 
		 graphDb.beginTx();
		 return false; 
	 }
	 
	 /**
	  * Checks if the node is concrete .
	  */
	 @Override
	 public  boolean isConcrete(){
		 if(isURI() || isLiteral() || isBlank())
			 return true;
		 else
			 return false;
	 }
	 
	@Override
	public Object visitWith(NodeVisitor v) {
		//if(isLiteral())
			//super.visitWith(v);
		return null;
	}

	 /**
	  * Match the literal value with the given value  .
	  */
	 @Override
	 public boolean sameValueAs(Object o) {
		// System.out.println("Same value as");
		 graphDb.beginTx();
		 org.apache.jena.graph.Node n = (org.apache.jena.graph.Node)o;
		 if(n.isLiteral() && isLiteral()) {
			 // TODO ((LiteralLabel)label).sameValueAs( ((Node_Literal) o).getLiteral() )
			// return n.getLiteralValue().equals(getLiteralValue());
			 //System.out.println("Same value as");
			 return o instanceof Node_Literal 
		              && ((LiteralLabel)label).sameValueAs( ((Node_Literal) o).getLiteral() );
		 }
		 else 
			 return false;
	 }

	/**
	  * Matches the node with the given object .
	 */
	@Override
	public boolean equals(Object o) {
		if(this==o) return true;
		
		graphDb.beginTx();
		//System.out.println("Equal method");
		if(o instanceof Node) {
			//System.out.println("It is instance of Neo4j node");
			return o.equals(delegate);
		} else if(o instanceof org.apache.jena.graph.Node) {
			org.apache.jena.graph.Node n = (org.apache.jena.graph.Node)o;
			if(n.isURI() && isURI()) {
				//System.out.println("It is instance of jena node and is uri");
				return n.hasURI(getURI());
			} else if(n.isLiteral() && isLiteral()) {
				//System.out.println("It is instance of jena node and is literal");
				// return other instanceof Node_Literal && label.equals( ((Node_Literal) other).label );
				return n.getLiteralValue().equals(getLiteralValue());
			} else if(n.isBlank() && isBlank()) {
				//System.out.println("It is instance of jena node and is blank");
				return true; // FIXME
			}
		}
		return false;
	}
	
	@Override
    public String toString( PrefixMapping pm, boolean quoting ) {
		if(isLiteral()) {
			//System.out.println("to string");
			return getLiteral().toString( quoting );
			/*Transaction tx = graphDb.beginTx();
			 String value = delegate.getProperty(NeoGraph.PROPERTY_VALUE).toString();
			 return value;*/
		}
		return super.toString(pm, quoting);
	}

}