package at.tugraz.oop2;

import com.google.protobuf.ByteString;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class Map {
    // singleton structure
    private static Map _instance = null;
    static Map getInstance() {
        if (_instance == null) _instance = new Map();
        return _instance;
    };

    // constructor
    private Map() {}

    // methods
    public void load(String location) {  _data = MapLoader.load(location); };

    public Amenity getAmenity(Long id) {
        return _data.getAmenity(id);
    }
    public Amenity[] getAmenities(Rectangle2D.Double bbox, String type, Long skip, Long take) {
        return _data.getAmenities(bbox, type, skip, take);
    }
    public Amenity[] getAmenities(Point2D.Double point, Double distance,String type, Long skip, Long take) {
        return _data.getAmenities(point, distance, type, skip, take);
    }
    public Road getRoad(Long id) {
        return _data.getRoad(id);
    }
    public Road[] getRoads(Rectangle2D.Double frame, String type, Long skip, Long take) {
        return _data.getRoads(frame, type, skip, take);
    }
    public ByteString getTile(Integer x, Integer y, Integer z, List<String> filter) {
        return MapRenderer.getTile(x, y, z, filter, _data);
    }
    public Route getRoute(Long from, Long to, String weighting) {
        return _data.getRoute(from, to, weighting);
    }
    public Usages getUsage(Rectangle2D.Double frame) {
        return _data.getUsage(frame);
    }

    // members
    private MapData _data;
}