package at.tugraz.oop2;
;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;

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
    static private class Node extends Base {
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
        List<Node> nodes = new ArrayList<>();
        List<Relation> relations = new ArrayList<>();

        parse(location, ways, nodes, relations);

        List<Road> roads = new ArrayList<>();
        List<Amenity> amenities = new ArrayList<>();
        List<MapObject> others = new ArrayList<>();

        assemble(ways, nodes, relations, roads, amenities, others);

        return new MapData(roads, amenities, others);
    }

    private static void parse(String location, List<Way> ways, List<Node> nodes, List<Relation> relations) {
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
                    Node node = new Node();
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
    private static void assemble(List<Way> ways, List<Node> nodes, List<Relation> relations, List<Road> roads, List<Amenity> amenities, List<MapObject> others) {//        // find amenities in ways
        System.out.print("[MapLoader]: started assembly...");
        for(Way way : ways) {
            if (way.tags.containsKey("amenity"))
                amenities.add(constructAmenity(way, ways, nodes, relations));
            else if (way.tags.containsKey("highway")) {
                Road road = constructRoad(way, ways, nodes, relations);
                if (road.geom != null)
                    roads.add(road);
            }
            else
                others.add(constructMapObject(way, ways, nodes, relations));

        }

        // find amenities in nodes
        for(Node node : nodes) {
            if (node.tags.containsKey("amenity"))
                amenities.add(constructAmenity(node, ways, nodes, relations));
            else
                others.add(constructMapObject(node, ways, nodes, relations));
        }

        // find amenities in relations
        for(Relation relation : relations) {
            if(relation.tags.containsKey("amenity"))
                amenities.add(constructAmenity(relation, ways, nodes, relations));
            else
                others.add(constructMapObject(relation, ways, nodes, relations));
        }

        System.out.println(" Done!");
    }

    //TODO: implement in templates to avoid code duplication
    private static Amenity constructAmenity(Way way, List<Way> ways, List<Node> nodes, List<Relation> relations) {
        return new Amenity(
                way.id,
                way.tags.getOrDefault("name", "unknown"),
                way.tags,
                way.tags.getOrDefault("amenity", "invalid"),
                constructGeometry(way, ways, nodes, relations)
        );
    }
    private static Amenity constructAmenity(Node node, List<Way> ways, List<Node> nodes, List<Relation> relations) {
        return new Amenity(
                node.id,
                node.tags.getOrDefault("name", "unknown"),
                node.tags,
                node.tags.getOrDefault("amenity", "invalid"),
                constructGeometry(node, ways, nodes, relations)
        );
    }
    private static Amenity constructAmenity(Relation relation, List<Way> ways, List<Node> nodes, List<Relation> relations) {
        return new Amenity(
                relation.id,
                relation.tags.getOrDefault("name", "unknown"),
                relation.tags,
                relation.tags.getOrDefault("amenity", "invalid"),
                constructGeometry(relation, ways, nodes, relations)
        );
    }
    private static Geometry constructGeometry(Way way, List<Way> ways, List<Node> nodes, List<Relation> relations) {
        List<Coordinate> coordinates = getCoordinates(way, nodes);
        if(coordinates == null) return null;

        if(coordinates.size() > 2 && coordinates.get(0) == coordinates.get(coordinates.size() - 1)) // create polygon
            return new GeometryFactory().createPolygon(coordinates.toArray(new Coordinate[0]));
        else // create line string
            return new GeometryFactory().createLineString(coordinates.toArray(new Coordinate[0]));
    }
    private static Geometry constructGeometry(Node node, List<Way> ways, List<Node> nodes, List<Relation> relations) {
        return new GeometryFactory().createPoint(new Coordinate(node.longitude, node.latitude));
    }
    private static Geometry constructGeometry(Relation relation, List<Way> ways, List<Node> nodes, List<Relation> relations) {
        List<Geometry> geometries = new ArrayList<>();

        if(relation.tags.containsValue("multipolygon")) { // multipolygon
            List<Geometry> multis = new ArrayList<>();
            List<Polygon> inners = new ArrayList<>();
            Polygon outer = null;

            for(int index = 0; index < relation.members.size();) {
                Circle circle = getNext(index, relation.members, ways, nodes);
                if(circle == null) { ++index; continue; }

                // handle found polygon
                if(circle.role.equals("outer")) {
                    if(outer != null) {
                        inners.add(0, outer);
                        multis.add(new GeometryFactory().createMultiPolygon(inners.toArray(new Polygon[0])));
                        inners.clear();
                    }
                    outer = circle.polygon;
                }
                else if(circle.role.equals("inner")) {
                    inners.add(circle.polygon);
                }
                else { // TODO: don't handle....
                    System.out.println("Something went wrong: Invalid role!");
                    System.exit(-1);
                }
                index = circle.index;
            }

            if(outer != null) {
                inners.add(0, outer);
                multis.add(new GeometryFactory().createMultiPolygon(inners.toArray(new Polygon[0])));
            }

            geometries.addAll(multis);
        }
        else { // geometry collection
            for(Relation.Member member : relation.members) { // construct geometry from way
                if(member.type.equals("way")) {
                    Way item = ways.get(wayLookup.get(member.reference));
                    geometries.add(constructGeometry(item, ways, nodes, relations));
                }
                else if(member.type.equals("relation")) { // recursive generate geom from relation
                    Relation item = relations.get(relationLookup.get(member.reference));
                    geometries.add(constructGeometry(item, ways, nodes, relations));
                }
                else { // invalid state should never be reached
                    System.out.println("Something went wrong!");
                    System.exit(-1);
                }
            }
        }

        return new GeometryFactory().createGeometryCollection(geometries.toArray(new Geometry[0]));
    }

    private static List<Coordinate> getCoordinates(Way way, List<Node> nodes) {
        List<Coordinate> coordinates = new ArrayList<>();

        for(Long reference : way.references) {
            Integer nodeId = nodeLookup.getOrDefault(reference, null);
            if(nodeId == null) return null;
            Node node = nodes.get(nodeId);
            coordinates.add(new Coordinate(node.longitude, node.latitude));
        }

        return coordinates;
    }
    private static List<Coordinate> getCoordinates(List<Long> references, List<Node> nodes) {
        List<Coordinate> coordinates = new ArrayList<>();

        for(Long reference : references) {
            Integer nodeId = nodeLookup.getOrDefault(reference, null);
            if(nodeId == null) return null;
            Node node = nodes.get(nodeId);
            coordinates.add(new Coordinate(node.longitude, node.latitude));
        }

        return coordinates;
    }

    private static Circle getNext(int index, List<Relation.Member> members, List<Way> ways, List<Node> nodes) {
        List<Relation.Member> pool = members.subList(index, members.size());
        Circle circle = new Circle();
        circle.index = index;
        int size = pool.size();

        // set role
        circle.role = pool.get(0).role;

        List<Long> references = new ArrayList<>();
        Integer id = wayLookup.getOrDefault(pool.get(0).reference, null);
        if(id == null) return null;
        references.addAll(ways.get(id).references);

        for(int outer = 0; outer < size; ++outer) {
            for(int inner = pool.size() - 1; inner >= 0; --inner) {
                if(!pool.get(inner).role.equals((circle.role))) continue; // exclude items with wrong role
                if(pool.get(inner).type.equals("relation")) continue; // exclude relations TODO: fix

                // try to attach
                id = wayLookup.getOrDefault(pool.get(inner).reference, null);
                if(id == null) return null;
                Way way = ways.get(id);
                Long con = references.get(references.size() - 1);
                Long beg = way.references.get(0);
                Long end = way.references.get((way.references.size() - 1));

                if(con.equals(beg)) { // normal link
                }
                else if (con.equals(end)){ // reverse link
                    Collections.reverse(way.references);
                }
                else continue;

                // set index
                int newIndex = members.indexOf(pool.get(inner));
                if(newIndex > circle.index) {
                    circle.index = newIndex;
                }

                references.remove(references.size() - 1); // remove doubles
                references.addAll(way.references);
                pool.remove(inner);

                if(references.get(0).equals(references.get(references.size() - 1))) {
                    List<Coordinate> coordinates = getCoordinates(references, nodes);
                    circle.polygon = new GeometryFactory().createPolygon(coordinates.toArray(new Coordinate[0]));
                    return circle;
                }
            }
        }

        return null;
    }

    private static Road constructRoad(Way way, List<Way> ways, List<Node> nodes, List<Relation> relations) {
        Road result = new Road(
                way.id,
                way.tags.get("highway"),
                way.tags,
                "way",
                constructGeometry(way, ways, nodes, relations),
                (ArrayList<Long>)way.references
        );

        return result;
   }


    private static MapObject constructMapObject(Way way, List<Way> ways, List<Node> nodes, List<Relation> relations) {
        MapObject result = new MapObject(
                way.id,
                way.tags.getOrDefault("name", "unknown"),
                way.tags.getOrDefault("highway", "unknown"),
                way.tags,
                constructGeometry(way, ways, nodes, relations)
        );

        return result;
    }
    private static MapObject constructMapObject(Node node, List<Way> ways, List<Node> nodes, List<Relation> relations) {
        MapObject result = new MapObject(
                node.id,
                node.tags.getOrDefault("name", "unknown"),
                node.tags.getOrDefault("highway", "unknown"),
                node.tags,
                constructGeometry(node, ways, nodes, relations)
        );

        return result;
    }
    private static MapObject constructMapObject(Relation relation, List<Way> ways, List<Node> nodes, List<Relation> relations) {
        MapObject result = new MapObject(
                relation.id,
                relation.tags.getOrDefault("name", "unknown"),
                relation.tags.getOrDefault("highway", "unknown"),
                relation.tags,
                constructGeometry(relation, ways, nodes, relations)
        );

        return result;
    }
}
