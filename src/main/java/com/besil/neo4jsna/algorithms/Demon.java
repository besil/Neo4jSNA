package com.besil.neo4jsna.algorithms;

import com.besil.neo4jsna.engine.GraphAlgoEngine;
import com.besil.neo4jsna.engine.algorithm.SingleNodeScanAlgorithm;
import com.besil.neo4jsna.utils.GraphUtils;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
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
    private final GraphAlgoEngine engine;
    private final LabelPropagation lp;
    public Demon(GraphDatabaseService db) {
        this.db = db;
        this.engine = new GraphAlgoEngine(db);
        engine.disableLogging();
        this.lp = new LabelPropagation(DemonRelType.DEMON_RELTYPE);
    }

    @Override
    public void compute(Node ego) {
        this.executeEgoMinusEgo(ego);
        engine.execute(lp, DemonLabel);

        Long2LongMap communities = lp.getResult();
    }

    public void executeEgoMinusEgo(Node root) {
        LongList neighbours = new LongArrayList();
        int nodeMarked = 0;

        for (Relationship r : root.getRelationships(Direction.OUTGOING)) {
            Node neigh = r.getEndNode();
            neigh.addLabel(DemonLabel);
            neighbours.add(neigh.getId());
            nodeMarked++;
        }

        int relMarked = 0;
        for (long neigh : neighbours) {
            Node neighNode = db.getNodeById(neigh);
            for (Relationship r : neighNode.getRelationships(Direction.OUTGOING)) {
                Node other = r.getEndNode();
                if (neighbours.contains(other.getId())) {
                    neighNode.createRelationshipTo(other, DemonRelType.DEMON_RELTYPE);
                    relMarked++;
                }
            }
        }
        if (relMarked > 0) {
            System.out.println("Marked " + nodeMarked + " nodes");
            System.out.println("Marked " + relMarked + " relationships");
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
