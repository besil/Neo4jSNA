package com.besil.neo4jsna.algorithms;

import org.neo4j.graphdb.Direction;

public class StronglyConnectedComponents extends ConnectedComponents {
	public StronglyConnectedComponents() {
		super();
		this.direction = Direction.INCOMING;
	}
	
	@Override
	public String getName() {
		return "Strongly Connected Components";
	}
}
