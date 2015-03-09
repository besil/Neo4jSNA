package com.besil.neo4jsna.engine.algorithm;

import org.neo4j.graphdb.Node;

public interface SingleNodeScanAlgorithm extends Algorithm {
	public abstract void compute(Node n);
}
