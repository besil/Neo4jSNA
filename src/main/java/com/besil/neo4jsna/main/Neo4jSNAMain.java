package com.besil.neo4jsna.main;

import java.util.Map.Entry;

import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.helpers.collection.IteratorUtil;
import org.neo4j.tooling.GlobalGraphOperations;

import com.besil.neo4jsna.algorithms.ConnectedComponents;
import com.besil.neo4jsna.computer.GraphComputer;

public class Neo4jSNAMain {
	public static void main(String[] args) {
		@SuppressWarnings("unused")
		String drwho = "data/tmp/drwho";
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
	
		
		ConnectedComponents cc = new ConnectedComponents();
		GraphComputer computer = new GraphComputer(g);
		
		
		computer.execute(cc);
		
		Long2LongMap components = cc.getResult();
		for(Entry<Long, Long> e : components.entrySet()) {
			System.out.println(e.getKey()+" -> "+e.getValue());
		}
		LongSet s = new LongOpenHashSet( components.values() );
		
		System.out.println(s.size());
		
		g.shutdown();
	}
}
