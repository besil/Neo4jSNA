package com.besil.neo4jsna.algorithms.louvain;

import com.besil.neo4jsna.utils.GraphUtils;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.schema.Schema;
import org.neo4j.tooling.GlobalGraphOperations;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Created by besil on 7/24/15.
 */
public class Louvain {
    private final Logger logger = Logger.getLogger(Louvain.class.getName());
    private final String communityProperty = "community";
    private final GraphDatabaseService g;
    private final double totalEdgeWeight;
    private Label nodeLabel;
    private IndexDefinition communityIndex;

    public Louvain(GraphDatabaseService g) {
        this.g = g;
        this.nodeLabel = DynamicLabel.label("nodes");

        try (Transaction tx = g.beginTx()) {
            communityIndex = g.schema().indexFor(nodeLabel).on(communityProperty).create();
            tx.success();
        }

        try (Transaction tx = g.beginTx()) {
            Schema schema = g.schema();
            schema.awaitIndexOnline(communityIndex, 10, TimeUnit.SECONDS);
            tx.success();
        }

        try (Transaction tx = g.beginTx()) {
            for (Node n : GlobalGraphOperations.at(g).getAllNodes()) {
                n.addLabel(this.nodeLabel);
                n.addLabel(this.nodeLabel);
                n.setProperty(communityProperty, n.getId());
            }
            tx.success();
        }


        double edgeWeight = 0.0;
        try (Transaction tx = g.beginTx()) {
            GraphUtils.print(g);

            for (Relationship r : GlobalGraphOperations.at(g).getAllRelationships()) {
                edgeWeight += weight(r);
            }
            tx.success();
        }
        totalEdgeWeight = edgeWeight;
        logger.info("Total edge weight: " + totalEdgeWeight);
    }

    public void pass() {
        try (Transaction tx = g.beginTx()) {
            this.firstPhase();
            tx.success();
        }

        try (Transaction tx = g.beginTx()) {
            this.secondPhase();
            tx.success();
        }
    }

    public void execute() {
        this.pass();
    }

    public void firstPhase() {
        int movements = 0;

        do {
            for (Node src : GlobalGraphOperations.at(g).getAllNodes()) {
                logger.info("Src: " + src);
                long srcCommunity = (long) src.getProperty(communityProperty);

                for (Relationship r : src.getRelationships(Direction.BOTH)) {
                    Node neigh = r.getOtherNode(src);
                    long neighCommunity = (long) neigh.getProperty(communityProperty);

                    double delta = this.calculateDelta(src, srcCommunity, neighCommunity);
                    logger.info("    Dst: " + neigh + " -> " + delta);

                    if (delta > 0) {
                        src.setProperty(communityProperty, neighCommunity);
                        movements++;
                    }
                }
            }
            logger.info("Movements: " + movements);
        } while (movements != 0);
    }

    private double calculateDelta(Node n, long srcCommunity, long dstCommunity) {
        double first, second;
        first = this.communityWeightWithout(n, dstCommunity) - this.communityWeightWithout(n, srcCommunity);
        first = first / totalEdgeWeight;

        second = (this.communityVolumeWithout(n, srcCommunity) - this.communityVolumeWithout(n, dstCommunity)) * nodeVolume(n);
        second = second / (2 * Math.pow(totalEdgeWeight, 2));

        return first + second;
    }

    private double weight(Relationship r) {
        return 1.0;
    }

    private double communityWeightWithout(Node n, long cId) {
        double weight = 0.0;
        for (Relationship r : n.getRelationships(Direction.BOTH)) {
            if (!r.getOtherNode(n).equals(n))
                weight += this.weight(r);
        }
        return weight;
    }

    private double communityWeight(Node n, long cId) {
        double weight = 0.0;
        for (Relationship r : n.getRelationships(Direction.BOTH)) {
            weight += this.weight(r);
        }
        return weight;
    }

    private double nodeVolume(Node n) {
        double vol = 0;
        for (Relationship r : n.getRelationships(Direction.BOTH)) {
            vol += this.weight(r);   // put here the edge weight

            if (r.getOtherNode(n).equals(n))
                vol += this.weight(r);
        }
        return vol;
    }

    private double communityVolumeWithout(Node n, long cId) {
        double vol = 0;
        ResourceIterator<Node> members = g.findNodes(nodeLabel, communityProperty, cId);
        while (members.hasNext()) {
            Node member = members.next();
            if (!member.equals(n))
                vol += nodeVolume(member);
        }
        return vol;
    }

    private double communityVolume(long cId) {
        double vol = 0;
        ResourceIterator<Node> members = g.findNodes(nodeLabel, communityProperty, cId);
        while (members.hasNext()) {
            Node member = members.next();
            vol += nodeVolume(member);
        }
        return vol;
    }

    public void secondPhase() {

    }

    public LouvainResult getResult() {
        return new LouvainResult();
    }

    public void clean() {
        for (Node n : GlobalGraphOperations.at(g).getAllNodes()) {
            n.removeProperty(communityProperty);
        }
    }
}
