package com.besil.neo4jsna.algorithms.louvain;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

/**
 * Created by besil on 7/24/15.
 */
public class LouvainResult {
    private Int2ObjectMap<LouvainLayer> layerMap;

    public LouvainResult() {
        this.layerMap = new Int2ObjectOpenHashMap<>();
    }

    public LouvainLayer layer(int level) {
        if (!layerMap.containsKey(level)) {
            this.layerMap.put(level, new LouvainLayer(level));
        }
        return this.layerMap.get(level);
    }

}
