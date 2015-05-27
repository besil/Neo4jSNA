package com.besil.neo4jsna.algorithms;

import com.besil.neo4jsna.engine.algorithm.VertexAlgorithm;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

/**
 * Created by besil on 26/05/15.
 */
public class Demon implements VertexAlgorithm {
    private final GraphDatabaseService db;

    public Demon(GraphDatabaseService db) {
        this.db = db;
    }

    public Iterable<Relationship> getEgoNetwork(Node node) {
        return node.getRelationships();
    }

    @Override
    public void init(Node node) {

    }

    @Override
    public void apply(Node node) {

    }

    @Override
    public void collectResult(Node node) {

    }

    @Override
    public int getMaxIterations() {
        return 0;
    }

    @Override
    public String getAttributeName() {
        return "demon";
    }

    @Override
    public String getName() {
        return "Demon";
    }

    @Override
    public Object getResult() {
        return null;
    }
}
