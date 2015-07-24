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
    private final String communityProperty = "community", layerProperty = "layer";
    private final GraphDatabaseService g;
    private final double totalEdgeWeight;
    private Label activeLabel, newLayer;
    private IndexDefinition communityIndex, layerIndex, newLayerIndex;
    private int layerCount;
    private int passCount = 0;

    public Louvain(GraphDatabaseService g) {
        this.g = g;
        layerCount = 0;
        this.activeLabel = DynamicLabel.label("activeNode");
        this.newLayer = DynamicLabel.label("newLayer");

        try (Transaction tx = g.beginTx()) {
            communityIndex = g.schema().indexFor(activeLabel).on(communityProperty).create();
            layerIndex = g.schema().indexFor(activeLabel).on(layerProperty).create();
            newLayerIndex = g.schema().indexFor(newLayer).on(communityProperty).create();
            tx.success();
        }

        try (Transaction tx = g.beginTx()) {
            Schema schema = g.schema();
            schema.awaitIndexOnline(communityIndex, 10, TimeUnit.SECONDS);
            schema.awaitIndexOnline(layerIndex, 10, TimeUnit.SECONDS);
            schema.awaitIndexOnline(newLayerIndex, 10, TimeUnit.SECONDS);
            tx.success();
        }

        try (Transaction tx = g.beginTx()) {
            for (Node n : GlobalGraphOperations.at(g).getAllNodes()) {
                n.addLabel(this.activeLabel);
//                n.setProperty(communityProperty, n.getId() + ":" + layerCount);
                n.setProperty(communityProperty, n.getId());
                n.setProperty(layerProperty, layerCount);
            }
            tx.success();
        }


        double edgeWeight = 0.0;
        try (Transaction tx = g.beginTx()) {
            for (Relationship r : GlobalGraphOperations.at(g).getAllRelationships()) {
                edgeWeight += weight(r);
            }
            tx.success();
        }
        totalEdgeWeight = edgeWeight;
        logger.info("Total edge weight: " + totalEdgeWeight);
    }

    public int pass() {
        String mex = "Starting pass " + passCount;
        logger.info("----------");
        logger.info(mex);
        logger.info("----------");

        logger.info("--------- Start Moving phase");
        try (Transaction tx = g.beginTx()) {
            this.firstPhase();
            GraphUtils.print(g);
            tx.success();
        }

        GraphAlgoEngine engine = new GraphAlgoEngine(g);
        UndirectedModularity um = new UndirectedModularity(g);
        engine.execute(um);
        logger.info("Modularity: " + um.getResult());

        logger.info("--------- Start Aggregation phase");
        int totMacroNodes = 0;
        try (Transaction tx = g.beginTx()) {
            totMacroNodes = this.secondPhase();
            GraphUtils.print(g);
            tx.success();
        }
        passCount++;

        return totMacroNodes;
    }

    public void execute() {
        int totMacroNodes;

        do {
            totMacroNodes = this.pass();
        } while (totMacroNodes != 0);
    }

    public void firstPhase() {
        int movements;

        do {
            movements = 0;
            ResourceIterator<Node> nodes = g.findNodes(activeLabel);
            while (nodes.hasNext()) {
                Node src = nodes.next();
//                logger.info("Src: " + src);
                long srcCommunity = (long) src.getProperty(communityProperty);
                long bestCommunity = srcCommunity;
                double bestDelta = 0.0;

                Iterable<Relationship> rels = passCount == 0 ? src.getRelationships(Direction.BOTH) : src.getRelationships(Direction.BOTH, LouvainRels.NewEdges);
                for (Relationship r : rels) {
                    logger.info(r.getType().toString());
                    Node neigh = r.getOtherNode(src);
                    long neighCommunity = (long) neigh.getProperty(communityProperty);

                    logger.info("Calculating movement for (" + src + ", " + neigh + ")");
                    double delta = this.calculateDelta(src, srcCommunity, neighCommunity);
//                    logger.info("    Dst: " + neigh + " -> " + delta);

                    if (delta > bestDelta) {
                        bestDelta = delta;
                        bestCommunity = neighCommunity;
                    }
                }

                if (srcCommunity != bestCommunity) {
                    logger.info("**** Moving " + src + " to community " + bestCommunity);
                    src.setProperty(communityProperty, bestCommunity);
                    movements++;
                }
            }

            logger.info("Movements: " + movements);

        } while (movements != 0);
    }

    private double calculateDelta(Node n, long srcCommunity, long dstCommunity) {
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

    private double communityWeightWithout(Node n, long cId) {
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

    private double communityVolumeWithout(Node n, long cId) {
        double vol = 0;
        ResourceIterator<Node> members = g.findNodes(activeLabel, communityProperty, cId);
        while (members.hasNext()) {
            Node member = members.next();
            if (!member.equals(n))
                vol += nodeVolume(member);
        }
        return vol;
    }

    private double communityVolume(String cId) {
        double vol = 0;

        ResourceIterator<Node> members = g.findNodes(activeLabel, communityProperty, cId);
        while (members.hasNext()) {
            Node member = members.next();
            vol += nodeVolume(member);
        }
        return vol;
    }

    public int secondPhase() {
        layerCount++;
        int totMacroNodes = 0;

        g.findNodes(newLayer).forEachRemaining(n -> n.removeLabel(newLayer));

        GraphUtils.print(g);
        if (true) throw new RuntimeException();

        ResourceIterator<Node> activeNodes = g.findNodes(activeLabel);
        while (activeNodes.hasNext()) {
            Node activeNode = activeNodes.next();
            long cId = (long) activeNode.getProperty(communityProperty);

            Node macroNode = g.findNode(newLayer, communityProperty, cId);
            if (macroNode == null) {
                totMacroNodes++;
                macroNode = g.createNode(newLayer);
//                macroNode.addLabel(newLayer);
                macroNode.setProperty(communityProperty, cId);
                macroNode.setProperty(layerProperty, layerCount);
            }
            // Create a relationship to the original node
            activeNode.createRelationshipTo(macroNode, LouvainRels.Layer);

            // Now you must connect the macronodes (if any)!
            for (Relationship r : activeNode.getRelationships()) {
                if (!r.isType(LouvainRels.Layer)) {
                    Node neigh = r.getOtherNode(activeNode);

                    long neighCid = (long) neigh.getProperty(communityProperty);
                    Node otherMacroNode = g.findNode(newLayer, communityProperty, neighCid);
                    if (otherMacroNode == null) {
                        totMacroNodes++;
                        otherMacroNode = g.createNode(newLayer);
                        otherMacroNode.setProperty(communityProperty, cId);
                        otherMacroNode.setProperty(layerProperty, layerCount);
                    }
                    if (!otherMacroNode.equals(macroNode)) {
                        otherMacroNode.createRelationshipTo(macroNode, LouvainRels.NewEdges);
                        macroNode.createRelationshipTo(otherMacroNode, LouvainRels.NewEdges);
                    }

                }
            }
            activeNode.removeLabel(activeLabel);
        }

        ResourceIterator<Node> macros = g.findNodes(newLayer);
        while (macros.hasNext()) {
            Node next = macros.next();
            next.removeLabel(newLayer);
            next.addLabel(activeLabel);
        }

        return totMacroNodes;
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
        Layer, NewEdges
    }
}
