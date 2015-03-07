package com.besil.neo4jsna.engine;

import org.neo4j.cypher.javacompat.ExecutionResult;

public interface CypherAlgorithm {
	public String getName();
	public String getQuery();
	public void collectResult(ExecutionResult result);
	public Object getResult();
}
