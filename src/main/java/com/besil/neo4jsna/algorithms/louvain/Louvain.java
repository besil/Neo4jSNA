package com.besil.neo4jsna.algorithms.louvain;

import com.besil.neo4jsna.engine.GraphAlgoEngine;
import com.besil.neo4jsna.measures.UndirectedModularity;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.schema.Schema;
import org.neo4j.tooling.GlobalGraphOperations;

import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by besil on 7/24/15.
 */
public class Louvain {
    private final Logger logger = Logger.getLogger(Louvain.class.getName());
    private final String communityProperty = "community", layerProperty = "layer", weightProperty = "weight";
    private final GraphDatabaseService g;
    private final double totalEdgeWeight;
    private final LouvainResult louvainResult;
    private final int batchSize = 100_000;
    private Label layerLabel, communityLabel, newLayerLabel;
    private IndexDefinition layerIndex, communityIndex, tmpNewLayerIndex;
    private int layerCount = 0;
    private int macroNodeCount = 0;

    public Louvain(GraphDatabaseService g) {
        for (Handler h : Logger.getLogger("").getHandlers())
            h.setLevel(Level.INFO);

        this.louvainResult = new LouvainResult();
        this.g = g;
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
    }

    public void execute() {
        do {
            logger.info("Layer count: " + layerCount);
            macroNodeCount = this.pass();
        } while (macroNodeCount != 0);
    }

    public int pass() {
        try (Transaction tx = g.beginTx()) {
            this.firstPhase();
            tx.success();
        }

        logger.info("Starting modularity...");
        GraphAlgoEngine engine = new GraphAlgoEngine(g);
        UndirectedModularity um = new UndirectedModularity(g);
        engine.execute(um);

        int totMacroNodes;
        totMacroNodes = this.secondPhase();
        logger.info("Created " + totMacroNodes);

        layerCount++;
        return totMacroNodes;
    }

    public void firstPhase() {
        int movements;
        int counterOps = 0;

        Transaction tx = g.beginTx();

        do {
            int count = 0;
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
                    if (!src.equals(neigh)) {
                        long neighCommunity = (long) neigh.getProperty(communityProperty);

                        double delta = this.calculateDelta(src, srcCommunity, neighCommunity);
//                    logger.info("    Dst: " + neigh + " -> " + delta);

                        if (delta > bestDelta) {
                            bestDelta = delta;
                            bestCommunity = neighCommunity;
                        }
                    }
                }

                if (srcCommunity != bestCommunity) {
                    src.setProperty(communityProperty, bestCommunity);
                    tx = this.batchCommit(++counterOps, tx, g);
                    movements++;
                }
            }
            logger.info("Movements so far: " + movements);
        } while (movements != 0);

        tx.success();
        tx.close();
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

//    private double communityWeight(Node n, String cId) {
//        double weight = 0.0;
//        for (Relationship r : n.getRelationships(Direction.BOTH)) {
//            if (r.getOtherNode(n).getProperty(communityProperty).equals(cId))
//                weight += this.weight(r);
//        }
//        return weight;
//    }

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

//    private double communityVolume(String cId) {
//        double vol = 0;
//        ResourceIterator<Node> members = g.findNodes(communityLabel, communityProperty, cId);
//        while (members.hasNext()) {
//            Node member = members.next();
//            vol += nodeVolume(member);
//        }
//        return vol;
//    }

    public int secondPhase() {
        int totMacroNodes = 0;
        long counterOps = 0;

        Transaction tx = g.beginTx();

        // Check if a new layer must be created
        LongSet macroNodesCommunities = new LongOpenHashSet();
        ResourceIterator<Node> checkNodes = g.findNodes(layerLabel, layerProperty, layerCount);
        while (checkNodes.hasNext()) {
            Node n = checkNodes.next();
            macroNodesCommunities.add((long) n.getProperty(communityProperty));
        }

        if (macroNodesCommunities.size() == macroNodeCount) {
            // Nothing to move: save to layer object and exit
            LouvainLayer louvainLayer = louvainResult.layer(layerCount);
            ResourceIterator<Node> activeNodes = g.findNodes(layerLabel, layerProperty, layerCount);
            while (activeNodes.hasNext()) {
                Node activeNode = activeNodes.next();
                long activeNodeId = activeNode.hasProperty("id") ? (int) activeNode.getProperty("id") : activeNode.getId();
                long cId = (long) activeNode.getProperty(communityProperty);

                louvainLayer.add(activeNodeId, cId);
            }

            return totMacroNodes;
        }

        int count = 0;
        LouvainLayer louvainLayer = louvainResult.layer(layerCount);
        // Get all nodes of current layer
        ResourceIterator<Node> activeNodes = g.findNodes(layerLabel, layerProperty, layerCount);
        while (activeNodes.hasNext()) {
            if (++count % 1000 == 0)
                logger.info("Computed " + count + " nodes");
            Node activeNode = activeNodes.next();
            long activeNodeId = activeNode.hasProperty("id") ? Long.parseLong((String) activeNode.getProperty("id")) : activeNode.getId();
            long cId = (long) activeNode.getProperty(communityProperty);

            louvainLayer.add(activeNodeId, cId);

            // Prendi il macronode associato a questa community
            Node macroNode = g.findNode(newLayerLabel, communityProperty, cId);
            if (macroNode == null) {    // Se non esiste, crealo
                totMacroNodes++;
                macroNode = g.createNode(newLayerLabel);
                macroNode.setProperty(communityProperty, cId);
                macroNode.setProperty(layerProperty, layerCount + 1); // e' il nuovo layer

                tx = this.batchCommit(++counterOps, tx, g);
            }

            // Create a relationship to the original node
            activeNode.createRelationshipTo(macroNode, LouvainRels.Layer);
            tx = this.batchCommit(++counterOps, tx, g);

            activeNode.removeLabel(layerLabel);
            tx = this.batchCommit(++counterOps, tx, g);
            activeNode.removeLabel(communityLabel);
            tx = this.batchCommit(++counterOps, tx, g);
        }


        ResourceIterator<Node> macroNodes = g.findNodes(newLayerLabel);
        while (macroNodes.hasNext()) {
            Node macroNode = macroNodes.next();

            for (Relationship layer : macroNode.getRelationships(Direction.INCOMING, LouvainRels.Layer)) {
                Node originalNode = layer.getOtherNode(macroNode);

                for (Relationship r : originalNode.getRelationships(Direction.BOTH)) {
                    if (!r.isType(LouvainRels.Layer)) {
                        Node neigh = r.getOtherNode(originalNode);
                        Node otherMacroNode = neigh.getSingleRelationship(LouvainRels.Layer, Direction.OUTGOING).getOtherNode(neigh);

                        Relationship macroRel = getRelationshipBetween(macroNode, otherMacroNode, Direction.BOTH, LouvainRels.NewEdges);
                        if (macroRel == null) {
                            macroRel = macroNode.createRelationshipTo(otherMacroNode, LouvainRels.NewEdges);
                            tx = this.batchCommit(++counterOps, tx, g);
                            macroRel.setProperty(weightProperty, 0.0);
                            tx = this.batchCommit(++counterOps, tx, g);
                        }
                        double w = (double) macroRel.getProperty(weightProperty);
                        macroRel.setProperty(weightProperty, w + 1.0);
                        tx = this.batchCommit(++counterOps, tx, g);
                    }
                }
            }
        }

        ResourceIterator<Node> macros = g.findNodes(newLayerLabel);
        while (macros.hasNext()) {
            Node next = macros.next();
            next.removeLabel(newLayerLabel);
            tx = this.batchCommit(++counterOps, tx, g);
            next.addLabel(communityLabel);
            tx = this.batchCommit(++counterOps, tx, g);
            next.addLabel(layerLabel);
            tx = this.batchCommit(++counterOps, tx, g);
        }

        tx.success();
        tx.close();

        return totMacroNodes;
    }

    private Transaction batchCommit(long counterOps, Transaction tx, GraphDatabaseService g) {
        if (++counterOps % batchSize == 0) {
            logger.info("Committing...");
            tx.success();
            tx.close();
            tx = g.beginTx();
        }
        return tx;
    }

    private Relationship getRelationshipBetween(Node n1, Node n2, Direction dir, RelationshipType... relTypes) {
        for (Relationship rel : n1.getRelationships(dir, relTypes)) {
            if (rel.getOtherNode(n1).equals(n2)) return rel;
        }
        return null;
    }

    public LouvainResult getResult() {
        return this.louvainResult;
    }

    public void clean() {
        try (Transaction tx = g.beginTx()) {
            for (Node n : GlobalGraphOperations.at(g).getAllNodes()) {
                n.removeProperty(communityProperty);
            }
            tx.success();
        }
    }

    enum LouvainRels implements RelationshipType {
        Layer, NewEdges
    }
}
