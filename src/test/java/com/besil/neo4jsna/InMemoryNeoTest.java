package com.besil.neo4jsna;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.util.stream.IntStream;

/**
 * Created by besil on 27/05/15.
 */
public abstract class InMemoryNeoTest {
    protected static GraphDatabaseService db;
    protected Transaction tx;
    protected Int2ObjectMap<Node> nodes;

    @BeforeClass
    public static void setUpClass() {
        db = new TestGraphDatabaseFactory().newImpermanentDatabase();
    }

    private Node createNode(int id) {
        Node n = db.createNode();
        n.setProperty("id", id);
        return n;
    }

    private void initGraph() {
        this.nodes = new Int2ObjectOpenHashMap<>();
        RelationshipType knows = MyRelationshipTypes.KNOWS;

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

    @Before
    public void setUp() {
        tx = db.beginTx();
        this.initGraph();
    }

    @After
    public void tearDown() {
        tx.close();
    }

    enum MyRelationshipTypes implements RelationshipType {
        KNOWS
    }
}
