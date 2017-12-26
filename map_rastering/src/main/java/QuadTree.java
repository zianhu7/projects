import java.util.List;
import java.util.ArrayList;

/**
 * Created by Zian Hu on 4/16/2017.
 */
public class QuadTree {

    private Node root;
    private QuadTree NW, NE, SE, SW;

    class Node {
        String imgName;
        double lonDPP, latDPP;
        double ulLon, ulLat, lrLon, lrLat;
        int depth;

        Node(String imgName, double ulLon, double ulLat, double lrLon, double lrLat) {
            this.imgName = imgName;
            this.ulLon = ulLon;
            this.ulLat = ulLat;
            this.lrLon = lrLon;
            this.lrLat = lrLat;
            this.lonDPP = calcDPP(lrLon, ulLon);
            this.latDPP = calcDPP(ulLat, lrLat);
            String imgNum = imgName.substring(0, imgName.length() - 4);
            if (imgNum.equals("root")) {
                depth = 0;
            } else {
                depth = imgNum.length();
            }
        }
    }


    public QuadTree(String imgName, double ulLon, double ulLat, double lrLon, double lrLat) {
        this.root = new Node(imgName, ulLon, ulLat, lrLon, lrLat);
        build();
    }

    /*
    public void insert(Node x) {
        this.root = insert(root, x.imgName, x.ulLon, x.ulLat, x.lrLon, x.lrLat);
    }

    private QuadTree insert(Node s, String imgName, double ulLon, double ulLat, double lrLon, double lrLat) {
        double xPos = findCenterX(ulLon, lrLon);
        double yPos = findCenterY(ulLat, lrLat);
        if (s == null) {
            return new Node(imgName, ulLon, ulLat, lrLon, lrLat);
        } else if (xPos < s.xPos && yPos < s.yPos) {
            s.SW = insert(s.SW, imgName, ulLon, ulLat, lrLon, lrLat);
        } else if (xPos < s.xPos && yPos > s.yPos) {
            s.NW = insert(s.NW, imgName, ulLon, ulLat, lrLon, lrLat);
        } else if (xPos > s.xPos && yPos < s.yPos) {
            s.SE = insert(s.SE, imgName, ulLon, ulLat, lrLon, lrLat);
        } else if (xPos > s.xPos && yPos > s.yPos) {
            s.NE = insert(s.NE, imgName, ulLon, ulLat, lrLon, lrLat);
        } return s;
    } */

    public void build() {
        if (this.root.imgName.length() >= 11) {
            return;
        } else if (root.imgName.equals("root.png")) {
            NW = new QuadTree("1.png", root.ulLon, root.ulLat,
                    (root.ulLon + root.lrLon) / 2, (root.ulLat + root.lrLat) / 2);
            NE = new QuadTree("2.png", (root.ulLon + root.lrLon) / 2, root.ulLat,
                    root.lrLon, (root.ulLat + root.lrLat) / 2);
            SW = new QuadTree("3.png", root.ulLon, (root.ulLat + root.lrLat) / 2,
                    (root.ulLon + root.lrLon) / 2, root.lrLat);
            SE = new QuadTree("4.png", (root.ulLon + root.lrLon) / 2,
                    (root.ulLat + root.lrLat) / 2, root.lrLon, root.lrLat);
        } else {
            String tmp = root.imgName.substring(0, root.imgName.length() - 4);
            NW = new QuadTree(tmp + "1.png", root.ulLon, root.ulLat,
                    (root.ulLon + root.lrLon) / 2, (root.ulLat + root.lrLat) / 2);
            NE = new QuadTree(tmp + "2.png", (root.ulLon + root.lrLon) / 2, root.ulLat,
                    root.lrLon, (root.ulLat + root.lrLat) / 2);
            SW = new QuadTree(tmp + "3.png", root.ulLon, (root.ulLat + root.lrLat) / 2,
                    (root.ulLon + root.lrLon) / 2, root.lrLat);
            SE = new QuadTree(tmp + "4.png", (root.ulLon + root.lrLon) / 2,
                    (root.ulLat + root.lrLat) / 2, root.lrLon, root.lrLat);
        }
    }

    public void levelFind(QuadTree s, int level, Rasterer x, QueryBox query) {
        if (level == 0) {
            if (query.intersect(s)) {
                x.imgList.add(s.root);
            }
            return;
        } else {
            levelFind(s.NW, level - 1, x, query);
            levelFind(s.NE, level - 1, x, query);
            levelFind(s.SW, level - 1, x, query);
            levelFind(s.SE, level - 1, x, query);
        }
    }

    /*
    public List<Node> traverseSearch(QueryBox query, QuadTree s) {
        if (root.imgName.length() == 11) {
            List<Node> imgList = new ArrayList<>();
            if (query.intersect(s)) {
                imgList.add(root);
                return imgList;
            }
        } else {
            //If intersects a quadrant and no others but doesn't meet LonDPP requirements, search children.
            if (query.intersect(s.NW) && !query.intersect(s.NE) && !query.intersect(s.SW) &&
                    !query.intersect(s.SE) && calcLonDPP(s.NW.ulLon(), s.NW.lrLon()) >= query.lonDPP) {
                return traverseSearch(query, s.NW);
            } else if (query.intersect(s.NE) && !query.intersect(s.NW) && !query.intersect(s.SW) &&
                    !query.intersect(s.SE) && calcLonDPP(s.NE.ulLon(), s.NE.lrLon()) >= query.lonDPP) {
                return traverseSearch(query, s.NE);
            } else if (query.intersect(s.SW) && !query.intersect(s.NW) && !query.intersect(s.NE) &&
                    !query.intersect(s.SE) && calcLonDPP(s.SW.ulLon(), s.SW.lrLon()) >= query.lonDPP) {
                return traverseSearch(query, s.SW);
            } else if (query.intersect(s.SE) && !query.intersect(s.NW) && !query.intersect(s.SW) &&
                    !query.intersect(s.NE) && calcLonDPP(s.SE.ulLon(), s.SE.lrLon()) >= query.lonDPP) {
                return traverseSearch(query, s.SE);
            } else {

            }
        }
    } */

    /*
    public void traverse(QueryBox query, QuadTree s, Rasterer x) {
        if (query.intersect(s)) {
            if (query.lonDPP >= s.calcDPP(s.lrLon(), s.ulLon()) || s.root.imgName.length() == 11) {
                x.imgList.add(s.root);
                return;
            } else {
                traverse(query, s.NW, x);
                traverse(query, s.NE, x);
                traverse(query, s.SW, x);
                traverse(query, s.SE, x);
            }
        } else {
            return;
        }
    }*/


    public void searchLevel(QueryBox query, QuadTree s, Rasterer x) {
        if (query.intersect(s)) {
            if (query.lonDPP >= s.calcDPP(s.lrLon(), s.ulLon()) || s.root.depth == 7) {
                x.setDepth(s.root.depth);
            } else {
                searchLevel(query, s.NW, x);
                searchLevel(query, s.NE, x);
                searchLevel(query, s.SW, x);
                searchLevel(query, s.SE, x);
            }
        } else {
            return;
        }
    }

    private double calcDPP(double max, double min) {
        return (max - min) / 256;
    }

    public double ulLon() {
        return root.ulLon;
    }

    public double ulLat() {
        return root.ulLat;
    }

    public double lrLon() {
        return root.lrLon;
    }

    public double lrLat() {
        return root.lrLat;
    }
}
