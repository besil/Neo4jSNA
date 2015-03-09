package com.besil.neo4jsna.engine.algorithm;

import org.neo4j.graphdb.Node;

public abstract interface VertexAlgorithm extends Algorithm {
	public abstract void init(Node node);
	public abstract void apply(Node node);
	public abstract void collectResult(Node node);
	
	public abstract int getMaxIterations();
}
