package com.besil.neo4jsna.engine.algorithm;

import org.neo4j.cypher.javacompat.ExecutionResult;

public interface CypherAlgorithm extends Algorithm {
	public String getQuery();
	public void collectResult(ExecutionResult result);

}
