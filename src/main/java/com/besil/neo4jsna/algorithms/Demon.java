package com.besil.neo4jsna.algorithms;

import com.besil.neo4jsna.engine.algorithm.SingleNodeScanAlgorithm;
import com.besil.neo4jsna.utils.GraphUtils;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import org.neo4j.graphdb.*;

import java.util.Iterator;

/**
 * Created by besil on 26/05/15.
 */
public class Demon implements SingleNodeScanAlgorithm {
    public static final Label DemonLabel = DynamicLabel.label("DEMON_NODE");
    private final GraphDatabaseService db;
    public Demon(GraphDatabaseService db) {
        this.db = db;
    }

    @Override
    public void compute(Node n) {

    }

    public void executeEgoMinusEgo(Node root) {
        LongList neighbours = new LongArrayList();

        for (Relationship r : root.getRelationships(Direction.OUTGOING)) {
            Node neigh = r.getEndNode();
            neigh.addLabel(DemonLabel);
            neighbours.add(neigh.getId());
        }

        for (long neigh : neighbours) {
            Node neighNode = db.getNodeById(neigh);
            for (Relationship r : neighNode.getRelationships(Direction.OUTGOING)) {
                Node other = r.getEndNode();
                if (neighbours.contains(other.getId()))
                    neighNode.createRelationshipTo(other, DemonRelType.DEMON_RELTYPE);
            }
        }
    }

    public ResourceIterator<Node> getDemonNodes() {
        return db.findNodes(DemonLabel);
    }

    public Iterator<Relationship> getDemonRelationships() {
        return GraphUtils.getRelationshisByNodeAndRelationshipType(db, Demon.DemonLabel, Demon.DemonRelType.DEMON_RELTYPE);
    }

    /**
     * Mark the nodes of the egoNetwork of node root with label DemonLabel and create the relationships of type DemonRelType
     *
     * @param root
     * @return
     */
    public void setEgoNetwork(Node root) {
        LongList neighbours = new LongArrayList();

        root.addLabel(DemonLabel);
        for (Relationship r : root.getRelationships(Direction.OUTGOING)) {
            Node neigh = r.getOtherNode(root);
            neigh.addLabel(DemonLabel);
            root.createRelationshipTo(neigh, DemonRelType.DEMON_RELTYPE);
            neighbours.add(neigh.getId());
        }

        for (long neigh : neighbours) {
            Node neighNode = db.getNodeById(neigh);
            for (Relationship r : neighNode.getRelationships(Direction.OUTGOING)) {
                Node other = r.getOtherNode(neighNode);
                if (neighbours.contains(other.getId()))
                    neighNode.createRelationshipTo(other, DemonRelType.DEMON_RELTYPE);
            }
        }
    }

    public void clearEgoNetwork(Node root) {
        Iterator<Relationship> demonRels = GraphUtils.getRelationshisByNodeAndRelationshipType(db, DemonLabel, DemonRelType.DEMON_RELTYPE);
        while (demonRels.hasNext())
            demonRels.next().delete();
        ResourceIterator<Node> nodes = db.findNodes(DemonLabel);
        while (nodes.hasNext())
            nodes.next().removeLabel(DemonLabel);
    }

    @Override
    public String getName() {
        return "Demon";
    }

    @Override
    public Object getResult() {
        return null;
    }

    public static enum DemonRelType implements RelationshipType {DEMON_RELTYPE;}

}
