import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;


/**
 * This class provides a shortestPath method for finding routes between two points
 * on the map. Start by using Dijkstra's, and if your code isn't fast enough for your
 * satisfaction (or the autograder), upgrade your implementation by switching it to A*.
 * Your code will probably not be fast enough to pass the autograder unless you use A*.
 * The difference between A* and Dijkstra's is only a couple of lines of code, and boils
 * down to the priority you use to order your vertices.
 */
public class Router {
    /**
     * Return a LinkedList of <code>Long</code>s representing the shortest path from st to dest,
     * where the longs are node IDs.
     */

    private static class NodeComparator implements Comparator<GraphDB.VNode> {
        @Override
        public int compare(GraphDB.VNode x, GraphDB.VNode y) {
            if (x.priority < y.priority) {
                return -1;
            } else if (x.priority == y.priority) {
                return 0;
            } else {
                return 1;
            }
        }
    }


    public static LinkedList<Long> shortestPath(GraphDB g, double stlon, double stlat,
                                                double destlon, double destlat) {
        PriorityQueue<GraphDB.VNode> queue = new PriorityQueue<>(10, new NodeComparator());
        Map<Long, Double> distTo = new HashMap<>();
        Set<Long> marked = new HashSet<>();
        Map<Long, Long> edgeTo = new HashMap<>();
        long startNodeID = g.closest(stlon, stlat);
        long endNodeID = g.closest(destlon, destlat);
        g.getNode(startNodeID).setPriority(0);
        distTo.put(startNodeID, 0.0);
        queue.add(g.getNode(startNodeID));
        while (!queue.isEmpty()) {
            long nodeID = queue.poll().id;
            if (nodeID == endNodeID) {
                break;
            }
            if (!marked.contains(nodeID)) {
                marked.add(nodeID);
            }
            if (edgeTo.get(nodeID) != null) {
                long fromNode = edgeTo.get(nodeID);
                distTo.put(nodeID, distTo.get(fromNode) + g.distance(nodeID, fromNode));
            }
            for (long neighborID : g.getNode(nodeID).adj) {
                GraphDB.VNode node = g.getNode(neighborID);
                double edgeLength = g.distance(neighborID, nodeID);
                double euclideanLength = g.findDist(node, destlon, destlat);
                double newPriority = edgeLength + euclideanLength + distTo.get(nodeID);
                //Node update, not dequeue
                if (node.priority > newPriority) {
                    edgeTo.put(neighborID, nodeID);
                    //Need to update distTo here
                    if (queue.contains(node)) {
                        queue.remove(node);
                        node.setPriority(newPriority);
                        queue.add(node);
                    } else if (!marked.contains(neighborID)) {
                        node.setPriority(newPriority);
                        queue.add(node);
                    }
                }
            }
        }
        LinkedList<Long> shortest = new LinkedList<>();
        long nodeID = endNodeID;
        long edgeToID = edgeTo.get(endNodeID);
        shortest.add(endNodeID);
        shortest.addFirst(edgeToID);
        while (edgeToID != startNodeID) {
            nodeID = edgeToID;
            edgeToID = edgeTo.get(nodeID);
            shortest.addFirst(edgeToID);
        }
        return shortest;
    }
}
