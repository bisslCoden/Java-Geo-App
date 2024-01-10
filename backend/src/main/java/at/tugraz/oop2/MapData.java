package at.tugraz.oop2;

import org.geotools.graph.build.GraphBuilder;
import org.geotools.graph.build.basic.BasicGraphBuilder;
import org.geotools.graph.path.DijkstraShortestPathFinder;
import org.geotools.graph.path.Path;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Graph;
import org.geotools.graph.structure.Node;

import org.geotools.graph.structure.basic.BasicEdge;
import org.geotools.graph.structure.line.BasicXYNode;
import org.geotools.graph.traverse.standard.DijkstraIterator;
import org.geotools.referencing.GeodeticCalculator;
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

        List<Long> nodes = new ArrayList<>();
        List<Road> roads = new ArrayList<>();

        class builderEdge extends BasicEdge {
            double weight_len = 0;
            double weight_time = 0;

            public builderEdge(Node a, Node b, double w, String speed)
            {
                super(a, b);
                weight_len = w;
                try
                {
                    weight_time = Double.parseDouble(speed);
                }
                catch (NullPointerException e)
                {
                    // key not found
                    weight_time = 30;
                }
                catch (NumberFormatException e)
                {
                    // not a valid speed
                    weight_time = 30;
                }
                weight_len /= weight_time;
            }
            double getWeight(boolean len){
                if(len) return weight_len;
                return weight_time;
            }
        }


        GraphBuilder builder = new BasicGraphBuilder();

        boolean from_found = false;
        boolean to_found = false;
        for(Road r :_roads)
        {
            if(r.child_ids.size() < 2) continue;
            if(Objects.equals(r.child_ids.get(0), from) ||Objects.equals(r.child_ids.get(r.child_ids.size()-1), from))
            {
                from_found = true;
            }
            if(Objects.equals(r.child_ids.get(0), to) ||Objects.equals(r.child_ids.get(r.child_ids.size()-1), to))
            {
                to_found = true;
            }
        }
        if(!(from_found && to_found))
        {
            return null;
        }

        if(Objects.equals(from, to))
        {
            return new Route((double) 0, (double) 0, new Road[0]);
        }

        nodes.add(from);
        Node f = new BasicXYNode();
        try {
            f.setID(Math.toIntExact(from));
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        builder.addNode(f);

        nodes.add(to);
        Node t = new BasicXYNode();
        try {
            t.setID(Math.toIntExact(from));
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        builder.addNode(t);


        List<Long> nodes_slice = nodes.subList(0, 1);
        int last_end = 1;

        Collection<Node> builder_nodes = builder.getGraph().getNodes();

        boolean modification = true;
        while(modification)
        {
            modification = false;
            for(Long n : nodes_slice)
            {
                for(Road r : _roads)
                {
                    if(r.child_ids.size() < 2) continue;
                    if(!Objects.equals(r.type, "highway") || roads.contains(r)) continue;
                    if (Objects.equals(r.child_ids.get(0), n) || Objects.equals(r.child_ids.get(r.child_ids.size()-1), n))
                    {
                        Node a = null;
                        if(!nodes.contains(r.child_ids.get(0)))
                        {
                            nodes.add(r.child_ids.get(0));
                            a = new BasicXYNode();
                            try {
                                a.setID(Math.toIntExact(from));
                            }
                            catch (Exception e)
                            {
                                System.out.println(e.getMessage());
                            }
                            builder.addNode(a);
                        }

                        Node b = null;
                        if(!nodes.contains(r.child_ids.get(r.child_ids.size()-1)))
                        {
                            nodes.add(r.child_ids.get(r.child_ids.size()-1));
                            b = new BasicXYNode();
                            try {
                                b.setID(Math.toIntExact(from));
                            }
                            catch (Exception e)
                            {
                                System.out.println(e.getMessage());
                            }
                            builder.addNode(b);
                        }
                        if((a == null && !builder_nodes.contains(a) || b == null && !builder_nodes.contains(b)))
                        {
                            builder_nodes = builder.getGraph().getNodes();
                        }
                        if(a == null)
                        {
                            for(Node find_node : builder_nodes)
                            {
                                if(Objects.equals(find_node.getID(), Math.toIntExact(r.child_ids.get(0))))
                                {
                                    a = find_node;
                                    break;
                                }
                            }
                        }
                        if(b == null)
                        {
                            for(Node find_node : builder_nodes)
                            {
                                if(Objects.equals(find_node.getID(), Math.toIntExact(r.child_ids.get(r.child_ids.size()-1))))
                                {
                                    b = find_node;
                                    break;
                                }
                            }
                        }

                        GeodeticCalculator calc = new GeodeticCalculator();


                        double weight = 0;
                        Coordinate[] coordinates = r.geom.getCoordinates();
                        Coordinate last = new Coordinate(0, 0);
                        for(Coordinate c : coordinates)
                        {
                            if(last.x == 0 && last.y == 0)
                            {
                                last = c;
                                continue;
                            }

                            calc.setStartingGeographicPoint(last.x, last.y);
                            calc.setDestinationGeographicPoint(c.y, c.y);


                            weight += calc.getOrthodromicDistance();
                            last = c;
                        }
                        String speed = r.tags.get("maxspeed");
                        Edge e = new builderEdge(a, b, weight, speed);
                        try {
                            e.setID(Math.toIntExact(from));
                        }
                        catch (Exception ex)
                        {
                            System.out.println(ex.getMessage());
                        }

                        roads.add(r);
                        builder.addEdge(e);

                        modification = true;
                    }
                }
            }
            nodes_slice = nodes.subList(last_end, nodes.size()-1);
            last_end = nodes.size()-1;
        }

        class edgeWeighter implements DijkstraIterator.EdgeWeighter{

            @Override
            public double getWeight(Edge edge) {
                builderEdge e = (builderEdge) edge;
                return e.getWeight(weighting);
            }
        }

        edgeWeighter weighter = new edgeWeighter();

        DijkstraShortestPathFinder finder = new DijkstraShortestPathFinder(builder.getGraph(), t, weighter);
        Path route = finder.getPath(f);
        if(route == null)
        {
            throw new RuntimeException("400");
        }
        double length = 0;
        double time = 0;
        List<Road> resp = new ArrayList<>();
        for(Edge e : route.getEdges())
        {
            for(Road r : roads)
            {
                if(e.getID() == r.id) resp.add(r);
                length += ((builderEdge) e).weight_len;
                time += ((builderEdge) e).weight_time;

            }
        }

        return new Route(length, time, resp.toArray(new Road[0]));
    }

    public Usages getUsage(Rectangle2D.Double frame) {
        System.out.println("Frame: " + frame.x + ","+frame.y+","+(frame.x+frame.width)+","+(frame.y+frame.height));
        // TODO: fix
        // transform frame
        Geometry frame_geom = new GeometryFactory().createPolygon(new Coordinate[] {
                new Coordinate(frame.x, frame.y),
                new Coordinate(frame.x + frame.width, frame.y),
                new Coordinate(frame.x + frame.width, frame.y + frame.height),
                new Coordinate(frame.x, frame.y + frame.height),
                new Coordinate(frame.x, frame.y),
        });
        Geometry frame_trans = null;
        try {
            frame_trans = JTS.transform(frame_geom, _transform);
        } catch (TransformException e) {
            throw new RuntimeException(e);
        }

        // calculate areas
        HashMap<String, Double> shares = new HashMap<>();
        Double usage = frame_trans.getArea();

        // search in map objects
        for(MapObject other : _others) {
            String key = other.tags.getOrDefault("landuse", null);
            if(key == null) continue;
            if(!isInside(frame, other.geom)) continue;
            Double value = shares.getOrDefault(key, 0.0);

            if(!other.geom.getGeometryType().equals("Polygon")) continue;
            Geometry target = other.geom;
            Envelope bbox = new Envelope(frame.x, frame.x + frame.width, frame.y, frame.y + frame.height);
            target = JTS.toGeometry(bbox).intersection(target);

            try {
                target = JTS.transform(target, _transform);
            } catch (TransformException e) {
                throw new RuntimeException(e);
            }


            Double area = target.getArea();
            value += area;
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
