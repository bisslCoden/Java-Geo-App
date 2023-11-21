package at.tugraz.oop2;

import org.w3c.dom.css.Rect;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapData {
    // TODO: change to actual types (these are types used to parse the xml file - temp copies)
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
    class Road {};
    class Amenity {};
    class Route {};
    class Usage {};
    class PortableNetworkGraphic {};

    // methods
    // TODO: parameters might change (e.g. Point to x and y)
    // roads
    public Road getRoad(Long id) { return new Road(); };
    public Road[] getRoads(Rect boundingBox) { return new Road[0]; }

    // amenities
    // TODO: add parameter for type restriction
    public Amenity getAmenity(Long id) { return new Amenity(); };
    public Amenity[] getAmenities(Rect boundingBox) { return new Amenity[0]; }
    public Amenity[] getAmenities(Point location, Double range) { return new Amenity[0]; }

    // TODO: rendering might be implemented in the background in combination with the dataset
    public PortableNetworkGraphic getTile(Point location) { return new PortableNetworkGraphic(); }

    // route (A2)
    public Route getRoute(Long from, Long to, Weight w) { return new Route(); }

    // usage (A2)
    public Usage getUsage(Rect boundingBox) { return new Usage(); }

}
