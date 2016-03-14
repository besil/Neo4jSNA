package com.besil.neo4jsna.engine.algorithm;

import org.neo4j.graphdb.Node;

public interface VertexAlgorithm<T> extends Algorithm<T> {
	void init(Node node);

	void apply(Node node);

	void collectResult(Node node);

	int getMaxIterations();

	String getAttributeName();
}
