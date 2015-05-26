package com.besil.neo4jsna.algorithms;

import com.besil.neo4jsna.engine.algorithm.CypherAlgorithm;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import org.neo4j.graphdb.Result;

import java.util.Map;

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
        return "MATCH p = (a) -- (b) -- (c), (c) -- (a) " +
                "RETURN id(a) as nodeid, count(p) as triangleCount";
    }

    @Override
    public void collectResult(Result result) {
        while (result.hasNext()) {
            Map<String, Object> row = result.next();
            long nodeid = (long) row.get("nodeid");
            long triangleCount = (long) row.get("triangleCount");
            this.triangleMap.put(nodeid, triangleCount);
        }
    }

    @Override
    public Long2LongMap getResult() {
        return this.triangleMap;
    }

}
