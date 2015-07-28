package com.besil.neo4jsna.main;

import com.besil.neo4jsna.algorithms.*;
import com.besil.neo4jsna.algorithms.louvain.Louvain;
import com.besil.neo4jsna.algorithms.louvain.LouvainResult;
import com.besil.neo4jsna.engine.GraphAlgoEngine;
import com.besil.neo4jsna.measures.DirectedModularity;
import com.besil.neo4jsna.measures.UndirectedModularity;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.helpers.collection.IteratorUtil;
import org.neo4j.io.fs.FileUtils;
import org.neo4j.tooling.GlobalGraphOperations;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Neo4jSNAMain {
	public static void main(String[] args) {
        String zipFile = "data/cineasts_12k_movies_50k_actors_2.1.6.zip";
        String path = "data/cineasts_12k_movies_50k_actors.db";

        try {
            FileUtils.deleteRecursively(new File(path));
            unZipIt(zipFile, path);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        long nodeCount, relsCount;
		
		// Open a database instance
        GraphDatabaseService g = new GraphDatabaseFactory()
                .newEmbeddedDatabaseBuilder(path)
                .setConfig(GraphDatabaseSettings.allow_store_upgrade, "true")
                .newGraphDatabase();
        try (Transaction tx = g.beginTx() ) {
			nodeCount = IteratorUtil.count( GlobalGraphOperations.at(g).getAllNodes() );
			relsCount = IteratorUtil.count( GlobalGraphOperations.at(g).getAllRelationships() );
			tx.success();
		}
		
		System.out.println("Node count: "+nodeCount);
        System.out.println("Rel count: " + relsCount);

        // Declare the GraphAlgoEngine on the database instance
		GraphAlgoEngine engine = new GraphAlgoEngine(g);
		if( args.length > 1 && args[1].equals("off") )
			engine.disableLogging();

        Louvain louvain = new Louvain(g);
        louvain.execute();
        LouvainResult result = louvain.getResult();
        for (int layer : result.layers()) {
            System.out.println("Layer " + layer + ": " + result.layer(layer).size() + " nodes");
        }

        LabelPropagation lp = new LabelPropagation();
        // Starts the algorithm on the given graph g
		engine.execute(lp);
		Long2LongMap communityMap = lp.getResult();
		long totCommunities = new LongOpenHashSet( communityMap.values() ).size();
        System.out.println("There are " + totCommunities + " communities according to Label Propagation");

		DirectedModularity modularity = new DirectedModularity(g);
		engine.execute(modularity);
        System.out.println("The directed modularity of this network is " + modularity.getResult());

        UndirectedModularity umodularity = new UndirectedModularity(g);
		engine.execute(umodularity);
        System.out.println("The undirected modularity of this network is " + umodularity.getResult());

        engine.clean(lp); // Now you can clean Label propagation results

        TriangleCount tc = new TriangleCount();
		engine.execute(tc);
		Long2LongMap triangleCount = tc.getResult();
		Optional<Long> totalTriangles = triangleCount.values().stream().reduce( (x, y) -> x + y );
		System.out.println("There are "+totalTriangles.get()+" triangles");

		PageRank pr = new PageRank(g);
		engine.execute(pr);
		Long2DoubleMap ranks = pr.getResult();
		engine.clean(pr);
		Optional<Double> res = ranks.values().parallelStream().reduce( (x, y) -> x + y );
		System.out.println("Check PageRank sum is 1.0: "+ res.get());

		ConnectedComponents cc = new ConnectedComponents();
		engine.execute(cc);
		Long2LongMap components = cc.getResult();
		engine.clean(cc);
		int totalComponents = new LongOpenHashSet( components.values() ).size();
		System.out.println("There are "+ totalComponents+ " different connected components");
		
		StronglyConnectedComponents scc = new StronglyConnectedComponents();
		engine.execute(scc);
		components = scc.getResult();
		engine.clean(scc);
		totalComponents = new LongOpenHashSet( components.values() ).size();
		System.out.println("There are "+ totalComponents+ " different strongly connected components");
		
		// Don't forget to shutdown the database
		g.shutdown();
	}

    public static void unZipIt(String zipFile, String outputFolder) {

        byte[] buffer = new byte[1024];

        try {

            //create output directory is not exists
            File folder = new File(outputFolder);
            if (!folder.exists()) {
                folder.mkdir();
            }

            //get the zip file content
            ZipInputStream zis =
                    new ZipInputStream(new FileInputStream(zipFile));
            //get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();

            while (ze != null) {

                String fileName = ze.getName();
                File newFile = new File(outputFolder + File.separator + fileName);

                System.out.println("file unzip : " + newFile.getAbsoluteFile());

                //create all non exists folders
                //else you will hit FileNotFoundException for compressed folder
                new File(newFile.getParent()).mkdirs();

                FileOutputStream fos = new FileOutputStream(newFile);

                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }

                fos.close();
                ze = zis.getNextEntry();
            }

            zis.closeEntry();
            zis.close();

            System.out.println("Done");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
