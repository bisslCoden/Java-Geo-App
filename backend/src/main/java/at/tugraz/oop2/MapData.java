package at.tugraz.oop2;

import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.FactoryException;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;

import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D;

import java.util.ArrayList;
import java.util.List;

public class MapData {
    public MapData() {}
    public MapData(List<Road> roads, List<Amenity> amenities, List<MapObject> others) {
        this._roads = roads;
        this._amenities = amenities;
        this._others = others;

        // setup transformation
        try {
            CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:4326");
            CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:31256");
            this._transform = CRS.findMathTransform(sourceCRS, targetCRS, true);
        } catch (FactoryException e) {
            throw new RuntimeException(e);
        }
    }

    // methods
    public Amenity getAmenity(Long id) {
        for(Amenity amenity : _amenities)
        {
            System.out.println(amenity.id);
            if(amenity.id == id) return amenity;
        }
        return null;
    }
    public Amenity[] getAmenities(Rectangle2D.Double frame, String type, Long skip, Long take) {
        List<Amenity> result = new ArrayList<Amenity>();

        int skipped = 0, took = 0;
        for(Amenity amenity : _amenities) {
            // filter
            if(took >= take) break;
            if(type != null && amenity.type != type) continue; // TODO: verify
            if(!isInside(frame, amenity.geom)) continue;
            if(skipped < skip) { ++skipped; continue; }

            // take
            result.add(amenity);
            ++took;
        }

        return result.toArray(new Amenity[0]);
    }
    public Amenity[] getAmenities(Point2D.Double point, Double distance, String type, Long skip, Long take) {
        List<Amenity> result = new ArrayList<>();

        int skipped = 0, took = 0;
        for(Amenity amenity : _amenities) {
            // filter
            if(took >= take) break;
            if(type != null && amenity.type != type) continue; // TODO: verify
            if(!isInside(point, distance, amenity.geom)) continue;
            if(skipped < skip) { ++skipped; continue; }

            // take
            result.add(amenity);
            ++took;
        }

        return result.toArray(new Amenity[0]);
    }

    public Road getRoad(Long id) {
        for(Road road : _roads)
            if(road.id == id) return road;
        return null;
    }
    public Road[] getRoads(Rectangle2D.Double frame, String type, Long skip, Long take) {
        List<Road> result = new ArrayList<Road>();

        int skipped = 0, took = 0;
        for(Road road : _roads) {
            // filter
            if(took >= take) break;
            if(type != null && road.type != type) continue; // TODO: verify
            if(!isInside(frame, road.geom)) continue;
            if(skipped < skip) { ++skipped; continue;}

            // take
            result.add(road);
            ++took;
        }

        return result.toArray(new Road[0]);
    }

    public boolean isInside(Rectangle2D.Double frame, Geometry geom) { // TODO: refactor
        Geometry boundingBox = new GeometryFactory().createPolygon(new Coordinate[] {
                new Coordinate(frame.x, frame.y),
                new Coordinate(frame.x + frame.width, frame.y),
                new Coordinate(frame.x + frame.width, frame.y + frame.height),
                new Coordinate(frame.x, frame.y + frame.width),
                new Coordinate(frame.x, frame.y),
        });

        return geom.intersects(boundingBox);
    }

    public boolean isInside(Point2D.Double point, Double distance, Geometry geom) { // TODO: refactor
        Geometry center, target;
        try {
            center = JTS.transform(new GeometryFactory().createPoint(new Coordinate(point.x, point.y)), _transform);
            target = JTS.transform(geom, _transform);
        } catch (TransformException e) {
            throw new RuntimeException(e);
        }

        return center.distance(target) <= distance;
    }

    // member
    public List<Road> _roads = new ArrayList<>();
    public List<Amenity> _amenities = new ArrayList<>();
    public List<MapObject> _others = new ArrayList<>();

    private MathTransform _transform = null;
}
