# Neo4jSNA

Neo4jSNA is a Java based collection of useful algorithms for Social Network analysi, based on Neo4j, the graph database.
Actually, there's no support for Neo4j REST API: all algorithms are implemented using the Neo4j Embedded mode.

The implemented algorithms are, for now:

- [x] Page Rank
- [x] Connected Components
- [ ] Triangle count
- [ ] Community Detection
	- [ ] Label Propagation
	- [ ] Louvain Method

# QuickStart

'''Java
String cinea = "data/tmp/cineasts";
String path = cinea;
long nodeCount, relsCount;

GraphDatabaseService g = new GraphDatabaseFactory().newEmbeddedDatabase(path);
try (Transaction tx = g.beginTx() ) {
	nodeCount = IteratorUtil.count( GlobalGraphOperations.at(g).getAllNodes() );
	relsCount = IteratorUtil.count( GlobalGraphOperations.at(g).getAllRelationships() );
	tx.success();
}

System.out.println("Node count: "+nodeCount);
System.out.println("Rel count: "+relsCount);

GraphEngine engine = new GraphEngine(g);

System.out.println("PageRank");
PageRank pr = new PageRank(g);
engine.execute(pr);
Long2DoubleMap ranks = pr.getResult();
Optional<Double> res = ranks.values().parallelStream().reduce( (x, y) -> x + y );
System.out.println("Check PageRank sum is 1.0: "+ res.get());

System.out.println("Connected Components");
ConnectedComponents cc = new ConnectedComponents();
engine.execute(cc);
Long2LongMap components = cc.getResult();
int totalComponents = new LongOpenHashSet( components.values() ).size();
System.out.println("There are "+ totalComponents+ " different components");

g.shutdown();
'''
