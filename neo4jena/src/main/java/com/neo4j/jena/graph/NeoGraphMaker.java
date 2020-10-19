/*
 * Copyright 2014 NUST. All rights reserved.
 * Use is subject to license terms.
 */
package com.neo4j.jena.graph;

import org.apache.jena.graph.impl.SimpleGraphMaker;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphMaker;
import org.apache.jena.graph.Node;
import org.apache.jena.util.iterator.ExtendedIterator;

public class NeoGraphMaker extends SimpleGraphMaker implements GraphMaker {
	
	NeoGraph neoGraph;
	
	public Graph addDescription(Graph arg0, Node arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void close() {
		neoGraph.close();
	}

	@Override
	public Graph createGraph() {
		// TODO Auto-generated method stub
		return neoGraph;
	}

	@Override
	public Graph createGraph(String arg0) {
		// TODO Auto-generated method stub
		
		return null;
	}

	@Override
	public Graph createGraph(String arg0, boolean arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public Graph getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	public Graph getDescription(Node arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Graph getGraph() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasGraph(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ExtendedIterator listGraphs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Graph openGraph() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Graph openGraph(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Graph openGraph(String arg0, boolean arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeGraph(String arg0) {
		// TODO Auto-generated method stub	
	}
}