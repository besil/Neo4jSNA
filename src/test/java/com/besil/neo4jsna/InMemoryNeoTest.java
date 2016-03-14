package com.besil.neo4jsna;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.test.TestGraphDatabaseFactory;

/**
 * Created by besil on 27/05/15.
 */
public abstract class InMemoryNeoTest {
    protected GraphDatabaseService db;
    protected Int2ObjectMap<Node> nodes;

    protected abstract void initGraph();

    @Before
    public void setUp() {
        db = new TestGraphDatabaseFactory().newImpermanentDatabase();
        try (Transaction tx = db.beginTx()) {
            this.initGraph();
            tx.success();
        }
    }

    @After
    public void tearDown() {
        db.shutdown();
    }

    protected enum CommonsRelationshipTypes implements RelationshipType {
        KNOWS
    }

}
