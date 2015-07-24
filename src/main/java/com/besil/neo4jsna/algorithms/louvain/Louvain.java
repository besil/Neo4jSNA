package com.besil.neo4jsna.algorithms.louvain;

import com.besil.neo4jsna.engine.GraphAlgoEngine;
import com.besil.neo4jsna.measures.UndirectedModularity;
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
    private final String communityProperty = "community", activeProperty = "layer";
    private final GraphDatabaseService g;
    private final double totalEdgeWeight;
    private Label nodeLabel;
    private IndexDefinition communityIndex, activeIndex;
    private int layerCount;

    public Louvain(GraphDatabaseService g) {
        this.g = g;
        layerCount = 0;
        this.nodeLabel = DynamicLabel.label("nodes");

        try (Transaction tx = g.beginTx()) {
            communityIndex = g.schema().indexFor(nodeLabel).on(communityProperty).create();
            activeIndex = g.schema().indexFor(nodeLabel).on(activeProperty).create();
            tx.success();
        }

        try (Transaction tx = g.beginTx()) {
            Schema schema = g.schema();
            schema.awaitIndexOnline(communityIndex, 10, TimeUnit.SECONDS);
            schema.awaitIndexOnline(activeIndex, 10, TimeUnit.SECONDS);
            tx.success();
        }

        try (Transaction tx = g.beginTx()) {
            for (Node n : GlobalGraphOperations.at(g).getAllNodes()) {
                n.addLabel(this.nodeLabel);
                n.setProperty(communityProperty, n.getId() + ":" + layerCount);
                n.setProperty(activeProperty, true);
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

        GraphAlgoEngine engine = new GraphAlgoEngine(g);
        UndirectedModularity um = new UndirectedModularity(g);
        engine.execute(um);
        logger.info("Modularity: " + um.getResult());

        try (Transaction tx = g.beginTx()) {
            this.secondPhase();
            tx.success();
        }
    }

    public void execute() {
        this.pass();
    }

    public void firstPhase() {
        int movements;

        do {
            movements = 0;
            for (Node src : GlobalGraphOperations.at(g).getAllNodes()) {
                logger.info("Src: " + src);
                String srcCommunity = (String) src.getProperty(communityProperty);
                String bestCommunity = srcCommunity;
                double bestDelta = 0.0;

                for (Relationship r : src.getRelationships(Direction.BOTH)) {
                    Node neigh = r.getOtherNode(src);
                    String neighCommunity = (String) neigh.getProperty(communityProperty);

                    double delta = this.calculateDelta(src, srcCommunity, neighCommunity);
                    logger.info("    Dst: " + neigh + " -> " + delta);

                    if (delta > bestDelta) {
//                        logger.info("        Moving "+src+" to community "+neighCommunity);
//                        src.setProperty(communityProperty, neighCommunity);
//                        movements++;
                        bestDelta = delta;
                        bestCommunity = neighCommunity;
                    }
                }

                if (srcCommunity != bestCommunity) {
                    logger.info("        Moving " + src + " to community " + bestCommunity);
                    src.setProperty(communityProperty, bestCommunity);
                    movements++;
                }
            }


            GraphUtils.print(g);
            logger.info("Movements: " + movements);

//            try {
//                Thread.sleep(5000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
        } while (movements != 0);
    }

    private double calculateDelta(Node n, String srcCommunity, String dstCommunity) {
        double first, second;

//        logger.info("Diff between: "+this.communityWeightWithout(n, dstCommunity) +" - "+ this.communityVolumeWithout(n, srcCommunity) );
        first = this.communityWeightWithout(n, dstCommunity) - this.communityWeightWithout(n, srcCommunity);
//        logger.info("First first: "+first);
        first = first / totalEdgeWeight;

//        logger.info("First: "+first);

        second = (this.communityVolumeWithout(n, srcCommunity) - this.communityVolumeWithout(n, dstCommunity)) * nodeVolume(n);
        second = second / (2 * Math.pow(totalEdgeWeight, 2));

//        logger.info("Second: "+second);

//        logger.info("Delta: "+(first+second));

        return first + second;
    }

    private double weight(Relationship r) {
        return 1.0;
    }

    private double communityWeightWithout(Node n, String cId) {
        double weight = 0.0;
        for (Relationship r : n.getRelationships(Direction.BOTH)) {
            if (!r.getOtherNode(n).equals(n))
                if (r.getOtherNode(n).getProperty(communityProperty).equals(cId))
                    weight += this.weight(r);
        }
        return weight;
    }

    private double communityWeight(Node n, String cId) {
        double weight = 0.0;
        for (Relationship r : n.getRelationships(Direction.BOTH)) {
            if (r.getOtherNode(n).getProperty(communityProperty).equals(cId))
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

    private double communityVolumeWithout(Node n, String cId) {
        double vol = 0;
        ResourceIterator<Node> members = g.findNodes(nodeLabel, communityProperty, cId);
        while (members.hasNext()) {
            Node member = members.next();
            if (!member.equals(n))
                vol += nodeVolume(member);
        }
        return vol;
    }

    private double communityVolume(String cId) {
        double vol = 0;

        ResourceIterator<Node> members = g.findNodes(nodeLabel, communityProperty, cId);
        while (members.hasNext()) {
            Node member = members.next();
            vol += nodeVolume(member);
        }
        return vol;
    }

    public void secondPhase() {
        layerCount++;

        for (Node n : GlobalGraphOperations.at(g).getAllNodes()) {
            String[] split = ((String) n.getProperty(communityProperty)).split(":");
            String cId = split[0] + ":" + layerCount;
            Node macroNode = g.findNode(nodeLabel, communityProperty, cId);
            if (macroNode == null) {
                macroNode = g.createNode(nodeLabel);
                macroNode.setProperty(communityProperty, cId);
                macroNode.setProperty(activeProperty, true);
            }
            n.createRelationshipTo(macroNode, LouvainRels.Layer);
            n.setProperty(activeProperty, false);
        }
        GraphUtils.print(g);
    }

    public LouvainResult getResult() {
        return new LouvainResult();
    }

    public void clean() {
        for (Node n : GlobalGraphOperations.at(g).getAllNodes()) {
            n.removeProperty(communityProperty);
        }
    }

    enum LouvainRels implements RelationshipType {
        Layer
    }
}
