import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;

/**
 * This class provides all code necessary to take a query box and produce
 * a query result. The getMapRaster method must return a Map containing all
 * seven of the required fields, otherwise the front end code will probably
 * not draw the output correctly.
 */
public class Rasterer {
    protected List<QuadTree.Node> imgList = new ArrayList<>();
    private QuadTree quadTree = new QuadTree("root.png", MapServer.ROOT_ULLON,
            MapServer.ROOT_ULLAT, MapServer.ROOT_LRLON, MapServer.ROOT_LRLAT);
    private boolean successfulQuery;
    private double rasteredUllon, rasteredUllat, rasteredLrlon, rasteredLrlat;
    private int imageWidth, imageHeight, queryDepth;
    // Recommended: QuadTree instance variable. You'll need to make
    //              your own QuadTree since there is no built-in quadtree in Java.

    /**
     * imgRoot is the name of the directory containing the images.
     * You may not actually need this for your class.
     */
    public Rasterer(String imgRoot) {

    }

    public void setDepth(int depth) {
        this.queryDepth = depth;
    }

    private static class NodeFormatter implements Comparator<QuadTree.Node> {
        @Override
        public int compare(QuadTree.Node x, QuadTree.Node y) {
            if (y.ulLat < x.ulLat) {
                return -1;
            } else if (y.ulLat == x.ulLat) {
                if (x.ulLon < y.ulLon) {
                    return -1;
                } else if (x.ulLon > y.ulLon) {
                    return 1;
                } else {
                    return 0;
                }
            } else {
                return 1;
            }
        }
    }

    private int[] toRowCol(int index) {
        int row = index / imageWidth;
        int col = index % imageWidth;
        int[] rowCol = new int[2];
        rowCol[0] = row;
        rowCol[1] = col;
        return rowCol;
    }

    private String[][] formatNodes() {
        setDimensions();
        if (!successfulQuery) {
            return null;
        }
        //System.out.println("imageHeight is " + imageHeight);
        //System.out.println("Image Width is" + imageWidth);
        //System.out.println("Image Height is" + imageHeight);
        String[][] nameList = new String[imageHeight][imageWidth];
        //System.out.println("nameList is a " + imageHeight + "by" + imageWidth + "array.");
        //System.out.println("imgList is of length " + imgList.size());
        for (int i = 0; i < imgList.size(); i += 1) {
            //System.out.println(row);
            //System.out.println(col);
            int[] indices = toRowCol(i);
            //System.out.println("About to insert in indices " + indices[0] + "," + indices[1]);
            nameList[indices[0]][indices[1]] = "img/" + imgList.get(i).imgName;
        }
        return nameList;
    }

    private int findRIndex(QuadTree.Node node) {
        double yDiff = (rasteredUllat - node.ulLat) / node.latDPP;
        System.out.println("The node is " + node.imgName);
        int row = (int) Math.round(yDiff / 256);
        return row;
    }

    private int findCIndex(QuadTree.Node node) {
        double xDiff = (node.ulLon - rasteredUllon) / node.lonDPP;
        int col = (int) Math.round(xDiff / 256);
        return col;
    }

    private void setDimensions() {
        if (imgList == null) {
            successfulQuery = false;
            return;
        }
        successfulQuery = true;
        imgList.sort(new NodeFormatter());
        rasteredUllon = imgList.get(0).ulLon;
        rasteredUllat = imgList.get(0).ulLat;
        rasteredLrlon = imgList.get(imgList.size() - 1).lrLon;
        rasteredLrlat = imgList.get(imgList.size() - 1).lrLat;
        Set<Double> uniqueLats = new HashSet<>();
        for (QuadTree.Node x : imgList) {
            uniqueLats.add(x.ulLat);
        }
        imageHeight = uniqueLats.size();
        imageWidth = imgList.size() / imageHeight;
        /*
        double minLon = imgList.get(0).ulLon;
        double maxLon = imgList.get(0).lrLon;
        double minLat = imgList.get(0).lrLat;
        double maxLat = imgList.get(0).ulLat;
        for (QuadTree.Node node: imgList) {
            if (node.ulLon < minLon) {
                minLon = node.ulLon;
            }
            if (node.lrLon > maxLon) {
                maxLon = node.lrLon;
            }
            if (node.ulLat > maxLat) {
                maxLat = node.ulLat;
            }
            if (node.lrLat < minLat) {
                minLat = node.lrLat;
            }
        }
        rasteredLrlon = maxLon; rasteredUllat = maxLat;
        rasteredLrlat = minLat; rasteredUllon = minLon;
        double widthDiff = (maxLon - minLon) / imgList.get(0).lonDPP;
        imageWidth = (int) Math.round(widthDiff / 256);
        /*System.out.println("MaxLon is " + maxLon);
        System.out.println("MinLon is " + minLon);
        System.out.println("MaxLat is " + maxLat);
        System.out.println("MinLat is " + minLat);
        System.out.println("Length of rastered image in pixels is " + widthDiff);
        System.out.println("imageWidth is " + imageWidth);*/
    }

    private int findDepth() {
        String fileName = imgList.get(0).imgName;
        fileName = fileName.substring(0, fileName.length() - 4);
        if (fileName.equals("root")) {
            return 0;
        }
        return fileName.length();
    }


    /**
     * Takes a user query and finds the grid of images that best matches the query. These
     * images will be combined into one big image (rastered) by the front end. <br>
     * <p>
     * The grid of images must obey the following properties, where image in the
     * grid is referred to as a "tile".
     * <ul>
     * <li>The tiles collected must cover the most longitudinal distance per pixel
     * (LonDPP) possible, while still covering less than or equal to the amount of
     * longitudinal distance per pixel in the query box for the user viewport size. </li>
     * <li>Contains all tiles that intersect the query bounding box that fulfill the
     * above condition.</li>
     * <li>The tiles must be arranged in-order to reconstruct the full image.</li>
     * </ul>
     * </p>
     *
     * @param params Map of the HTTP GET request's query parameters - the query box and
     *               the user viewport width and height.
     * @return A map of results for the front end as specified:
     * "render_grid"   -> String[][], the files to display
     * "raster_ul_lon" -> Number, the bounding upper left longitude of the rastered image <br>
     * "raster_ul_lat" -> Number, the bounding upper left latitude of the rastered image <br>
     * "raster_lr_lon" -> Number, the bounding lower right longitude of the rastered image <br>
     * "raster_lr_lat" -> Number, the bounding lower right latitude of the rastered image <br>
     * "depth"         -> Number, the 1-indexed quadtree depth of the nodes of the rastered image.
     * Can also be interpreted as the length of the numbers in the image
     * string. <br>
     * "query_success" -> Boolean, whether the query was able to successfully complete. Don't
     * forget to set this to true! <br>
     * @see #REQUIRED_RASTER_REQUEST_PARAMS
     */
    public Map<String, Object> getMapRaster(Map<String, Double> params) {
        imgList.clear();
        Map<String, Object> results = new HashMap<>();
        QueryBox query = new QueryBox(params.get("ullon"), params.get("ullat"), params.get("lrlon"),
                params.get("lrlat"), params.get("w"));
        //quadTree.traverse(query, quadTree, this);
        quadTree.searchLevel(query, quadTree, this);
        quadTree.levelFind(quadTree, queryDepth, this, query);
        String[][] renderGrid = formatNodes();
        results.put("render_grid", renderGrid);
        results.put("raster_ul_lon", rasteredUllon);
        results.put("raster_ul_lat", rasteredUllat);
        results.put("raster_lr_lon", rasteredLrlon);
        results.put("raster_lr_lat", rasteredLrlat);
        results.put("depth", queryDepth);
        results.put("query_success", true);
        return results;
    }

}
