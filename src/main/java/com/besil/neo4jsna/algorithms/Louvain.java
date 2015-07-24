package com.besil.neo4jsna.algorithms;

import com.besil.neo4jsna.engine.algorithm.SingleNodeScanAlgorithm;
import com.besil.neo4jsna.engine.algorithm.VertexAlgorithm;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.tooling.GlobalGraphOperations;

/**
 * Created by besil on 7/24/15.
 */
public class Louvain {
    final String communityProperty = "comm";

    public void firstPhase(GraphDatabaseService graph) {
        for(Node n : GlobalGraphOperations.at(graph).getAllNodes()) {
            for(Relationship r : n.getRelationships(Direction.BOTH) ) {

            }
        }
    }

    public void secondPhase(GraphDatabaseService graph) {

    }

    public void init(GraphDatabaseService graph) {
        for(Node n : GlobalGraphOperations.at(graph).getAllNodes()) {
            n.setProperty(communityProperty, n.getId());
        }
    }
}
