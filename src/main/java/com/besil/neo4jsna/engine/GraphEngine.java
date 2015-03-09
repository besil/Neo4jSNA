package com.besil.neo4jsna.engine;

import java.util.logging.Logger;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.tooling.GlobalGraphOperations;

import com.besil.neo4jsna.engine.algorithm.CypherAlgorithm;
import com.besil.neo4jsna.engine.algorithm.VertexAlgorithm;
import com.besil.neo4jsna.measures.Modularity;
import com.besil.neo4jsna.utils.Timer;

public class GraphEngine {
	private final Logger log = Logger.getLogger(GraphEngine.class.getName()); 
	protected GraphDatabaseService graph;
	protected ExecutionEngine engine;
	
	public GraphEngine(GraphDatabaseService g) {	
		this.graph = g;
		this.engine = new ExecutionEngine(g);
	}
	
	public void execute(Modularity modularity) {
		try( Transaction tx = graph.beginTx() ) {
			for(Node n : GlobalGraphOperations.at(graph).getAllNodes()) {
				modularity.compute(n);
			}
			for(Relationship r : GlobalGraphOperations.at(graph).getAllRelationships()) {
				modularity.compute(r);
			}
			tx.success();
		}
	}
	
	public void execute(CypherAlgorithm algorithm) {
		Timer timer = Timer.newTimer();
		ExecutionResult result = engine.execute(algorithm.getQuery());
		algorithm.collectResult(result);
		timer.stop();
		log.info(algorithm.getName()+" execution: "+timer.totalTime());
	}

	public void execute(VertexAlgorithm algorithm) {
		Timer timer = Timer.newTimer();
		timer.start();

		try( Transaction tx = graph.beginTx() ) {
			this.initPhase(algorithm);
			this.main(algorithm);
			this.collectResult(algorithm);
			tx.success();
		}

		timer.stop();
		log.info("Execute: "+timer.totalTime());
	}

	protected void initPhase(VertexAlgorithm algorithm) {
		Timer.timer().start();
		for(Node n : GlobalGraphOperations.at(graph).getAllNodes()) 
			algorithm.init(n);
		Timer.timer().stop();
		log.info("Init: "+Timer.timer().totalTime());
	}

	protected void main(VertexAlgorithm algorithm) {
		Timer.timer().start();
		for(int it=0; it<algorithm.getMaxIterations(); it++) {
			for(Node n : GlobalGraphOperations.at(graph).getAllNodes()) {
				algorithm.apply(n);
			}
		}
		Timer.timer().stop();
		log.info("Main: "+Timer.timer().totalTime());
	}

	public void collectResult(VertexAlgorithm algorithm) {
		Timer.timer().start();
		for(Node n: GlobalGraphOperations.at(graph).getAllNodes()) {
			algorithm.collectResult(n);
		}
		Timer.timer().stop();
		log.info("Collect: "+Timer.timer().totalTime());
	}

}
