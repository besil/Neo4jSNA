package com.besil.neo4jsna.engine.algorithm;

import org.neo4j.graphdb.Node;

public interface SingleNodeScanAlgorithm extends Algorithm {
    void compute(Node n);
}
