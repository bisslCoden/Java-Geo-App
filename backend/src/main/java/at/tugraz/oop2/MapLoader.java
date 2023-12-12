package at.tugraz.oop2;

import org.geotools.referencing.operation.matrix.GeneralMatrix;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.lang.annotation.ElementType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public static MapData load(String location) {
        // TODO: implement safety check
        List<Way> ways = new ArrayList<Way>();
        List<Node> nodes = new ArrayList<Node>();
        List<Relation> relations = new ArrayList<Relation>();

        parse(location, ways, nodes, relations);

        List<Road> roads = new ArrayList<Road>();
        List<Amenity> amenities = new ArrayList<Amenity>();

        assemble(ways, nodes, relations, roads, amenities);

        return new MapData(roads, amenities);
    }

    private static void parse(String location, List<Way> ways, List<Node> nodes, List<Relation> relations) {
        System.out.println("started parsing...");
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
                            case "long": node.longitude = Double.parseDouble(attributes.getValue(index)); break;
                            default: // do not handle
                        }
                    }

                    // append new node to list
                    //System.out.println("Adding node: " + node);
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
                    //System.out.println("Adding way: " + way);
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
                    //System.out.println("Adding relation: " + relation);
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
                    //if(key.equals("amenity")) {
                    //    System.out.println("Found amenity: " + value);
                    //}
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
        System.out.println("finished parsing!");
    }
    private static void assemble(List<Way> ways, List<Node> nodes, List<Relation> relations, List<Road> roads, List<Amenity> amenities) {//        // find amenities in ways
        System.out.println("started assembly...");
                for(Way way : ways)
            if(way.tags.containsKey("amenity"))
                amenities.add(constructAmenity(way, "way"));

        // find amenities in nodes
        for(Node node : nodes)
            if(node.tags.containsKey("amenity"))
                amenities.add(constructAmenity(node, "node"));

        // find amenities in relations
        for(Relation relation : relations)
            if(relation.tags.containsKey("amenity"))
                amenities.add(constructAmenity(relation, "relation"));

        // construct roads from ways
        for(Way way : ways)
            roads.add(roadFromWay(way));
        System.out.println("finished assembly!");
    }

    // TODO: generate geom data.
    private static <T extends Base> Amenity constructAmenity(T item, String type) {
        return new Amenity(
                item.tags.get("amenity"),
                item.id,
                item.tags,
                type
        );
    }

    private static Road roadFromWay(Way way) {
        return new Road(
                way.tags.get("amenity"),
                way.id,
                way.tags,
                "way",
                (ArrayList<Long>)way.references
        );
   }

    //private static void geomFromRelation() {}
//    private static void buildMultipolygon() {};
//    private static void buildGeometryCollection() {};
//    private static Geometry extractPolygons(List<Relation.Member> members) {
//        List<Geometry> geometries = new ArrayList<Geometry>();
//        GeometryFactory factory = new GeometryFactory();
//
//        for(int index = 0; index < members.size(); ++index) {
//
//        }
//
//        return factory.createGeometryCollection(geometries.toArray(new Geometry[0]));
//    }
//
//    private static GeometryCollection buildGeom(Relation relation, Map<String, String> tags) {
//        if(!relation.tags.containsValue("multipolygon")) return buildGeometryCollection(relation);
//
//        return null;
//    }
//
}
