package at.tugraz.oop2;
import org.geotools.geometry.jts.JTS;
import org.geotools.graph.build.GraphBuilder;
import org.geotools.graph.build.basic.BasicGraphBuilder;

import org.geotools.graph.structure.Graph;
//import org.geotools.graph.structure.Node;
import org.geotools.graph.structure.Node;
import org.geotools.graph.structure.basic.BasicEdge;
import org.geotools.graph.structure.line.BasicXYNode;
import org.geotools.referencing.CRS;
import org.geotools.referencing.GeodeticCalculator;
import org.locationtech.jts.geom.*;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;

import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;

import java.util.Map;
import java.util.*;

public class MapLoader {
    // types
    static private class Base {
        Long id;
        Map<String, String> tags = new HashMap<String, String>();
    };
    static private class RawNode extends Base {
        Double longitude; // x-coordinate
        Double latitude;  // y-coordinate

    };
    static private class Way extends Base {
        List<Long> references = new ArrayList<Long>();
    };
    static private class Relation extends Base {
        public static class Member {
            String type;
            String role;
            Long reference;
        }
        List<Member> members = new ArrayList<Member>();
    };
    static private class Circle {
        Polygon polygon;
        Integer index;
        String role;
    }
    static private HashMap<Long, Integer> wayLookup = new HashMap<>();
    static private HashMap<Long, Integer> nodeLookup = new HashMap<>();
    static private HashMap<Long, Integer> relationLookup = new HashMap<>();

    public static MapData load(String location) { // TODO: implement safety check
        List<Way> ways = new ArrayList<>();
        List<RawNode> nodes = new ArrayList<>();
        List<Relation> relations = new ArrayList<>();

        parse(location, ways, nodes, relations);

        List<Road> roads = new ArrayList<>();
        List<Amenity> amenities = new ArrayList<>();
        List<MapObject> others = new ArrayList<>();

        assemble(ways, nodes, relations, roads, amenities, others);

        Graph network = null;
        network = link(roads, nodes);

        Set<Long> used_nodes = new HashSet<>();
        for(Way way : ways) {
            for(Long ref : way.references) {
                if (nodeLookup.getOrDefault(ref, null) == null) continue;
                used_nodes.add(ref);
            }
        }

        Set<Long> used_ways = new HashSet<>();
        for(Relation relation : relations)
            for(Relation.Member member : relation.members) {
                if(wayLookup.getOrDefault(member.reference, null) == null) continue;
                used_ways.add(member.reference);
            }

        System.out.println("Loaded: " + (nodes.size() - used_nodes.size()) + " nodes, " + (ways.size() - used_ways.size()) + " ways, " + relations.size() + " relations");
        // 1 missing node, 4 missing ways....
        MapLogger.backendLoadFinished(nodes.size() - used_nodes.size(), ways.size() - used_ways.size(), relations.size());
        return new MapData(roads, amenities, others, network);
    }

    private static void parse(String location, List<Way> ways, List<RawNode> nodes, List<Relation> relations) {
        System.out.print("[MapLoader]: started parsing...");
        try {
            // setups
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            DefaultHandler handler = new DefaultHandler() {
                // types
                enum ElementType {
                    NONE,
                    NODE,
                    WAY,
                    RELATION,
                };

                // variables
                static ElementType element = ElementType.NONE;

                // event handlers
                // element begin
                public void startElement(String location, String lName, String qName, Attributes attributes) throws SAXException {
                    switch(qName) {
                        case "relation": handleRelation(attributes); break;
                        case "node": handleNode(attributes); break;
                        case "way": handleWay(attributes); break;
                        case "nd": handleNd(attributes);  break;
                        case "tag": handleTag(attributes); break;
                        case "member": handleMember(attributes); break;
                        default: // do not handle
                    }
                }

                // element data
                public void characters(char data[], int start, int length) throws SAXException {
                    return; // nothing to do here!
                }

                // element end
                public void endElement(String location, String lName, String qName) {
                    switch(qName) {
                        case "relation":
                        case "node":
                        case "way":
                            element = ElementType.NONE; // reset the element type
                            break;
                        default: // do not handle
                    }
                    return; // nothing more to do here!
                }

                // handlers
                public void handleNode(Attributes attributes) {
                    // create new node object
                    RawNode node = new RawNode();
                    element = ElementType.NODE;
                    // parse attributes
                    for(int index = 0; index < attributes.getLength(); ++index) {
                        switch (attributes.getLocalName(index))  {
                            case "id": node.id = Long.parseLong(attributes.getValue(index)); break;
                            case "lat": node.latitude = Double.parseDouble(attributes.getValue(index)); break;
                            case "lon": node.longitude = Double.parseDouble(attributes.getValue(index)); break;
                            default: // do not handle
                        }
                    }

                    // append new node to list
                    nodeLookup.put(node.id, nodes.size());
                    nodes.add(node);
                }
                public void handleWay(Attributes attributes) {
                    // create new way object
                    Way way = new Way();
                    element = ElementType.WAY;
                    // parse attributes
                    for(int index = 0; index < attributes.getLength(); ++index) {
                        switch (attributes.getLocalName(index)) {
                            case "id": way.id = Long.parseLong(attributes.getValue(index)); break;
                            default: // do not handle
                        }
                    }

                    // append way to list
                    wayLookup.put(way.id, ways.size());
                    ways.add(way);
                }
                public void handleRelation(Attributes attributes) {
                    // create new relation object
                    Relation relation = new Relation();
                    element = ElementType.RELATION;
                    // parse attributes
                    for(int index = 0; index < attributes.getLength(); ++index) {
                        switch (attributes.getLocalName(index)) {
                            case "id": relation.id = Long.parseLong(attributes.getValue(index)); break;
                            default: // do not handle
                        }
                    }

                    // append relation to list
                    relationLookup.put(relation.id, relations.size());
                    relations.add(relation);
                }
                public void handleNd(Attributes attributes) {
                    // get values
                    // TODO: handle edge-cases (attribute not existing => do not process)
                    Long reference = Long.parseLong(attributes.getValue(attributes.getIndex("ref")));

                    // insert values
                    // TODO: fix bug (ways cannot be empty on first nd element - might happen in invalid files)
                    ways.get(ways.size() - 1).references.add(reference);
                }
                public void handleTag(Attributes attributes) {
                    // get values
                    // TODO: handle edgecases (attribute not existing => do not process)
                    String key = attributes.getValue(attributes.getIndex("k"));
                    String value = attributes.getValue((attributes.getIndex("v")));

                    // insert values
                    switch (element) {
                        case NODE: nodes.get(nodes.size() - 1).tags.put(key, value); break;
                        case WAY: ways.get(ways.size() - 1) .tags.put(key, value); break;
                        case RELATION: relations.get(relations.size() - 1).tags.put(key, value); break;
                        default: System.out.println("Ran into default!");// do not handle
                    }
                }
                public void handleMember(Attributes attributes) {
                    // get values
                    // TODO: handle edge-cases (attribute not existing => do not process)
                    Relation.Member member = new Relation.Member();
                    member.type = attributes.getValue(attributes.getIndex("type"));
                    member.role = attributes.getValue(attributes.getIndex("role"));
                    member.reference = Long.parseLong((attributes.getValue(attributes.getIndex("ref"))));

                    relations.get(relations.size() - 1).members.add(member);
                }
            };

            // parse the file
            parser.parse(location, handler);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(" Done!");
    }
    private static void assemble(List<Way> ways, List<RawNode> nodes, List<Relation> relations, List<Road> roads, List<Amenity> amenities, List<MapObject> others) {//        // find amenities in ways
        System.out.print("[MapLoader]: started assembly...");

        // filter nodes
        for(RawNode node : nodes) {
            if(node.tags.containsKey("amenity")) { // treat node as amenity
                Amenity amenity = constructAmenity(node, ways, nodes, relations);
                if(amenity.geom != null) amenities.add(amenity);
            }
            else if(node.tags.containsKey("highway")) { // treat node as road
                Road road = constructRoad(node, ways, nodes, relations);
                if(road.geom != null) roads.add(road);
            }
            else { // treat node as other
                MapObject other = constructMapObject(node, ways, nodes, relations);
                if(other.geom != null) others.add(other);
            }
        }

        // filter ways
        for(Way way : ways) {
            if(way.tags.containsKey("amenity")) { // treat node as amenity
                Amenity amenity = constructAmenity(way, ways, nodes, relations);
                if(amenity.geom != null) amenities.add(amenity);
            }
            else if(way.tags.containsKey("highway")) { // treat node as road
                Road road = constructRoad(way, ways, nodes, relations);
                if(road.geom != null) roads.add(road);
            }
            else { // treat node as other
                MapObject other = constructMapObject(way, ways, nodes, relations);
                if(other.geom != null) others.add(other);
            }
        }

        // filter relations
        for(Relation relation : relations) {
            if(relation.tags.containsKey("amenity")) { // treat node as amenity
                Amenity amenity = constructAmenity(relation, ways, nodes, relations);
                if(amenity.geom != null) amenities.add(amenity);
            }
            else if(relation.tags.containsKey("highway")) { // treat node as road
                Road road = constructRoad(relation, ways, nodes, relations);
                if(road.geom != null) roads.add(road);
            }
            else { // treat node as other
                MapObject other = constructMapObject(relation, ways, nodes, relations);
                if(other.geom != null) others.add(other);
            }
        }

        System.out.println(" Done!");
    }

    //TODO: implement in templates to avoid code duplication
    private static Amenity constructAmenity(Way way, List<Way> ways, List<RawNode> nodes, List<Relation> relations) {
        return new Amenity(
                way.id,
                way.tags.getOrDefault("name", ""),
                way.tags,
                way.tags.getOrDefault("amenity", ""),
                constructGeometry(way, ways, nodes, relations)
        );
    }
    private static Amenity constructAmenity(RawNode node, List<Way> ways, List<RawNode> nodes, List<Relation> relations) {
        return new Amenity(
                node.id,
                node.tags.getOrDefault("name", ""),
                node.tags,
                node.tags.getOrDefault("amenity", ""),
                constructGeometry(node, ways, nodes, relations)
        );
    }
    private static Amenity constructAmenity(Relation relation, List<Way> ways, List<RawNode> nodes, List<Relation> relations) {
        return new Amenity(
                relation.id,
                relation.tags.getOrDefault("name", ""),
                relation.tags,
                relation.tags.getOrDefault("amenity", ""),
                constructGeometry(relation, ways, nodes, relations)
        );
    }

    private static Road constructRoad(Way way, List<Way> ways, List<RawNode> nodes, List<Relation> relations) {
        return new Road(
                way.id,
                way.tags.getOrDefault("name", ""),
                way.tags,
                way.tags.getOrDefault("highway", ""),
                constructGeometry(way, ways, nodes, relations),
                (ArrayList<Long>)way.references
        );
    }

    private static Road constructRoad(RawNode node, List<Way> ways, List<RawNode> nodes, List<Relation> relations) {
        return new Road(
                node.id,
                node.tags.getOrDefault("name", ""),
                node.tags,
                node.tags.getOrDefault("highway", ""),
                constructGeometry(node, ways, nodes, relations),
                new ArrayList<>()
        );
    };
    private static Road constructRoad(Relation relation, List<Way> ways, List<RawNode> nodes, List<Relation> relations)
    {
        return new Road(
                relation.id,
                relation.tags.getOrDefault("name", ""),
                relation.tags,
                relation.tags.getOrDefault("highway", ""),
                constructGeometry(relation, ways, nodes, relations),
                new ArrayList<>()
        );
    }

    private static MapObject constructMapObject(Way way, List<Way> ways, List<RawNode> nodes, List<Relation> relations) {
        return new MapObject(
                way.id,
                way.tags.getOrDefault("name", ""),
                way.tags.getOrDefault("highway", ""),
                way.tags,
                constructGeometry(way, ways, nodes, relations)
        );
    }
    private static MapObject constructMapObject(RawNode node, List<Way> ways, List<RawNode> nodes, List<Relation> relations) {
        return new MapObject(
                node.id,
                node.tags.getOrDefault("name", ""),
                node.tags.getOrDefault("highway", ""),
                node.tags,
                constructGeometry(node, ways, nodes, relations)
        );
    }
    private static MapObject constructMapObject(Relation relation, List<Way> ways, List<RawNode> nodes, List<Relation> relations) {
        return new MapObject(
                relation.id,
                relation.tags.getOrDefault("name", ""),
                relation.tags.getOrDefault("highway", ""),
                relation.tags,
                constructGeometry(relation, ways, nodes, relations)
        );
    }

    private static Geometry constructGeometry(Way way, List<Way> ways, List<RawNode> nodes, List<Relation> relations) {
        List<Coordinate> coordinates = getCoordinates(way, nodes);
        if(coordinates == null) return null;

        Long first = way.references.get(0);
        Long last = way.references.get(way.references.size() - 1);
        //if(coordinates.size() > 2 && coordinates.get(0) == coordinates.get(coordinates.size() - 1)) // create polygon
        if(way.references.size() > 2 && first.equals(last))
            return new GeometryFactory().createPolygon(coordinates.toArray(new Coordinate[0]));
        else // create line string
            return new GeometryFactory().createLineString(coordinates.toArray(new Coordinate[0]));
    }
    private static Geometry constructGeometry(RawNode node, List<Way> ways, List<RawNode> nodes, List<Relation> relations) {
        return new GeometryFactory().createPoint(new Coordinate(node.longitude, node.latitude));
    }
    private static Geometry constructGeometry(Relation relation, List<Way> ways, List<RawNode> nodes, List<Relation> relations) {
        List<Geometry> geometries = new ArrayList<>();

        if(relation.tags.containsValue("multipolygon")) {
            Polygon outer_polygon = null;
            List<Geometry> multi_polygons = new ArrayList<>();
            List<Geometry> inner_polygons = new ArrayList<>();
            for (int member_index = 0; member_index < relation.members.size(); ) {
                // handle relations
                if (relation.members.get(member_index).type.equals("relation")) {
                    ++member_index;

                    Integer relation_id = relationLookup.getOrDefault(relation.members.get(member_index - 1), null);
                    if (relation_id == null) continue;
                    Geometry collection = constructGeometry(relations.get(relation_id), ways, nodes, relations);
                    for (int collection_index = 0; collection_index < collection.getNumGeometries(); ++collection_index) {
                        geometries.add(collection.getGeometryN(collection_index));
                    }
                }
                // handle ways
                else if (relation.members.get(member_index).type.equals("way")) {
                    // get polygon
                    Circle circle = getNext(member_index, relation.members, ways, nodes);
                    if (circle == null) {
                        return null;
                        //++member_index;
                        //continue;
                    }

                    // handle outer
                    if (circle.role.equals("outer")) {
                        if (outer_polygon != null) {
                            inner_polygons.add(0, outer_polygon);
                            multi_polygons.add(
                                    new GeometryFactory().createMultiPolygon(
                                            inner_polygons.toArray(new Polygon[0])
                                    ));
                            inner_polygons.clear();
                        }
                        outer_polygon = circle.polygon;
                    }
                    // handle inner
                    else if (circle.role.equals("inner")) {
                        inner_polygons.add(circle.polygon);
                    }
                    // handle invalid
                    else {
                        System.out.println("Something went wrong: Invalid member role!");
                        System.exit(0);
                    }

                    // advance
                    member_index = circle.index;
                }
                // handle invalid
                else {
                    System.out.println("Something went wrong: Invalid member type!");
                    System.exit(0);
                }
            }

            if(outer_polygon != null) {
                inner_polygons.add(0, outer_polygon);
                multi_polygons.add(new GeometryFactory().createMultiPolygon(inner_polygons.toArray(new Polygon[0])));
            }

            geometries.addAll(multi_polygons);
        }
        else {
            for(Relation.Member member : relation.members) { // construct geometry from way
                if(member.type.equals("way")) {
                    Integer way_id = wayLookup.getOrDefault(member.reference, null);
                    if(way_id == null) continue; // none existing way
                    geometries.add(constructGeometry(ways.get(way_id), ways, nodes, relations));
                }
                else if(member.type.equals("relation")) { // recursive generate geom from relation
                    Integer relation_id = relationLookup.getOrDefault(member.reference, null);
                    if(relation_id == null) continue; // none existing relation
                    geometries.add(constructGeometry(relations.get(relation_id), ways, nodes, relations));
                }
                else { // invalid state should never be reached
                    System.out.println("Something went wrong: invalid member type!");
                    System.exit(0);
                }
            }
        }

        return new GeometryFactory().createGeometryCollection(geometries.toArray(new Geometry[0]));
    }

    private static List<Coordinate> getCoordinates(Way way, List<RawNode> nodes) {
        List<Coordinate> coordinates = new ArrayList<>();

        for(Long reference : way.references) {
            Integer nodeId = nodeLookup.getOrDefault(reference, null);
            if(nodeId == null) return null;
            RawNode node = nodes.get(nodeId);
            coordinates.add(new Coordinate(node.longitude, node.latitude));
        }

        return coordinates;
    }
    private static List<Coordinate> getCoordinates(List<Long> references, List<RawNode> nodes) {
        List<Coordinate> coordinates = new ArrayList<>();

        for(Long reference : references) {
            Integer nodeId = nodeLookup.getOrDefault(reference, null);
            if(nodeId == null) return null;
            RawNode node = nodes.get(nodeId);
            coordinates.add(new Coordinate(node.longitude, node.latitude));
        }

        return coordinates;
    }

    private static Circle getNext(int index, List<Relation.Member> members, List<Way> ways, List<RawNode> nodes) {
        List<Long> polygon = new ArrayList<>();
        List<Long> used = new ArrayList<>();
        Circle circle = new Circle();

        for(int outer_index = index; outer_index < members.size(); ++outer_index) {
            if(polygon.contains(members.get(outer_index).reference)) continue;
            for (int member_index = index; member_index < members.size(); ++member_index) {
                // get way of member
                if (members.get(member_index).type.equals("relation")) continue; // TODO: handle
                Integer way_id = wayLookup.getOrDefault(members.get(member_index).reference, null);
                if (way_id == null) return null;
                Way way = ways.get(way_id);

                // try to link
                if (polygon.size() == 0) { // attach first element
                    circle.index = member_index;
                    circle.role = members.get(member_index).role;
                } else {
                    Long con = polygon.get(polygon.size() - 1);
                    Long beg = way.references.get(0);
                    Long end = way.references.get(way.references.size() - 1);

                    if (con.equals(beg)) {
                    } // normal link
                    else if (con.equals(end)) { // reverse link
                        Collections.reverse(way.references);
                    } else continue; // cannot link next member
                }

                used.add(members.get(member_index).reference);
                polygon.addAll(way.references);
                if(member_index + 1 > circle.index) circle.index = member_index + 1;

                // check for completed polygon
                Long beg = polygon.get(0);
                Long end = polygon.get(polygon.size() - 1);

                if (beg.equals(end)) {
                    List<Coordinate> coordinates = getCoordinates(polygon, nodes);
                    circle.polygon = new GeometryFactory().createPolygon(coordinates.toArray(new Coordinate[0]));
                    return circle;
                }
            }
        }

        return null;
    }


    private static Graph link(List<Road> roads, List<RawNode> nodes) {
        MathTransform transform = null;
        try {
            CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:4326");
            CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:31256");
            transform = CRS.findMathTransform(sourceCRS, targetCRS, true);
        } catch (FactoryException e) {
            throw new RuntimeException(e);
        }

        GraphBuilder builder = new BasicGraphBuilder();
        HashMap<Long, MapData.builderNode> geo_nodes = new HashMap<>();

        Collection<Node> builder_nodes = builder.getGraph().getNodes();


        for (Road r : roads) {
            if (r.child_ids.size() < 2 || !r.geom.getGeometryType().equals("LineString")) continue;

            MapData.builderNode a = geo_nodes.getOrDefault(r.child_ids.get(0), null);
            if (a == null) {
                a = new MapData.builderNode(r.child_ids.get(0));
                geo_nodes.put(r.child_ids.get(0), a);
                builder.addNode(a);
            }
            MapData.builderNode b = geo_nodes.getOrDefault(r.child_ids.get(r.child_ids.size() - 1), null);;
            if (b == null) {
                b = new MapData.builderNode(r.child_ids.get(r.child_ids.size() - 1));
                geo_nodes.put(r.child_ids.get(r.child_ids.size() - 1), b);
                builder.addNode(b);
            }


            LineString transformed_road = null;

            try {
               transformed_road = (LineString) JTS.transform(r.geom, transform);
            } catch (TransformException e) {
                throw new RuntimeException(e);
            }

            double weight = transformed_road.getLength();

            String speed = r.tags.getOrDefault("maxspeed", "30");
            MapData.builderEdge e = new MapData.builderEdge(r.id, a, b, weight, speed);

            builder.addEdge(e);
        }
        return builder.getGraph();
    }
}
