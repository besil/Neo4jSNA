# Neo4jSNA

Neo4jSNA is a Java based collection of useful algorithms for Social Network analysi, based on Neo4j, the graph database.
Actually, there's no support for Neo4j REST API: all algorithms are implemented using the Neo4j Embedded mode.

The project uses Maven for dependency management. The other used library is the fastutil project (http://fastutil.di.unimi.it/):
if you don't know it yet, you should definitely check it.

The implemented algorithms are, for now:

- [x] Page Rank
- [x] Connected Components
- [ ] Triangle count
- [ ] Community Detection
	- [ ] Label Propagation
	- [ ] Louvain Method

# QuickStart

```Java
String path = "data/tmp/cineasts";

// Open a database instance
GraphDatabaseService g = new GraphDatabaseFactory().newEmbeddedDatabase(path);

// Declare the GraphEngine on the database instance
GraphEngine engine = new GraphEngine(g);

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

// Don't forget to shutdown the database
g.shutdown();
```
