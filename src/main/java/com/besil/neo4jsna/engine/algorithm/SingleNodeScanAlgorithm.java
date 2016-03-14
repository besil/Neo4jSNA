package com.besil.neo4jsna.engine.algorithm;

import org.neo4j.graphdb.Node;

public interface SingleNodeScanAlgorithm<T> extends Algorithm<T> {
    void compute(Node n);
}
