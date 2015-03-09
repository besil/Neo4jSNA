package com.besil.neo4jsna.algorithms;

import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;

import java.util.Map;

import org.neo4j.cypher.javacompat.ExecutionResult;

import com.besil.neo4jsna.engine.algorithm.CypherAlgorithm;

public class TriangleCount implements CypherAlgorithm {
	protected Long2LongMap triangleMap;
	
	public TriangleCount() {
		this.triangleMap = new Long2LongOpenHashMap();
	}
	
	@Override
	public String getName() {
		return "Triangle Count";
	}

	@Override
	public String getQuery() {
		return 	"MATCH p = (a) -- (b) -- (c), (c) -- (a) "+
				"RETURN id(a) as nodeid, count(p) as triangleCount";
	}

	@Override
	public void collectResult(ExecutionResult result) {
		System.out.println(result.toString());
		
		for(Map<String, Object> row : result) {
			long nodeid = (long) row.get("nodeid");
			long triangleCount = (long) row.get("triangleCount");
			this.triangleMap.put(nodeid, triangleCount);
		}
		
//		while( nodes.hasNext() && triangleCount.hasNext()) {
//			this.triangleMap.put(nodes.next(), triangleCount.next());
//		}
	}

	@Override
	public Long2LongMap getResult() {
		return this.triangleMap;
	}

}
