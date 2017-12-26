import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collection;

/**
 * Graph for storing all of the intersection (vertex) and road (edge) information.
 * Uses your GraphBuildingHandler to convert the XML files into a graph. Your
 * code must include the vertices, adjacent, distance, closest, lat, and lon
 * methods. You'll also need to include instance variables and methods for
 * modifying the graph (e.g. addNode and addEdge).
 *
 * @author Alan Yao, Josh Hug
 */
public class GraphDB {
    /** Your instance variables for storing the graph. You should consider
     * creating helper classes, e.g. Node, Edge, etc. */

    /**
     * Example constructor shows how to create and start an XML parser.
     * You do not need to modify this constructor, but you're welcome to do so.
     *
     * @param dbPath Path to the XML file to be parsed.
     */
    private Map<Long, VNode> graph = new HashMap<>();

    class VNode {
        long id;
        double lat, lon;
        double priority = Double.POSITIVE_INFINITY;
        List<Long> adj = new ArrayList<>();

        VNode(long id, double lat, double lon) {
            this.id = id;
            this.lat = lat;
            this.lon = lon;
        }

        void setPriority(double priority) {
            this.priority = priority;
        }
    }

    public GraphDB(String dbPath) {
        try {
            File inputFile = new File(dbPath);
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            GraphBuildingHandler gbh = new GraphBuildingHandler(this);
            saxParser.parse(inputFile, gbh);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        clean();
    }

    public void addNode(long id, double lat, double lon) {
        VNode node = new VNode(id, lat, lon);
        graph.put(id, node);
    }

    public VNode getNode(long id) {
        return graph.get(id);
    }

    public void addEdge(long xid, long yid) {
        if (!graph.containsKey(xid) || !graph.containsKey(yid)) {
            return;
        }
        graph.get(xid).adj.add(yid);
        graph.get(yid).adj.add(xid);
    }

    /**
     * Helper to process strings into their "cleaned" form, ignoring punctuation and capitalization.
     *
     * @param s Input string.
     * @return Cleaned string.
     */
    static String cleanString(String s) {
        return s.replaceAll("[^a-zA-Z ]", "").toLowerCase();
    }

    /**
     * Remove nodes with no connections from the graph.
     * While this does not guarantee that any two nodes in the remaining graph are connected,
     * we can reasonably assume this since typically roads are connected.
     */
    private void clean() {

        Iterator<Map.Entry<Long, VNode>> nodes = graph.entrySet().iterator();
        while (nodes.hasNext()) {
            if (nodes.next().getValue().adj.size() == 0) {
                nodes.remove();
            }
        }
    }

    /**
     * Returns an iterable of all vertex ids in the graph.
     */
    Iterable<Long> vertices() {
        return graph.keySet();
    }

    /**
     * Returns ids of all vertices adjacent to v.
     */
    Iterable<Long> adjacent(long v) {
        VNode vertex = graph.get(v);
        return vertex.adj;
    }

    /**
     * Returns the Euclidean distance between vertices v and w, where Euclidean distance
     * is defined as sqrt( (lonV - lonV)^2 + (latV - latV)^2 ).
     */
    double distance(long v, long w) {
        VNode nodeV = graph.get(v);
        VNode nodeW = graph.get(w);
        double lonDiff = Math.pow(nodeW.lon - nodeV.lon, 2);
        double latDiff = Math.pow(nodeW.lat - nodeV.lat, 2);
        return Math.sqrt(lonDiff + latDiff);
    }

    /**
     * Returns the vertex id closest to the given longitude and latitude.
     */
    long closest(double lon, double lat) {
        Collection<VNode> nodes = graph.values();
        double minDistance = 8888888;
        long id = 0;
        for (VNode vertex : nodes) {
            double distance = findDist(vertex, lon, lat);
            if (distance < minDistance) {
                minDistance = distance;
                id = vertex.id;
            }
        }
        return id;
    }

    double findDist(VNode x, double lon, double lat) {
        double lonDiff = Math.pow(x.lon - lon, 2);
        double latDiff = Math.pow(x.lat - lat, 2);
        return Math.sqrt(lonDiff + latDiff);
    }

    /**
     * Longitude of vertex v.
     */
    double lon(long v) {
        return graph.get(v).lon;
    }

    /**
     * Latitude of vertex v.
     */
    double lat(long v) {
        return graph.get(v).lat;
    }
}
