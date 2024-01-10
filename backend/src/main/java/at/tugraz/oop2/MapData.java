package at.tugraz.oop2;

import org.apache.commons.lang3.ObjectUtils;
import org.geotools.graph.build.GraphBuilder;
import org.geotools.graph.build.GraphGenerator;
import org.geotools.graph.build.basic.BasicGraphBuilder;
import org.geotools.graph.build.basic.BasicGraphGenerator;
import org.geotools.graph.build.line.BasicLineGraphGenerator;
import org.geotools.graph.build.line.LineGraphGenerator;
import org.geotools.graph.structure.Graph;
import org.geotools.graph.structure.Node;
import org.geotools.graph.structure.line.XYNode;
import org.hsqldb.lib.HsqlArrayHeap;
import org.locationtech.jts.geom.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.FactoryException;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.springframework.aop.target.HotSwappableTargetSource;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D;

import java.util.*;
import java.util.Map;

public class MapData {
    public MapData() {}
    public MapData(List<Road> roads, List<Amenity> amenities, List<MapObject> others, Graph network) {
        this._roads = roads;
        this._amenities = amenities;
        this._others = others;
        this._network = network;

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
        MapLogger.backendLogAmenityRequest((int)(long)id);
        for(Amenity amenity : _amenities)
        {
            if(amenity.id == id) return amenity;
        }
        return null;
    }
    public Amenity[] getAmenities(Rectangle2D.Double frame, String type, Long skip, Long take, Long[] total) {
        MapLogger.backendLogAmenitiesRequest();
        List<Amenity> result = new ArrayList<Amenity>();

        total[0] = 0L;
        int skipped = 0, took = 0;
        for(Amenity amenity : _amenities) {
            // filter
            if(!type.isEmpty() && !amenity.type.equals(type)) continue;
            if(!isInside(frame, amenity.geom)) continue;
            ++total[0];

            // skip
            if(skipped < skip) { ++skipped; continue; }

            // take
            if(took < take) {
                result.add(amenity);
                ++took;
            }
        }

        return result.toArray(new Amenity[0]);
    }
    public Amenity[] getAmenities(Point2D.Double point, Double distance, String type, Long skip, Long take, Long[] total) {
        MapLogger.backendLogAmenitiesRequest();
        List<Amenity> result = new ArrayList<>();

        total[0] = 0L;
        int skipped = 0, took = 0;
        for(Amenity amenity : _amenities) {
            // filter
            if(!type.isEmpty() && !amenity.type.equals(type)) continue;
            if(!isInside(point, distance, amenity.geom)) continue;
            ++total[0];

            // skip
            if(skipped < skip) { ++skipped; continue; }

            // take
            if(took < take) {
                result.add(amenity);
                ++took;
            }
        }

        return result.toArray(new Amenity[0]);
    }

    public Road getRoad(Long id) {
        MapLogger.backendLogRoadRequest((int)(long)id);
        for(Road road : _roads)
            if(road.id == id) return road;
        return null;
    }
    public Road[] getRoads(Rectangle2D.Double frame, String type, Long skip, Long take, Long[] total) {
        MapLogger.backendLogRoadsRequest();
        List<Road> result = new ArrayList<Road>();

        total[0] = 0L;
        int skipped = 0, took = 0;
        for(Road road : _roads) {
            // filter
            if(!type.isEmpty() && !road.type.equals(type)) continue;
            if(!isInside(frame, road.geom)) continue;
            ++total[0];

            // skip
            if(skipped < skip) { ++skipped; continue;}

            // take
            if(took < take) {
                result.add(road);
                ++took;
            }
        }

        return result.toArray(new Road[0]);
    }

    public Route getRoute(Long from, Long to, boolean weighting) {
        // TODO: implement
        Road[] resp = new Road[1];
        resp[0] = _roads.get(0);
        return new Route(200.0, 200.0, resp);
    }

    public Usages getUsage(Rectangle2D.Double frame) {
        // transform frame
        Geometry frame_geom = new GeometryFactory().createPolygon(new Coordinate[] {
                new Coordinate(frame.x, frame.y),
                new Coordinate(frame.x + frame.width, frame.y),
                new Coordinate(frame.x + frame.width, frame.y + frame.height),
                new Coordinate(frame.x, frame.y + frame.height),
                new Coordinate(frame.x, frame.y),
        });
        try {
            frame_geom = JTS.transform(frame_geom, _transform);
        } catch (TransformException e) {
            throw new RuntimeException(e);
        }

        // calculate areas
        HashMap<String, Double> shares = new HashMap<>();
        Double usage = frame_geom.getArea();
        List<MapObject> objects = new ArrayList<>();

        for(Amenity amenity : _amenities) {
            if(!amenity.tags.containsKey("landuse")) continue;
            if(!isInside(frame, amenity.geom)) continue;
            objects.add(amenity);
        }
        for(Road road : _roads) {
            if(!road.tags.containsKey("landuse")) continue;
            if(!isInside(frame, road.geom)) continue;
            objects.add(road);
        }
        for(MapObject other : _others) {
            if(!other.tags.containsKey("landuse")) continue;
            if(!isInside(frame, other.geom)) continue;
            objects.add(other);
        }

        for(MapObject object : objects) {
            // get or create area entry in map
            String key = object.tags.get("landuse");
            Double value = shares.getOrDefault(key, 0.0);

            // get area
            value += getArea(object.geom, frame);
            shares.put(key, value);
        }

        // convert shares to usage array
        ArrayList<Usage> usages = new ArrayList<>();
        for(HashMap.Entry<String, Double> entry : shares.entrySet()) {
            String type = entry.getKey();
            Double area = entry.getValue();
            Double share = area / usage;
            usages.add(new Usage(type, share, area));
        }
        Collections.sort(usages, new Comparator<Usage>() {
            @Override
            public int compare(Usage u1, Usage u2) {
                return Double.compare(u1.share, u2.share);
            }
        });

        if(usages.size() == 0) return null;
        return new Usages(usage, usages.toArray(new Usage[0]));
    }


    private double getArea(Geometry geom, Rectangle2D.Double frame) {
        try {
            // check bounding box
            Geometry target = new GeometryFactory().createGeometry(geom);
            Envelope bbox = new Envelope(frame.x, frame.x + frame.width, frame.y, frame.y + frame.height);
            target = JTS.toGeometry(bbox).intersection(target);

            // transform geometry to target
            try {
                target = JTS.transform(target, _transform);
            } catch (TransformException e) {
                throw new RuntimeException(e);
            }
            // search in map objects
            return target.getArea();
        }
        catch(Exception e) {
            System.out.println("Failed!");
            return 0.0;
        }
    }


    public boolean isInside(Rectangle2D.Double frame, Geometry geom) { // TODO: refactor
        Geometry boundingBox = new GeometryFactory().createPolygon(new Coordinate[] {
                new Coordinate(frame.x, frame.y),
                new Coordinate(frame.x + frame.width, frame.y),
                new Coordinate(frame.x + frame.width, frame.y + frame.height),
                new Coordinate(frame.x, frame.y + frame.height),
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

    private Graph _network = null;

    private MathTransform _transform = null;
}
