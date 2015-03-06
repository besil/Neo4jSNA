package com.besil.neo4jsna.main;

import java.util.Optional;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.helpers.collection.IteratorUtil;
import org.neo4j.tooling.GlobalGraphOperations;

import com.besil.neo4jsna.algorithms.PageRank;
import com.besil.neo4jsna.computer.GraphComputer;

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
		
		System.out.println(nodeCount);
		System.out.println(relsCount);
	
		
		PageRank pr = new PageRank(g);
		GraphComputer computer = new GraphComputer(g);
		
		computer.execute(pr);
		
		Long2DoubleMap ranks = pr.getResult();
		Optional<Double> res = ranks.values().parallelStream().reduce( (x, y) -> x + y );
		System.out.println(res.get());
		
		g.shutdown();
	}
}
