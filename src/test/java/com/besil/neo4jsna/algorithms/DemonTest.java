package com.besil.neo4jsna.algorithms;

import com.besil.neo4jsna.InMemoryNeoTest;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.junit.Assert;
import org.junit.Test;
import org.neo4j.graphdb.*;
import org.neo4j.helpers.collection.IteratorUtil;

import java.util.Iterator;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

/**
 * Created by besil on 26/05/15.
 */
public class DemonTest extends InMemoryNeoTest {
    @Test
    public void egoNetwork() {
        LongSet neighs = new LongOpenHashSet();
        LongStream.range(0, 6).forEach(neighs::add);

        try (Transaction tx = db.beginTx()) {
            Demon demon = new Demon(db);
            Node root = nodes.get(0);
            demon.setEgoNetwork(root);

            long demonNodes = 0;
            long relCount = 0;

            ResourceIterator<Node> egoNodes = demon.getDemonNodes();
            Iterator<Relationship> egoRelationships = demon.getDemonRelationships();

            while (egoNodes.hasNext()) {
                Node n = egoNodes.next();
                Assert.assertTrue(neighs.contains(n.getId()));
                demonNodes++;
            }
            Assert.assertEquals(6, demonNodes);

            relCount = IteratorUtil.asCollection(egoRelationships).size();
            Assert.assertEquals(7, relCount);

            demon.clearEgoNetwork(root);

            egoNodes = demon.getDemonNodes();
            egoRelationships = demon.getDemonRelationships();
            demonNodes = 0;
            relCount = 0;

            while (egoNodes.hasNext()) {
                Node n = egoNodes.next();
                Assert.assertTrue(neighs.contains(n.getId()));
                demonNodes++;
            }
            Assert.assertEquals(0, demonNodes);
            relCount = IteratorUtil.asCollection(egoRelationships).size();
            Assert.assertEquals(0, relCount);
        }
    }

    @Test
    public void egoMinusEgo() {
        try (Transaction tx = db.beginTx()) {

            Demon demon = new Demon(db);
            Node root = nodes.get(0);

            demon.executeEgoMinusEgo(root);

            ResourceIterator<Node> demonNodes = demon.getDemonNodes();
            Iterator<Relationship> demonRelationships = demon.getDemonRelationships();

            int neighCount = 0;
            while (demonNodes.hasNext()) {
                demonNodes.next();
                neighCount++;
            }
            Assert.assertEquals(5, neighCount);

            int relCount = IteratorUtil.asList(demonRelationships).size();
            Assert.assertEquals(2, relCount);

            demon.clearEgoNetwork(root);
        }
    }

    protected void initGraph() {
        this.nodes = new Int2ObjectOpenHashMap<>();
        RelationshipType knows = CommonsRelationshipTypes.KNOWS;

        IntStream.range(0, 6).forEach(n -> nodes.put(n, this.createNode(n)));

        nodes.get(0).createRelationshipTo(nodes.get(1), knows);
        nodes.get(0).createRelationshipTo(nodes.get(2), knows);
        nodes.get(0).createRelationshipTo(nodes.get(3), knows);
        nodes.get(0).createRelationshipTo(nodes.get(4), knows);
        nodes.get(0).createRelationshipTo(nodes.get(5), knows);

        nodes.get(1).createRelationshipTo(nodes.get(2), knows);
        nodes.get(3).createRelationshipTo(nodes.get(4), knows);

        IntStream.range(6, 11).forEach(n -> nodes.put(n, this.createNode(n)));

        nodes.get(3).createRelationshipTo(nodes.get(6), knows);
        nodes.get(4).createRelationshipTo(nodes.get(7), knows);
        nodes.get(5).createRelationshipTo(nodes.get(8), knows);

        nodes.get(4).createRelationshipTo(nodes.get(9), knows);
        nodes.get(4).createRelationshipTo(nodes.get(10), knows);

        nodes.get(9).createRelationshipTo(nodes.get(10), knows);

    }

    private Node createNode(int id) {
        Node n = db.createNode();
        n.setProperty("id", id);
        return n;
    }

}
