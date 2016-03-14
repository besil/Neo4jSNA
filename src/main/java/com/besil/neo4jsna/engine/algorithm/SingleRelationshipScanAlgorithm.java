package com.besil.neo4jsna.engine.algorithm;

import org.neo4j.graphdb.Relationship;

public interface SingleRelationshipScanAlgorithm<T> extends Algorithm<T> {
	void compute(Relationship r);
}
