package com.besil.neo4jsna.main;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import java.util.Optional;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.helpers.collection.IteratorUtil;
import org.neo4j.tooling.GlobalGraphOperations;

import com.besil.neo4jsna.algorithms.ConnectedComponents;
import com.besil.neo4jsna.algorithms.PageRank;
import com.besil.neo4jsna.computer.GraphEngine;

public class Neo4jSNAMain {
	public static void main(String[] args) {
		String cinea = "data/tmp/cineasts";
		
		String path = cinea;
		long nodeCount, relsCount;
		
		GraphDatabaseService g = new GraphDatabaseFactory().newEmbeddedDatabase(path);
		try (Transaction tx = g.beginTx() ) {
			nodeCount = IteratorUtil.count( GlobalGraphOperations.at(g).getAllNodes() );
			relsCount = IteratorUtil.count( GlobalGraphOperations.at(g).getAllRelationships() );
			tx.success();
		}
		
		System.out.println("Node count: "+nodeCount);
		System.out.println("Rel count: "+relsCount);
	
		GraphEngine engine = new GraphEngine(g);
		
		System.out.println("PageRank");
		PageRank pr = new PageRank(g);
		engine.execute(pr);
		Long2DoubleMap ranks = pr.getResult();
		Optional<Double> res = ranks.values().parallelStream().reduce( (x, y) -> x + y );
		System.out.println("Check PageRank sum is 1.0: "+ res.get());

		System.out.println("Connected Components");
		ConnectedComponents cc = new ConnectedComponents();
		engine.execute(cc);
		Long2LongMap components = cc.getResult();
		int totalComponents = new LongOpenHashSet( components.values() ).size();
		System.out.println("There are "+ totalComponents+ " different components");
		
		g.shutdown();
	}
}
