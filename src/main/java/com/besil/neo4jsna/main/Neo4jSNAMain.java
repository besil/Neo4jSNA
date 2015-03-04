package com.besil.neo4jsna.main;

import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.helpers.collection.IteratorUtil;
import org.neo4j.tooling.GlobalGraphOperations;

import com.besil.neo4jsna.algorithms.ConnectedComponents;

public class Neo4jSNAMain {
	public static void main(String[] args) {
		String path = "data/tmp/cineasts_12k_movies_50k_actors.db";
		long nodeCount, relsCount;
		
		GraphDatabaseService g = new GraphDatabaseFactory().newEmbeddedDatabase(path);
//		GraphDatabaseService g = BatchInserters.batchDatabase(path);
		try (Transaction tx = g.beginTx() ) {
			nodeCount = IteratorUtil.count( GlobalGraphOperations.at(g).getAllNodes() );
			relsCount = IteratorUtil.count( GlobalGraphOperations.at(g).getAllRelationships() );
			tx.success();
		}
		
		System.out.println(nodeCount);
		System.out.println(relsCount);
	
		
		ConnectedComponents cc = new ConnectedComponents(g);
		cc.execute(10);
		
		Long2LongMap components = cc.getResult();
		LongSet s = new LongOpenHashSet( components.values() );
		System.out.println(s.size());
		
		g.shutdown();
	}
}
