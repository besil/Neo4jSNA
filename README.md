# Neo4jSNA

Neo4jSNA is a Java based collection of useful algorithms for Social Network analysis, based on Neo4j, the graph database.
Actually, all algorithms are implemented using the Neo4j Embedded mode.

The project uses Maven for dependency management. The other used library is the <a href="http://fastutil.di.unimi.it/" target="_blank">fastutil project</a>:
if you don't know it yet, you should definitely check it.

The implemented algorithms are, for now:

- [x] <a href="http://en.wikipedia.org/wiki/PageRank" target="_blank">PageRank</a>:
- [x] Connected Components
- [x] Triangle count
- [ ] <a href="http://arxiv.org/pdf/0906.0612v2.pdf" target="_blank">Community Detection</a>
	- [x] <a href="http://arxiv.org/pdf/0709.2938v1.pdf" target="_blank">Label Propagation</a>
	- [x] <a href="http://arxiv.org/pdf/0803.0476v2.pdf" target="_blank">Louvain Method</a>
	- [ ] <a href="http://www.michelecoscia.com/wp-content/uploads/2012/08/cosciakdd12.pdf" target="_blank">Demon</a>
	- [x] <a href="http://arxiv.org/pdf/physics/0602124.pdf" target="_blank">Modularity</a>


# QuickStart

```Java
String path = "data/tmp/cineasts";

// Open a database instance
GraphDatabaseService g = new GraphDatabaseFactory().newEmbeddedDatabase(path);

// Declare the GraphAlgoEngine on the database instance
GraphAlgoEngine engine = new GraphAlgoEngine(g);

LabelPropagation lp = new LabelPropagation();
// Starts the algorithm on the given graph g
engine.execute(lp);
Long2LongMap communityMap = lp.getResult();
long totCommunities = new LongOpenHashSet( communityMap.values() ).size();
System.out.println("There are "+totCommunities+" communities according to Label Propagation");

DirectedModularity modularity = new DirectedModularity(g);
engine.execute(modularity);
System.out.println("The directed modularity of this network is "+modularity.getResult());

UndirectedModularity umodularity = new UndirectedModularity(g);
engine.execute(umodularity);
System.out.println("The undirected modularity of this network is "+umodularity.getResult());

TriangleCount tc = new TriangleCount();
engine.execute(tc);
Long2LongMap triangleCount = tc.getResult();
Optional<Long> totalTriangles = triangleCount.values().stream().reduce( (x, y) -> x + y );
System.out.println("There are "+totalTriangles.get()+" triangles");

PageRank pr = new PageRank(g);
engine.execute(pr);
Long2DoubleMap ranks = pr.getResult();
Optional<Double> res = ranks.values().parallelStream().reduce( (x, y) -> x + y );
System.out.println("Check PageRank sum is 1.0: "+ res.get());

ConnectedComponents cc = new ConnectedComponents();
engine.execute(cc);
Long2LongMap components = cc.getResult();
int totalComponents = new LongOpenHashSet( components.values() ).size();
System.out.println("There are "+ totalComponents+ " different connected components");

StronglyConnectedComponents scc = new StronglyConnectedComponents();
engine.execute(cc);
components = scc.getResult();
totalComponents = new LongOpenHashSet( components.values() ).size();
System.out.println("There are "+ totalComponents+ " different strongly connected components");

// Don't forget to shutdown the database
g.shutdown();
```
