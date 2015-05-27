package com.besil.neo4jsna.algorithms;

import com.besil.neo4jsna.engine.algorithm.VertexAlgorithm;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by besil on 26/05/15.
 */
public class Demon implements VertexAlgorithm {
    private final GraphDatabaseService db;

    public Demon(GraphDatabaseService db) {
        this.db = db;
    }

    public List<Relationship> getEgoNetwork(Node root) {
        List<Relationship> rels = new LinkedList<>();
        LongSet neighbours = new LongOpenHashSet();

        for (Relationship r : root.getRelationships(Direction.OUTGOING)) {
            Node neigh = r.getOtherNode(root);
            neighbours.add(neigh.getId());
            rels.add(r);
        }

        for (long neigh : neighbours) {
            Node neighNode = db.getNodeById(neigh);
            for (Relationship r : neighNode.getRelationships(Direction.OUTGOING)) {
                Node other = r.getOtherNode(neighNode);
                if (neighbours.contains(other.getId()))
                    rels.add(r);
            }
        }
        return rels;
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
