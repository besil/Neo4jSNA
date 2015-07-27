package com.besil.neo4jsna.algorithms.louvain;

import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;

/**
 * Created by besil on 7/24/15.
 */
public class LouvainLayer {
    private final int layer;
    private final Long2LongMap node2CommunityMap;
    private double modularity;

    protected LouvainLayer(int _layer) {
        this.layer = _layer;
        this.node2CommunityMap = new Long2LongOpenHashMap();
    }

    public int size() {
        return node2CommunityMap.size();
    }

    public void add(long nodeId, long cId) {
        this.node2CommunityMap.put(nodeId, cId);
    }

    public void modularity(double _modularity) {
        this.modularity = _modularity;
    }

    public double modularity() {
        return this.modularity;
    }

    public Long2LongMap getNode2CommunityMap() {
        return node2CommunityMap;
    }
}
