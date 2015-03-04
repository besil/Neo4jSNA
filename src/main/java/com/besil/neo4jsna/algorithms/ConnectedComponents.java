package com.besil.neo4jsna.algorithms;

import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.tooling.GlobalGraphOperations;

public class ConnectedComponents {
	protected GraphDatabaseService graph;
	protected final String attName = "ConnectedComponent";

	public ConnectedComponents(GraphDatabaseService g) {	
		this.graph = g;
		try(Transaction tx = this.graph.beginTx()) {
			for(Node n : GlobalGraphOperations.at(g).getAllNodes() ) {
				n.setProperty(attName, n.getId());
			}
			tx.success();
		}
	}

	public void execute(int iterations) {
		try(Transaction tx = this.graph.beginTx()) {
			for(int iteration=0; iteration<iterations; iteration++) {
				System.out.println("Iteration "+iteration);
				for(Node n : GlobalGraphOperations.at(graph).getAllNodes()) {
					long newComponent = this.getLowestComponent(n);
					n.setProperty(attName, newComponent);
				}
			}
			tx.success();
		}
	}
	
	public Long2LongMap getResult() {
		Long2LongMap res = new Long2LongOpenHashMap();
		
		try(Transaction tx = this.graph.beginTx()) {
			for(Node n : GlobalGraphOperations.at(graph).getAllNodes()) {
				res.put(n.getId(), (long) n.getProperty(attName));
			}
			tx.success();
		}
		
		return res;
	}
	
	protected long getLowestComponent(Node n) {
		long minComponent = (long) n.getProperty(attName);

		for(Relationship r : n.getRelationships()) {
			long otherComponent = (long) r.getOtherNode(n).getProperty(attName);
			minComponent = minComponent < otherComponent ? minComponent : otherComponent;
		}
		return minComponent;
	}

}
