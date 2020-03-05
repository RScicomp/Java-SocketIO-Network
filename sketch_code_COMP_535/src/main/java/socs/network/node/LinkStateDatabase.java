package socs.network.node;

import socs.network.message.LSA;
import socs.network.message.LinkDescription;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class LinkStateDatabase {

  //linkID => LSAInstance
  //LSAs. We store the LSAs of other routers.
  HashMap<String, LSA> _store = new HashMap<String, LSA>();

  private RouterDescription rd = null;

  public LinkStateDatabase(RouterDescription routerDescription) {
    rd = routerDescription;
    LSA l = initLinkStateDatabase();
    _store.put(l.linkStateID, l);
  }

  /**
   * output the shortest path from this router to the destination with the given IP address
   */
  String getShortestPath(String destinationIP) {
    /**
     * https://www.geeksforgeeks.org/printing-paths-dijkstras-shortest-path-algorithm/
     */
    //Get all routers in in Topology
    ArrayList<String> routers = this.getRouters();
    System.out.println("ROUTERS: " + routers.toString());
    int nVertices = routers.size();

    int[][] adjacencyMatrix = new int[nVertices][nVertices];
    for (LSA lsa: _store.values()) {
      for (LinkDescription ld : lsa.links) {
        if(!lsa.linkStateID.equals(ld.linkID)){
          adjacencyMatrix[routers.indexOf(lsa.linkStateID)][routers.indexOf(ld.linkID)] = ld.tosMetrics;
        }
      }
    }

    System.out.println("Adjacency Matrix: ");

    for(int i=0; i<nVertices; i++){
      System.out.println(Arrays.toString(adjacencyMatrix[i]));
    }

    int startVertex = routers.indexOf(this.rd.simulatedIPAddress);
    
    int[] shortestDistances = new int[nVertices]; 
    
		boolean[] added = new boolean[nVertices]; 

		for (int vertexIndex = 0; vertexIndex < nVertices; vertexIndex++) { 
			shortestDistances[vertexIndex] = Integer.MAX_VALUE; 
			added[vertexIndex] = false; 
		} 
		
		shortestDistances[startVertex] = 0; 

		int[][] parents = new int[nVertices][2]; 

    parents[startVertex][0] = -1; //no parent
    parents[startVertex][1] = 0; //edge weight to parent

		for (int i = 1; i < nVertices; i++) { 

			int nearestVertex = -1; 
			int shortestDistance = Integer.MAX_VALUE; 
			for (int vertexIndex = 0; vertexIndex < nVertices; vertexIndex++) 
			{ 
				if (!added[vertexIndex] && shortestDistances[vertexIndex] < shortestDistance) { 
					nearestVertex = vertexIndex; 
					shortestDistance = shortestDistances[vertexIndex]; 
				} 
      } 
      
			added[nearestVertex] = true; 

			for (int vertexIndex = 0; vertexIndex < nVertices; vertexIndex++) { 
				int edgeDistance = adjacencyMatrix[nearestVertex][vertexIndex]; 
				
				if (edgeDistance > 0 && ((shortestDistance + edgeDistance) < shortestDistances[vertexIndex])) { 
          parents[vertexIndex][0] = nearestVertex; 
          parents[vertexIndex][1] = edgeDistance;
					shortestDistances[vertexIndex] = shortestDistance + edgeDistance; 
				} 
			} 
		} 

    return getPath(routers.indexOf(destinationIP), startVertex, parents, routers);

  }

  private static String getPath(int currentVertex, int srcVertex, int[][] parents, ArrayList<String> routers) { 
    String path = "";

    if (currentVertex == -1) { 
      return ""; 
    } 

    path = path + getPath(parents[currentVertex][0], srcVertex, parents, routers); 

    // NEED TO ACTUALLY BUILD STRING AND RETURN, ALSO TEST MORE THOUROUGHLLY
    if(currentVertex == srcVertex){
      return routers.get(currentVertex);
    }
    else{
      return "->(" + parents[currentVertex][1] + ") " + routers.get(currentVertex);
    }
  } 

  //helper function 
  ArrayList<String> getRouters() {
    ArrayList<String> routers = new ArrayList<String>();

    for (LSA lsa: _store.values()) {
      if(!routers.contains(lsa.linkStateID)){
        routers.add(lsa.linkStateID);
      }
      for (LinkDescription ld : lsa.links) {
        if(!routers.contains(ld.linkID)){
          routers.add(ld.linkID);
        }
      }
    }

    return routers;
  }

  //initialize the linkstate database by adding an entry about the router itself
  private LSA initLinkStateDatabase() {
    LSA lsa = new LSA();
    lsa.linkStateID = rd.simulatedIPAddress;
    lsa.lsaSeqNumber = Integer.MIN_VALUE;
    LinkDescription ld = new LinkDescription();
    ld.linkID = rd.simulatedIPAddress;
    ld.portNum = -1;
    ld.tosMetrics = 0;
    lsa.links.add(ld);
    return lsa;
  }


  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (LSA lsa: _store.values()) {
      sb.append(lsa.linkStateID).append("(" + lsa.lsaSeqNumber + ")").append(":\t");
      for (LinkDescription ld : lsa.links) {
        sb.append(ld.linkID).append(",").append(ld.portNum).append(",").
                append(ld.tosMetrics).append("\t");
      }
      sb.append("\n");
    }
    return sb.toString();
  }

}
