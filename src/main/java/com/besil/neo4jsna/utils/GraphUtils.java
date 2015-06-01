package com.besil.neo4jsna.utils;

import com.besil.neo4jsna.algorithms.Demon;
import org.neo4j.graphdb.*;
import org.neo4j.tooling.GlobalGraphOperations;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by besil on 01/06/15.
 */
public class GraphUtils {
    public static long getNodeCount(GraphDatabaseService g) {
        int count = 0;
        for (Node n : GlobalGraphOperations.at(g).getAllNodes())
            count++;
        return count;
    }

    public static long getEdgeCount(GraphDatabaseService g) {
        int count = 0;
        for (Relationship r : GlobalGraphOperations.at(g).getAllRelationships())
            count++;
        return count;
    }

    public static Iterator<Relationship> getRelationshisByNodeAndRelationshipType(GraphDatabaseService g, Label label, Demon.DemonRelType relType) {
        List<Relationship> rels = new LinkedList<Relationship>();
        ResourceIterator<Node> nodes = g.findNodes(label);
        while (nodes.hasNext()) {
            Node node = nodes.next();
            for (Relationship rel : node.getRelationships(Direction.OUTGOING, relType))
                rels.add(rel);
        }
        return rels.iterator();
    }

    public static void printGraph(GraphDatabaseService g) {
        for (Node n : GlobalGraphOperations.at(g).getAllNodes()) {
            System.out.println(n + " " + n.getLabels());
        }
        for (Relationship r : GlobalGraphOperations.at(g).getAllRelationships()) {
            System.out.println(r.getStartNode() + " -[" + r.getType() + "-> " + r.getEndNode());
        }
    }
}
