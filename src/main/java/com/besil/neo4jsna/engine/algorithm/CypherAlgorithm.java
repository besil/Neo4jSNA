package com.besil.neo4jsna.engine.algorithm;

import org.neo4j.graphdb.Result;

public interface CypherAlgorithm extends Algorithm {
	public String getQuery();

	public void collectResult(Result result);

}
