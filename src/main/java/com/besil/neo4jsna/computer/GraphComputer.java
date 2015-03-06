package com.besil.neo4jsna.computer;

import java.util.logging.Logger;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.tooling.GlobalGraphOperations;

public class GraphComputer {
	private final Logger log = Logger.getLogger(GraphComputer.class.getName()); 

	protected GraphDatabaseService graph;

	public GraphComputer(GraphDatabaseService g) {	
		this.graph = g;
	}

	public void execute(VertexAlgorithm algorithm) {
		this.initPhase(algorithm);
		this.main(algorithm);
		this.collectResult(algorithm);
	}

	protected void initPhase(VertexAlgorithm algorithm) {
		try(Transaction tx = graph.beginTx()) {
			for(Node n : GlobalGraphOperations.at(graph).getAllNodes()) {
				algorithm.init(n);
			}
			tx.success();
		}
	}

	protected void main(VertexAlgorithm algorithm) {
		try(Transaction tx=graph.beginTx()) {
			for(int it=0; it<algorithm.getMaxIterations(); it++) {
				log.info("Iteration "+it);
				for(Node n : GlobalGraphOperations.at(graph).getAllNodes()) {
					algorithm.apply(n);
				}
			}
		}
	}

	public void collectResult(VertexAlgorithm algorithm) {
		try(Transaction tx=graph.beginTx()) {
			for(Node n: GlobalGraphOperations.at(graph).getAllNodes()) {
				algorithm.collectResult(n);
			}
		}
	}

}
