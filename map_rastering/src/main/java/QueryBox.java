/**
 * Created by TV_01 on 4/17/2017.
 */
public class QueryBox {
    double ulLon, ulLat, lrLon, lrLat, lonDPP;
    QueryBox(double ulLon, double ulLat, double lrLon, double lrLat, double width) {
        this.ulLon = ulLon; this.ulLat = ulLat; this.lrLon = lrLon; this.lrLat = lrLat;
        this.lonDPP = (lrLon - ulLon) / width;
    }

    public boolean intersect(QuadTree s) {
        if (lrLat > s.ulLat()) {
            return false;
        } else if (lrLon < s.ulLon()) {
            return false;
        } else if (s.lrLat() > ulLat) {
            return false;
        } else if (s.lrLon() < ulLon) {
            return false;
        } return true;
    }
}
