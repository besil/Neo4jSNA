package com.besil.neo4jsna.algorithms;

import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;

import java.util.Map.Entry;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import com.besil.neo4jsna.engine.VertexAlgorithm;

public class LabelPropagation implements VertexAlgorithm {
	protected Long2LongMap communityMap;
	protected final String attName = "community";
	
	public LabelPropagation() {
		this.communityMap = new Long2LongOpenHashMap();
	}
	
	@Override
	public void init(Node node) {
		node.setProperty(attName, node.getId());
	}

	@Override
	public void apply(Node node) {
		long mostFrequentLabel = this.getMostFrequentLabel(node);
		node.setProperty(attName, mostFrequentLabel);
	}
	
	protected long getMostFrequentLabel(Node node) {
		Long2LongMap commMap = new Long2LongOpenHashMap();
		for( Relationship r : node.getRelationships() ) {
			Node other = r.getOtherNode(node);
			long otherCommunity = (long) other.getProperty(attName);
			commMap.put(other.getId(), otherCommunity);
		}
		
		long mostFrequentLabel = -1;
		long mostFrequentLabelCount = -1;
		for( Entry<Long, Long> e : commMap.entrySet() ) {
			if( e.getValue() > mostFrequentLabelCount ) {
				mostFrequentLabelCount = e.getValue();
				mostFrequentLabel = e.getKey();
			}
		}
		return mostFrequentLabel;
	}

	@Override
	public void collectResult(Node node) {
		this.communityMap.put(node.getId(), (long) node.getProperty(attName));
	}

	@Override
	public int getMaxIterations() {
		return 20;
	}

	@Override
	public String getName() {
		return "Label Propagation CD";
	}

	@Override
	public Long2LongMap getResult() {
		return communityMap;
	}

}
