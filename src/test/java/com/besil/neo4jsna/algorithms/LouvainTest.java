package com.besil.neo4jsna.algorithms;

import com.besil.neo4jsna.InMemoryNeoTest;
import com.besil.neo4jsna.algorithms.louvain.Louvain;
import com.besil.neo4jsna.algorithms.louvain.LouvainLayer;
import com.besil.neo4jsna.algorithms.louvain.LouvainResult;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.junit.Assert;
import org.junit.Test;
import org.neo4j.graphdb.Node;

/**
 * Created by besil on 7/24/15.
 */
public class LouvainTest extends InMemoryNeoTest {
    @Override
    protected void initGraph() {
        Int2ObjectMap<Node> nodes = new Int2ObjectOpenHashMap<>();

        for (int i = 0; i < 9; i++) {
            Node n = db.createNode();
            n.setProperty("id", i);
            nodes.put(i, n);
        }

        for (int i = 0; i < 9; i++) {
            Node src = nodes.get(i);
            Node dst = (i + 1) % 3 != 0 ? nodes.get(i + 1) : nodes.get(i - 2);

            src.createRelationshipTo(dst, CommonsRelationshipTypes.KNOWS);
//            dst.createRelationshipTo(src, CommonsRelationshipTypes.KNOWS);
        }

        nodes.get(0).createRelationshipTo(nodes.get(3), CommonsRelationshipTypes.KNOWS);
        nodes.get(3).createRelationshipTo(nodes.get(6), CommonsRelationshipTypes.KNOWS);
        nodes.get(6).createRelationshipTo(nodes.get(0), CommonsRelationshipTypes.KNOWS);

//        for (int i = 0; i < 9; i += 3) {
//            Node src = nodes.get(i);
//            Node dst1 = nodes.get((i + 3) % 9);
//            Node dst2 = nodes.get((i + 6) % 9);
//            src.createRelationshipTo(dst1, CommonsRelationshipTypes.KNOWS);
// //            dst1.createRelationshipTo(src, CommonsRelationshipTypes.KNOWS);
//            src.createRelationshipTo(dst2, CommonsRelationshipTypes.KNOWS);
// //            dst2.createRelationshipTo(src, CommonsRelationshipTypes.KNOWS);
//        }

    }

    @Test
    public void louvain() {
        Louvain louvain = new Louvain(db);
        louvain.execute();

        LouvainResult lResult = louvain.getResult();

        LouvainLayer layer = lResult.layer(0);
        System.out.println(layer.getNode2CommunityMap());
        Assert.assertEquals(layer.size(), 9);

        layer = lResult.layer(1);
        System.out.println(layer.getNode2CommunityMap());
        Assert.assertEquals(layer.size(), 3);

        layer = lResult.layer(2);
        System.out.println(layer.getNode2CommunityMap());
        Assert.assertEquals(layer.size(), 0);

        louvain.clean();
    }
}
