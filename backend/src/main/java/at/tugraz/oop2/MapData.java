package at.tugraz.oop2;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class MapData {
    // TODO: change to actual types (these are types used to parse the xml file - temp copies)
    public MapData(List<Road> roads, List<Amenity> amenities) {
        _roads = roads;
        _amenities = amenities;
    }

    // methods
    public Amenity getAmenity(Long id) {
        System.out.println("in amenity search: " + id + "  " + _amenities.size());
        for(Amenity amenity : _amenities)
        {
            System.out.println(amenity.id);
            if(amenity.id == id) return amenity;
        }
        return null; // TODO: be more explicit with exceptions
    }
    public Amenity[] getAmenities(Rectangle2D.Double bbox, String type, Integer skip, Integer take) {
        List<Amenity> result = new ArrayList<Amenity>();

        for(Amenity amenity : _amenities) {
            // filter
            /*if(amenity.type != type) continue;
            if(!isInside(bbox, amenity.geom)) continue;*/

            result.add(amenity);
        }

        return result.toArray(new Amenity[0]);
    }

    public Road getRoad(Long id) {
        for(Road road : _roads)
            if(road.id == id) return road;
        return null; // TODO: be more explicit with exceptions
    }
    public Road[] getRoads(Rectangle2D.Double bbox, String type, Integer skip, Integer take) {
        List<Road> result = new ArrayList<Road>();

        for(Road road : _roads) {
            // filter
            if(road.type != type) continue;
            //if(!isInside(bbox, road.geom)) continue;

            result.add(road);
        }

        return result.toArray(new Road[0]);
    }

    /*
    private boolean isInside(Rectangle2D.Double bbox, MapObject.Geometry geom) {
        return true; // TODO: implement
    }*/

    // member
    private List<Road> _roads = new ArrayList<Road>();
    private List<Amenity> _amenities = new ArrayList<Amenity>();
}
