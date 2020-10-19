package com.neo4j.jena.graph;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

//import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.*;
//import org.neo4j.graphdb.PropertyContainer;

import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.util.iterator.ExtendedIterator;
//import org.apache.jena.util.iterator.Filter;
//import org.apache.jena.util.iterator.Map1;

public class ExecutionResultIterator implements ExtendedIterator<Triple> {

	private Result results;
	//private ExecutionResult results;
	private ResourceIterator<Map<String,Object>> delegate;
	//private Transaction tx;
	final GraphDatabaseService graphdb;
	
	public ExecutionResultIterator(Result results, GraphDatabaseService graphdb) {
		this.results = results;
		//System.out.println("In iterator constructor");
		//this.delegate = results.iterator();
		this.graphdb = graphdb;
		//this.transaction = tx;
		
		//System.out.println(delegate.hasNext());
		//System.out.println("constructor next:"+delegate.next());
		//System.out.println(results.dumpToString());
		
		//System.out.println(delegate.next().size());
	}

	@Override
	public void close() {
		delegate.close();
		//tx.success();
	}

	@Override
	public boolean hasNext() {
		//System.out.println(transaction);
		//System.out.println("ExecutionResultIterator#hasNext");
		try(Transaction tx = graphdb.beginTx()) {
			return delegate.hasNext();
		}
	}

	@Override
	public Triple next() {
		//System.out.println("ExecutionResultIterator#next");
		try(Transaction tx = graphdb.beginTx()) {
		Map<String,Object> row = delegate.next();
		//System.out.println("In execution iterator subject: " + row.get("subject") + row.get("subject").getClass());
		
		//Node nsubject = (Node) row.get("subject");
//		JenaNeoNode neonode = new JenaNeoNode(nsubject);
//		System.out.println("Node is uri:" + neonode.isURI());
		
		//System.out.println("Subject: "+ new JenaNeoNode((Node)row.get("subject")));
		
		Triple t = new Triple(new JenaNeoNode(graphdb, (Node)row.get("subject")),
				ResourceFactory.createProperty((String)row.get("type(predicate)")).asNode(),
				new JenaNeoNode(graphdb, (Node)row.get("object")));
		return t;
		}
	}

	@Override
	public void remove() {
		delegate.remove();
	}

	@Override
	public Triple removeNext() {
		Triple t = next();
		remove();
		return t;
	}

	@Override
	public <X extends Triple> ExtendedIterator<Triple> andThen(Iterator<X> other) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ExtendedIterator<Triple> filterKeep(Predicate<Triple> f) {
		return null;
	}

	@Override
	public ExtendedIterator<Triple> filterDrop(Predicate<Triple> f) {
		return null;
	}

	@Override
	public <U> ExtendedIterator<U> mapWith(Function<Triple, U> map1) {
		return null;
	}

	@Override
	public List<Triple> toList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Triple> toSet() {
		return new HashSet<Triple>(toList());
	}

}
