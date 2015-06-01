package com.besil.neo4jsna.engine;

import com.besil.neo4jsna.engine.algorithm.CypherAlgorithm;
import com.besil.neo4jsna.engine.algorithm.SingleNodeScanAlgorithm;
import com.besil.neo4jsna.engine.algorithm.VertexAlgorithm;
import com.besil.neo4jsna.measures.DirectedModularity;
import com.besil.neo4jsna.utils.Timer;
import org.neo4j.graphdb.*;
import org.neo4j.tooling.GlobalGraphOperations;

import java.util.logging.Level;
import java.util.logging.Logger;

public class GraphAlgoEngine {
	protected GraphDatabaseService graph;
    private Logger log = Logger.getLogger(GraphAlgoEngine.class.getName());

    public GraphAlgoEngine(GraphDatabaseService g) {
        this.graph = g;
    }
	
	public void execute(DirectedModularity modularity) {
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

    public void execute(SingleNodeScanAlgorithm algo) {
        try (Transaction tx = graph.beginTx()) {
            for (Node n : GlobalGraphOperations.at(graph).getAllNodes()) {
                algo.compute(n);
            }
        }
    }

    public void execute(CypherAlgorithm algorithm) {
		Timer timer = Timer.newTimer();
        Result result = this.graph.execute(algorithm.getQuery());
//        ExecutionResult result = engine.execute(algorithm.getQuery());
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

	public void clean(VertexAlgorithm algo) {
		try( Transaction tx = graph.beginTx() ) {
			for(Node n: GlobalGraphOperations.at(graph).getAllNodes()) {
				String attrName = algo.getAttributeName();
				if( n.hasProperty(attrName) )
					n.removeProperty(attrName);
			}
			tx.success();
		}
	}
	
	public void disableLogging() {
		this.log.setLevel(Level.OFF);
	}
	
}
