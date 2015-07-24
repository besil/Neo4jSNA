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

    public static void print(GraphDatabaseService g) {
        for (Node n : GlobalGraphOperations.at(g).getAllNodes()) {
            System.out.println(n + " " + n.getLabels());
        }
        System.out.println("---------------------------");
        for (Relationship r : GlobalGraphOperations.at(g).getAllRelationships()) {
            System.out.println(r.getStartNode() + " -[" + r.getType() + "-> " + r.getEndNode());
        }
    }

//    public static void print(GraphDatabaseService neo) {
//
//        System.out.println("---------------");
//        System.out.println("Node count: " + GraphUtils.getNodeCount(neo));
//        System.out.println("Rel count: " + GraphUtils.getEdgeCount(neo));
//
//        GlobalGraphOperations.at(neo).getAllNodes().forEach(node -> {
//            StringBuilder sb = new StringBuilder();
//            sb.append(node).append(" ").append(node.getLabels()).append("(");
//            for (String key : node.getPropertyKeys())
//                sb.append(key).append(":").append(node.getProperty(key)).append(" ");
//            sb.append(")");
//            System.out.println(sb.toString());
//        });
//        System.out.println("***************");
//        GlobalGraphOperations.at(neo).getAllRelationships().forEach(rel -> {
//            StringBuilder sb = new StringBuilder();
//            sb.append(rel.getId()).append(": ");
//            sb.append("(").append(rel.getStartNode()).append("-").append(rel.getEndNode()).append(") ");
//            sb.append(rel.getType().name()).append(" ");
//            for (String key : rel.getPropertyKeys())
//                sb.append(key).append(":").append(rel.getProperty(key)).append(" ");
//            System.out.println(sb.toString());
//        });
//        System.out.println("---------------");
//    }

    public static long getNodeCount(GraphDatabaseService db) {
        long count = 0;
        for (Node _ : GlobalGraphOperations.at(db).getAllNodes())
            count++;
        return count;
    }

    public static long getEdgeCount(GraphDatabaseService db) {
        long count = 0;
        for (Relationship _ : GlobalGraphOperations.at(db).getAllRelationships())
            count++;
        return count;
    }
}
