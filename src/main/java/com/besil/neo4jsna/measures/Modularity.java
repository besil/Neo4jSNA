package com.besil.neo4jsna.measures;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.tooling.GlobalGraphOperations;

import com.besil.neo4jsna.engine.algorithm.SingleNodeScanAlgorithm;
import com.besil.neo4jsna.engine.algorithm.SingleRelationshipScanAlgorithm;

public class Modularity implements SingleNodeScanAlgorithm, SingleRelationshipScanAlgorithm {
	protected final String attName = "community";
	protected double eii=0.0, ai=0.0;
	protected double divisor = 0.0;
	
	public Modularity(GraphDatabaseService g) {
		try( Transaction tx = g.beginTx() ) {
			for( @SuppressWarnings("unused") Relationship r : GlobalGraphOperations.at(g).getAllRelationships() ) divisor += 1.0;
			tx.success();
		}
	}
	
	public void compute(Node n) {
		double degree = n.getDegree(Direction.INCOMING) * n.getDegree(Direction.OUTGOING);
		ai += degree;
	}

	public void compute(Relationship r) {
		Node n1 = r.getStartNode();
		Node n2 = r.getEndNode();
		
		if( n1.getProperty(attName) == n2.getProperty(attName) ) {
			double weight = r.hasProperty("weight") ? (double) r.getProperty("weight") : 1.0;
			eii += weight;
		}
	}

	@Override
	public String getName() {
		return "Modularity";
	}

	@Override
	public Double getResult() {
		return ( ai / Math.pow(divisor, 2) ) - ( eii / divisor );				// Directed
//		// return ( ai / (4*Math.pow(divisor, 2)) ) - ( eii / (2*divisor) );	// Undirected
	}

}
