package com.besil.neo4jsna.algorithms;

import com.besil.neo4jsna.InMemoryNeoTest;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.junit.Assert;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.util.stream.IntStream;

/**
 * Created by besil on 26/05/15.
 */
public class DemonTest extends InMemoryNeoTest {
    @Test
    public void egoNetwork() {
//        GlobalGraphOperations.at(db).getAllNodes().forEach(System.out::println);
//        GlobalGraphOperations.at(db).getAllRelationships().forEach(r -> System.out.println(r.getStartNode() + " -- " + r.getEndNode()));
        IntSet neighs = new IntOpenHashSet();
        IntStream.range(0, 6).forEach(n -> neighs.add(n));

        Demon demon = new Demon(db);
        Iterable<Relationship> egoNetwork = demon.getEgoNetwork(nodes.get(0));
        egoNetwork.forEach(r -> {
            Node neigh = r.getOtherNode(nodes.get(0));
            int id = (int) r.getOtherNode(nodes.get(0)).getProperty("id");
            Assert.assertTrue(neighs.contains(id));
        });

    }

}