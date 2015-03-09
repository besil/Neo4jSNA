package com.besil.neo4jsna.measures;

import org.neo4j.graphdb.GraphDatabaseService;

public class UndirectedModularity extends DirectedModularity {

	public UndirectedModularity(GraphDatabaseService g) {
		super(g);
	}
	
	@Override
	public Double getResult() {
		return ( ai / (4*Math.pow(divisor, 2)) ) - ( eii / (2*divisor) );	// Undirected
	}

}
