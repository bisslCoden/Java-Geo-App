package at.tugraz.oop2;

import org.w3c.dom.css.Rect;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapData {
    // TODO: change to actual types (these are types used to parse the xml file - temp copies)
    private static MapData instance_ = null;

    public static MapData instance(){
        if (instance_ == null)
            instance_ = new MapData();
        return instance_;
    };
    static public class Node {
        public Node() {
            tags = new HashMap<String, String>();
        };
        Long id;
        Double longitude; // x-coordinate
        Double latitude;  // y-coordinate
        Map<String, String> tags;
    };
    static public class Way {
        public Way() {
            references = new ArrayList<Long>();
            tags = new HashMap<String, String>();
        };
        Long id;
        List<Long> references;
        Map<String, String> tags;
    };
    static public class Relation {
        public Relation() {
            inner_relations = new ArrayList<Long>();
            outer_relations = new ArrayList<Long>();
            inner_ways = new ArrayList<Long>();
            outer_ways = new ArrayList<Long>();
        };
        List<Long> inner_relations;
        List<Long> outer_relations;
        List<Long> inner_ways;
        List<Long> outer_ways;
        Long id;
        Map<String, String> tags;
    };

    // TODO: remove temps (used to demonstrate interface with the data object)
    enum Weight {};

    class Route {};
    class Usage {};
    class PortableNetworkGraphic {};

    // methods
    // TODO: parameters might change (e.g. Point to x and y)
    // roads
    public Road getRoad(Long id) { return null; };
    public Road[] getRoads(Rectangle2D.Double boundingBox) { return null; }

    // amenities
    // TODO: add parameter for type restriction
    public Amenitiy getAmenity(Long id) { return null; };
    public Amenitiy[] getAmenities(Rectangle2D.Double boundingBox) { return null; }
    public Amenitiy[] getAmenities(Point2D.Double location, Double range) { return null; }

    // TODO: rendering might be implemented in the background in combination with the dataset
    public PortableNetworkGraphic getTile(Point location) { return new PortableNetworkGraphic(); }

    // route (A2)
    public Route getRoute(Long from, Long to, Weight w) { return new Route(); }

    // usage (A2)
    public Usage getUsage(Rect boundingBox) { return new Usage(); }

}
