package socs.network.node;

import socs.network.message.LSA;
import socs.network.message.LinkDescription;
import socs.network.node.WeightedGraph.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

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
    System.out.println("SRC ROUTER: " + this.rd.simulatedIPAddress);
    //Get all routers in in Topology
    ArrayList<String> routers = this.getRouters();
    System.out.println("ROUTERS: " + routers.toString());

    //create hashmap to map routerId to integer
    HashMap<String, Integer> posMap = new HashMap<String, Integer>();
    for(int i = 0; i < routers.size(); i++){
      posMap.put(routers.get(i), i);
    }

    //Initialize Graph
    Graph g = new WeightedGraph.Graph(routers.size());

    for (LSA lsa: _store.values()) {
      for (LinkDescription ld : lsa.links) {
        if(!lsa.linkStateID.equals(ld.linkID)){
          g.addEdge(posMap.get(lsa.linkStateID), posMap.get(ld.linkID), ld.tosMetrics);
        }
      }
    }

    g.printGraph();
    
    //TODO: run Dijskras on weighted graph
    g.dijkstra_GetMinDistances(posMap.get(this.rd.simulatedIPAddress));

    return null;
  }

  //helper function 
  ArrayList<String> getRouters() {
    ArrayList<String> routers = new ArrayList<String>();

    for (LSA lsa: _store.values()) {
      if(!routers.contains(lsa.linkStateID)){
        routers.add(lsa.linkStateID);
      }
      for (LinkDescription ld : lsa.links) {
        System.out.println("ld ids: "+ld.linkID);
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
