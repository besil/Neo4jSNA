package com.besil.neo4jsna.algorithms;

import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import com.besil.neo4jsna.computer.VertexAlgorithm;

public class ConnectedComponents implements VertexAlgorithm {
	protected Long2LongMap componentsMap;
	protected String attName = "ConnectedComponents";

	public ConnectedComponents() {
		this.componentsMap = new Long2LongOpenHashMap();
	}
	
	@Override
	public void init(Node node) {
		node.setProperty(attName, node.getId());
	}
	@Override
	public void apply(Node node) {
		long newComponent = this.getLowestComponent(node);
		node.setProperty(attName, newComponent);
	}
	
	protected long getLowestComponent(Node n) {
		long minComponent = (long) n.getProperty(attName);

		for(Relationship r : n.getRelationships()) {
			long otherComponent = (long) r.getOtherNode(n).getProperty(attName);
			minComponent = minComponent < otherComponent ? minComponent : otherComponent;
		}
		return minComponent;
	}
	
	@Override
	public int getMaxIterations() {
		return 20;
	}

	@Override
	public String getName() {
		return "Connected Components";
	}
	
	@Override
	public Object collectResult(Node node) {
		return this.componentsMap.put(node.getId(), (long) node.getProperty(this.attName)); 
	}

	public Long2LongMap getResult() {
		return this.componentsMap;
	}
}
