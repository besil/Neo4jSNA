package com.besil.neo4jsna.engine.algorithm;

import org.neo4j.graphdb.Result;

public interface CypherAlgorithm<T> extends Algorithm<T> {
	String getQuery();

	void collectResult(Result result);

}
