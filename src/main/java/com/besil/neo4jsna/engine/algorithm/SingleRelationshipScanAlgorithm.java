package com.besil.neo4jsna.engine.algorithm;

import org.neo4j.graphdb.Relationship;

public interface SingleRelationshipScanAlgorithm extends Algorithm {
	public abstract void compute(Relationship r);
}
