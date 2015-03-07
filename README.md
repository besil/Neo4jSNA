# Neo4jSNA

Neo4jSNA is a Java based collection of useful algorithms for Social Network analysis, based on Neo4j, the graph database.
Actually, all algorithms are implemented using the Neo4j Embedded mode.

The project uses Maven for dependency management. The other used library is the fastutil project (http://fastutil.di.unimi.it/):
if you don't know it yet, you should definitely check it.

The implemented algorithms are, for now:

- [x] Page Rank	(http://en.wikipedia.org/wiki/PageRank)
- [x] Connected Components
- [x] Triangle count
- [ ] Community Detection (http://arxiv.org/pdf/0906.0612v2.pdf)
	- [x] Label Propagation	(http://arxiv.org/pdf/0709.2938v1.pdf)
	- [ ] Louvain Method (http://arxiv.org/pdf/0803.0476v2.pdf)
	- [ ] Modularity (http://arxiv.org/pdf/physics/0602124.pdf)


# QuickStart

```Java
String path = "data/tmp/cineasts";

// Open a database instance
GraphDatabaseService g = new GraphDatabaseFactory().newEmbeddedDatabase(path);

// Declare the GraphEngine on the database instance
GraphEngine engine = new GraphEngine(g);

System.out.println("Triangle Count");
TriangleCount tc = new TriangleCount();
engine.execute(tc);
Long2LongMap triangleCount = tc.getResult();
Optional<Long> totalTriangles = triangleCount.values().stream().reduce( (x, y) -> x + y );
System.out.println("There are "+totalTriangles.get()+" triangles");

System.out.println("PageRank");
PageRank pr = new PageRank(g);
// Starts the algorithm on the given graph g
engine.execute(pr);
Map<Long, Double> ranks = pr.getResult();
Optional<Double> res = ranks.values().parallelStream().reduce( (x, y) -> x + y );
System.out.println("Check PageRank sum is 1.0: "+ res.get());

System.out.println("Connected Components");
ConnectedComponents cc = new ConnectedComponents();
engine.execute(cc);
Map<Long, Long> components = cc.getResult();
int totalComponents = new LongOpenHashSet( components.values() ).size();
System.out.println("There are "+ totalComponents+ " different components");

System.out.println("Label Propagation CD");
LabelPropagation lp = new LabelPropagation();
engine.execute(lp);
Long2LongMap communityMap = lp.getResult();
long totCommunities = new LongOpenHashSet( communityMap.values() ).size();
System.out.println("There are "+totCommunities+" communities");

// Don't forget to shutdown the database
g.shutdown();
```
