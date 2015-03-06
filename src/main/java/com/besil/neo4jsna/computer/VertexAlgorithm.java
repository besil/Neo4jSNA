package com.besil.neo4jsna.computer;

import org.neo4j.graphdb.Node;

public abstract interface VertexAlgorithm {
	public abstract void init(Node node);
	public abstract void apply(Node node);
	public abstract Object collectResult(Node node);
	
	public abstract int getMaxIterations();
	public abstract String getName();
	public abstract Object getResult();
}
