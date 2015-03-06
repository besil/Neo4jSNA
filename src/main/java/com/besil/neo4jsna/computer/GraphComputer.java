package com.besil.neo4jsna.computer;

import java.util.logging.Logger;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.tooling.GlobalGraphOperations;

import Utils.Timer;

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
		Timer.timer().start();
		try(Transaction tx = graph.beginTx()) {
			for(Node n : GlobalGraphOperations.at(graph).getAllNodes()) {
				algorithm.init(n);
			}
			tx.success();
		}
		Timer.timer().stop();
		log.info("Init: "+Timer.timer().totalTime());
	}

	protected void main(VertexAlgorithm algorithm) {
		Timer.timer().start();
		try(Transaction tx=graph.beginTx()) {
			for(int it=0; it<algorithm.getMaxIterations(); it++) {
				for(Node n : GlobalGraphOperations.at(graph).getAllNodes()) {
					algorithm.apply(n);
				}
			}
			tx.success();
		}
		Timer.timer().stop();
		log.info("Main: "+Timer.timer().totalTime());
	}

	public void collectResult(VertexAlgorithm algorithm) {
		Timer.timer().start();
		try(Transaction tx=graph.beginTx()) {
			for(Node n: GlobalGraphOperations.at(graph).getAllNodes()) {
				algorithm.collectResult(n);
			}
			tx.success();
		}
		Timer.timer().stop();
		log.info("Collect: "+Timer.timer().totalTime());
	}

}
