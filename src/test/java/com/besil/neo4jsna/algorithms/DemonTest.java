package com.besil.neo4jsna.algorithms;

import com.besil.neo4jsna.InMemoryNeoTest;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.junit.Assert;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Created by besil on 26/05/15.
 */
public class DemonTest extends InMemoryNeoTest {
    @Test
    public void egoNetwork() {
        IntSet neighs = new IntOpenHashSet();
        IntStream.range(0, 6).forEach(n -> neighs.add(n));

        Demon demon = new Demon(db);
        Node root = nodes.get(0);
        List<Relationship> egoNetwork = demon.getEgoNetwork(root);
        Assert.assertEquals(7, egoNetwork.size());

    }

}