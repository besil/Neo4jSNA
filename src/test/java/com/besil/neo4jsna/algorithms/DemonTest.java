package com.besil.neo4jsna.algorithms;

import com.besil.neo4jsna.InMemoryNeoTest;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.junit.Assert;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.helpers.collection.IteratorUtil;

import java.util.Iterator;
import java.util.stream.LongStream;

/**
 * Created by besil on 26/05/15.
 */
public class DemonTest extends InMemoryNeoTest {
    @Test
    public void egoNetwork() {
        LongSet neighs = new LongOpenHashSet();
        LongStream.range(0, 6).forEach(n -> neighs.add(n));

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

    @Test
    public void egoMinusEgo() {
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