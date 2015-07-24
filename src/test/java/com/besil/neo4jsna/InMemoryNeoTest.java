package com.besil.neo4jsna;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.test.TestGraphDatabaseFactory;

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

    protected abstract void initGraph();

    @Before
    public void setUp() {
        tx = db.beginTx();
        this.initGraph();
    }

    @After
    public void tearDown() {
        tx.close();
    }


}
