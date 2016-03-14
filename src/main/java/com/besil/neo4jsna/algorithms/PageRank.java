package com.besil.neo4jsna.algorithms;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.tooling.GlobalGraphOperations;

import com.besil.neo4jsna.engine.algorithm.VertexAlgorithm;

public class PageRank implements VertexAlgorithm<Long2DoubleMap> {
	protected final String attName = "PageRank";
	protected Long2DoubleMap rankMap;
	protected long nodeCount = 0;
	protected double dampingFactor = 0.85;
	protected double firstMember;
	
	public PageRank(GraphDatabaseService g) {
		rankMap = new Long2DoubleOpenHashMap();
		try(Transaction tx = g.beginTx()) {
			for(@SuppressWarnings("unused") Node n : GlobalGraphOperations.at(g).getAllNodes())
				nodeCount += 1;
			tx.success();
		}
		this.firstMember = ( 1.0 - this.dampingFactor ) / this.nodeCount;
	}
	
	@Override
	public void init(Node node) {
		node.setProperty(attName, 1.0 / this.nodeCount);
	}

	@Override
	public void apply(Node node) {
		double secondMember = 0.0;
		for( Relationship rin : node.getRelationships() ) {
			Node neigh = rin.getOtherNode(node);
			
			double neighRank = (double) neigh.getProperty(attName);
			secondMember += neighRank / neigh.getDegree();
		}
		
		secondMember *= this.dampingFactor;
		node.setProperty(attName, firstMember + secondMember);
	}

	@Override
	public void collectResult(Node node) {
		rankMap.put(node.getId(), (double) node.getProperty(attName));
	}

	@Override
	public int getMaxIterations() {
		return 40;
	}

	@Override
	public String getName() {
		return "PageRank";
	}

	@Override
	public Long2DoubleMap getResult() {
		return rankMap;
	}

	@Override
	public String getAttributeName() {
		return attName;
	}

}
