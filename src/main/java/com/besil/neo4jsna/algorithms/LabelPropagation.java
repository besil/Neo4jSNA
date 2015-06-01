package com.besil.neo4jsna.algorithms;

import com.besil.neo4jsna.engine.algorithm.VertexAlgorithm;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

import java.util.Map.Entry;

public class LabelPropagation implements VertexAlgorithm {
    protected final String attName = "community";
    protected final RelationshipType relType;
    protected Long2LongMap communityMap;

    public LabelPropagation() {
        this(null);
    }

    public LabelPropagation(RelationshipType relType) {
        this.communityMap = new Long2LongOpenHashMap();
        this.relType = relType;
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
        Iterable<Relationship> relationships = relType == null ? node.getRelationships() : node.getRelationships(relType);

        for (Relationship r : relationships) {
            Node other = r.getOtherNode(node);
			long otherCommunity = (long) other.getProperty(attName);
			// commMap.put(other.getId(), otherCommunity);	WRONG
			long count = commMap.getOrDefault(otherCommunity, 0L);
			commMap.put(otherCommunity, count+1);
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

	@Override
	public String getAttributeName() {
		return attName;
	}

}
