package com.besil.neo4jsna.utils;

import com.besil.neo4jsna.algorithms.Demon;
import org.neo4j.graphdb.*;
import org.neo4j.tooling.GlobalGraphOperations;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by besil on 01/06/15.
 */
public class GraphUtils {
    private static final Logger log = Logger.getLogger(GraphUtils.class.getName());

    public static Iterator<Relationship> getRelationshisByNodeAndRelationshipType(GraphDatabaseService g, Label label, Demon.DemonRelType relType) {
        List<Relationship> rels = new LinkedList<>();
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
            StringBuilder sb = new StringBuilder(n + " " + n.getLabels() + " ");
            List<String> properties = new LinkedList<>();
            n.getPropertyKeys().forEach(k -> properties.add(k));
            Collections.sort(properties);

            for (String pk : properties) {
                sb.append(pk + ":" + n.getProperty(pk) + " ");
            }
            log.info(sb.toString());
        }
        log.info("---------------------------");
        for (Relationship r : GlobalGraphOperations.at(g).getAllRelationships()) {
            log.info(r.getStartNode() + " -[" + r.getType() + "-> " + r.getEndNode());
        }
    }

//    public static void print(GraphDatabaseService neo) {
//
//        log.info("---------------");
//        log.info("Node count: " + GraphUtils.getNodeCount(neo));
//        log.info("Rel count: " + GraphUtils.getEdgeCount(neo));
//
//        GlobalGraphOperations.at(neo).getAllNodes().forEach(node -> {
//            StringBuilder sb = new StringBuilder();
//            sb.append(node).append(" ").append(node.getLabels()).append("(");
//            for (String key : node.getPropertyKeys())
//                sb.append(key).append(":").append(node.getProperty(key)).append(" ");
//            sb.append(")");
//            log.info(sb.toString());
//        });
//        log.info("***************");
//        GlobalGraphOperations.at(neo).getAllRelationships().forEach(rel -> {
//            StringBuilder sb = new StringBuilder();
//            sb.append(rel.getId()).append(": ");
//            sb.append("(").append(rel.getStartNode()).append("-").append(rel.getEndNode()).append(") ");
//            sb.append(rel.getType().name()).append(" ");
//            for (String key : rel.getPropertyKeys())
//                sb.append(key).append(":").append(rel.getProperty(key)).append(" ");
//            log.info(sb.toString());
//        });
//        log.info("---------------");
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
