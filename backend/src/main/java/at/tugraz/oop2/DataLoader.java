package at.tugraz.oop2;

import javax.swing.*;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.w3c.dom.Attr;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class DataLoader {
    // TODO: migrate class to not being static
    private static final Logger logger = Logger.getLogger(MapServiceServer.class.getName());

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

    static List<Relation> relations = new ArrayList<Relation>();
    static List<Node> nodes = new ArrayList<Node>();
    static List<Way> ways = new ArrayList<Way>();


    public static void Load(String location) {
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
                    element = ElementType.NONE; // reset the element type
                    return; // nothing more to do here!
                }

                // handlers
                public static void handleNode(Attributes attributes) {
                    // create new node object
                    Node node = new Node();

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
                    nodes.add(node);
                }
                public static void handleWay(Attributes attributes) {
                    // create new way object
                    Way way = new Way();

                    // parse attributes
                    for(int index = 0; index < attributes.getLength(); ++index) {
                        switch (attributes.getLocalName(index)) {
                            case "id": way.id = Long.parseLong(attributes.getValue(index)); break;
                            default: // do not handle
                        }
                    }

                    // append way to list
                    ways.add(way);
                }
                public static void handleRelation(Attributes attributes) {
                    // create new relation object
                    Relation relation = new Relation();

                    // parse attributes
                    for(int index = 0; index < attributes.getLength(); ++index) {
                        switch (attributes.getLocalName(index)) {
                            case "id": relation.id = Long.parseLong(attributes.getValue(index)); break;
                            default: // do not handle
                        }
                    }

                    // append relation to list
                    relations.add(relation);
                }
                public static void handleNd(Attributes attributes) {
                    // get values
                    // TODO: handle edge-cases (attribute not existing => do not process)
                    Long reference = Long.parseLong(attributes.getValue(attributes.getIndex("ref")));

                    // insert values
                    // TODO: fix bug (ways cannot be empty on first nd element - might happen in invalid files)
                    ways.get(ways.size() - 1).references.add(reference);
                }
                public static void handleTag(Attributes attributes) {
                    // get values
                    // TODO: handle edgecases (attribute not existing => do not process)
                    String key = attributes.getValue(attributes.getIndex("k"));
                    String value = attributes.getValue((attributes.getIndex("v")));

                    // insert values
                    switch (element) {
                        case NODE: nodes.get(nodes.size() - 1).tags.put(key, value); break;
                        case WAY: ways.get(ways.size() - 1) .tags.put(key, value); break;
                        case RELATION: relations.get(relations.size() - 1).tags.put(key, value); break;
                        default: // do not handle
                    }
                }
                public static void handleMember(Attributes attributes) {
                    // get values
                    // TODO: handle edge-cases (attribute not existing => do not process)
                    String type = attributes.getValue(attributes.getIndex("type"));
                    String role = attributes.getValue(attributes.getIndex("role"));
                    Long value = Long.parseLong((attributes.getValue(attributes.getIndex("ref"))));

                    // insert values
                    // TODO: solve in a better way
                    if (type == "way") {
                        if(role == "outer") relations.get((relations.size() - 1)).outer_ways.add(value);
                        else if(role == "inner") relations.get((relations.size() - 1)).inner_ways.add(value);
                        // TODO: there might be other types to be handled
                    }
                    else if (type == "relation") {
                        if(role == "outer") relations.get((relations.size() - 1)).outer_relations.add(value);
                        else if(role == "inner") relations.get((relations.size() - 1)).inner_relations.add(value);
                        // TODO: there might be other types to be handled
                    }
                    else {
                        // TODO: there might be other types to be handled
                        // do not handle yet
                    }
                }
            };

            // parse the file
            parser.parse(location, handler);

            // calculate further stuff
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}

/*
def buildGeom(members:list, tags: dict{str,str}) -> GeometryCollection:
  if "multipolygon" in tags:
    multi_polygons=[]
    inners=[]
    outer=None
    for(i=0; i<members.lenght;):
      closed_circle=getNextClosed(i, members)
      # this iterates over the next memebers and returns a polygon
      # if it is able to find a combination of coordinated in the next
      # (one or more) *same* role-types,
      # i.e. a closed ring of only outer or inner line segments
      if closed_circle:
        if closed_circle.last_role=="outer":
          if outer:
            multi_polygons+=[buildMultipolygon([outer, ...inners])] #multipolygons usually have the first ring as the outer one
          outer=closed_circle.polygon

        elif closed_circle.last_role=="inner":
          inners+=[closed_circle.polygon]

        i=closed_circle.last_member_idx+1
      else
        raise error
    # if there is still a an outer ring pick it up and add it
    if outer:
      multi_polygons+=[buildMultipolygon([outer, ...inners])]
    return buildGeometrycollection(multi_polygons)
  else:
    return buildGeometrycollection(members)
    # just multiple geometries
*/