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
    private final String communityProperty = "community", layerProperty = "layer", weightProperty = "weight";
    private final GraphDatabaseService g;
    private final double totalEdgeWeight;
    private Label layerLabel, communityLabel, newLayerLabel;
    private IndexDefinition layerIndex, communityIndex, tmpNewLayerIndex;
    private int layerCount;
    private int passCount = 0;

    public Louvain(GraphDatabaseService g) {
        this.g = g;
        layerCount = 0;
        this.layerLabel = DynamicLabel.label("layerLabel");
        this.communityLabel = DynamicLabel.label("communityLabel");
        this.newLayerLabel = DynamicLabel.label("newLayerLabel");


        try (Transaction tx = g.beginTx()) {
            this.layerIndex = g.schema().indexFor(layerLabel).on(layerProperty).create();
            this.communityIndex = g.schema().indexFor(communityLabel).on(communityProperty).create();
            this.tmpNewLayerIndex = g.schema().indexFor(newLayerLabel).on(communityProperty).create();
            tx.success();
        }

        try (Transaction tx = g.beginTx()) {
            Schema schema = g.schema();
            schema.awaitIndexOnline(layerIndex, 10, TimeUnit.SECONDS);
            schema.awaitIndexOnline(communityIndex, 10, TimeUnit.SECONDS);
            schema.awaitIndexOnline(tmpNewLayerIndex, 10, TimeUnit.SECONDS);
            tx.success();
        }

        try (Transaction tx = g.beginTx()) {
            for (Node n : GlobalGraphOperations.at(g).getAllNodes()) {
                n.addLabel(this.layerLabel);
                n.setProperty(layerProperty, layerCount);
                n.addLabel(this.communityLabel);
                n.setProperty(communityProperty, n.getId());
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

    public void execute() {
        int totMacroNodes;

        do {
            totMacroNodes = this.pass();
        } while (totMacroNodes != 1);
    }

    public int pass() {
        logger.info("----------");
        logger.info("Starting pass " + passCount);
        logger.info("----------");

        logger.info("********** Start Moving phase");
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
        int totMacroNodes;
        try (Transaction tx = g.beginTx()) {
            totMacroNodes = this.secondPhase();
            GraphUtils.print(g);
            tx.success();
        }
        passCount++;

        return totMacroNodes;
    }

    public void firstPhase() {
        int movements;

        do {
            movements = 0;
            // itera solo per i nodi del livello corrente
            ResourceIterator<Node> nodes = g.findNodes(layerLabel, layerProperty, layerCount);
            while (nodes.hasNext()) {
                Node src = nodes.next();
//                logger.info("Src: " + src);
                long srcCommunity = (long) src.getProperty(communityProperty);
                long bestCommunity = srcCommunity;
                double bestDelta = 0.0;

                Iterable<Relationship> rels = layerCount == 0 ? src.getRelationships(Direction.BOTH) : src.getRelationships(Direction.BOTH, LouvainRels.NewEdges);
                for (Relationship r : rels) {
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
        return r.hasProperty(weightProperty) ? (double) r.getProperty(weightProperty) : 1.0;
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
        ResourceIterator<Node> members = g.findNodes(communityLabel, communityProperty, cId);
        while (members.hasNext()) {
            Node member = members.next();
            if (!member.equals(n))
                vol += nodeVolume(member);
        }
        return vol;
    }

    private double communityVolume(String cId) {
        double vol = 0;

        ResourceIterator<Node> members = g.findNodes(communityLabel, communityProperty, cId);
        while (members.hasNext()) {
            Node member = members.next();
            vol += nodeVolume(member);
        }
        return vol;
    }

    public int secondPhase() {
        int totMacroNodes = 0;

        // Prendi tutti i nodi del livello corrente
        ResourceIterator<Node> activeNodes = g.findNodes(layerLabel, layerProperty, layerCount);
        while (activeNodes.hasNext()) {
            Node activeNode = activeNodes.next();
            logger.info("activeNode is " + activeNode.getId());
            long cId = (long) activeNode.getProperty(communityProperty);

            // Prendi il macronode associato a questa community
            Node macroNode = g.findNode(newLayerLabel, communityProperty, cId);
            if (macroNode == null) {    // Se non esiste, crealo
                logger.info("Creating macronode for community " + cId);
                totMacroNodes++;
                macroNode = g.createNode(newLayerLabel);
                macroNode.setProperty(communityProperty, cId);
                macroNode.setProperty(layerProperty, layerCount + 1); // e' il nuovo layer
            }

            // Create a relationship to the original node
            activeNode.createRelationshipTo(macroNode, LouvainRels.Layer);
            logger.info("Created -[layer] relationship between " + activeNode.getId() + " to " + macroNode.getId());
            GraphUtils.print(g);

            // Now you must connect the macronodes (if any)!
//            for (Relationship r : activeNode.getRelationships()) {
//                if (!r.isType(LouvainRels.Layer)) {
//                    Node neigh = r.getOtherNode(activeNode);
//                    logger.info("Neigh node is " + neigh.getId());
//                    long neighCid = (long) neigh.getProperty(communityProperty);
//                    logger.info("Looking for neighCid: " + neighCid);
//                    Node otherMacroNode = g.findNode(newLayerLabel, communityProperty, neighCid);
//                    if (otherMacroNode == null) {
//                        logger.info("Creating ANOTHER macroNode for community " + neighCid);
//                        totMacroNodes++;
//                        otherMacroNode = g.createNode(newLayerLabel);
//                        otherMacroNode.setProperty(communityProperty, neighCid);
//                        otherMacroNode.setProperty(layerProperty, layerCount + 1);
//                    }
//                    if (!otherMacroNode.equals(macroNode)) {
//
//                        otherMacroNode.createRelationshipTo(macroNode, LouvainRels.NewEdges);
//                        macroNode.createRelationshipTo(otherMacroNode, LouvainRels.NewEdges);
//                    }
//                }
//            }
            activeNode.removeLabel(layerLabel);
            activeNode.removeLabel(communityLabel);
        }

        ResourceIterator<Node> macroNodes = g.findNodes(newLayerLabel);
        while (macroNodes.hasNext()) {
            Node macroNode = macroNodes.next();
//            logger.info("MacroNode: "+macroNode);

            for (Relationship layer : macroNode.getRelationships(Direction.INCOMING, LouvainRels.Layer)) {
                Node originalNode = layer.getOtherNode(macroNode);
//                logger.info("OriginalNode: "+originalNode);

                for (Relationship r : originalNode.getRelationships()) {
                    if (!r.isType(LouvainRels.Layer)) {
                        Node neigh = r.getOtherNode(originalNode);
//                        logger.info("Neigh: " + neigh);
                        Node otherMacroNode = neigh.getSingleRelationship(LouvainRels.Layer, Direction.OUTGOING).getOtherNode(neigh);

//                        logger.info("OtherMacroNode: " + otherMacroNode.toString());
                        Relationship macroRel = getRelationshipBetween(macroNode, otherMacroNode, Direction.BOTH, LouvainRels.NewEdges);
                        if (macroRel == null) {
                            macroRel = macroNode.createRelationshipTo(otherMacroNode, LouvainRels.NewEdges);
                            macroRel.setProperty(weightProperty, 0.0);
                        }
                        double w = (double) macroRel.getProperty(weightProperty);
                        macroRel.setProperty(weightProperty, w + 1.0);

                    }
                }
            }
        }

        if (true)
            throw new RuntimeException();

        ResourceIterator<Node> macros = g.findNodes(newLayerLabel);
        while (macros.hasNext()) {
            Node next = macros.next();
            next.removeLabel(newLayerLabel);
            next.addLabel(communityLabel);
            next.addLabel(layerLabel);
        }

        layerCount++;
        return totMacroNodes;
    }

    private boolean areRelated(Node n1, Node n2) {
        return this.getRelationshipBetween(n1, n2, Direction.BOTH) != null;
    }

    private Relationship getRelationshipBetween(Node n1, Node n2, Direction dir, RelationshipType... relTypes) {
        for (Relationship rel : n1.getRelationships(dir, relTypes)) {
            if (rel.getOtherNode(n1).equals(n2)) return rel;
        }
        return null;
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
