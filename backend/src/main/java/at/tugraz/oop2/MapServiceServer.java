package at.tugraz.oop2;

import java.util.logging.Logger;

public class MapServiceServer {
    private static final Logger logger = Logger.getLogger(MapServiceServer.class.getName());

    public static void main(String[] args) {
        logger.info("Starting backend...");

        // test code
        Map.getInstance().load("data/styria_reduced.osm");
        Amenity amenity = Map.getInstance().getAmenity((long)483646800);
        System.out.println("Found amenity: ");
        System.out.println("name: " + amenity.name);
        System.out.println("tags: " + amenity.tags.size());
        System.out.println("id: " + amenity.id);
        System.out.println(("type: " + amenity.type));

        // test code
        Road road = Map.getInstance().getRoad((long)483646800);
        System.out.println("Found road: ");
        System.out.println("name: " + road.name);
        System.out.println("tags: " + road.tags.size());
        System.out.println("id: " + road.id);
        System.out.println(("type: " + road.type));
        System.out.println(("childs: " + road.child_ids.size()));

        logger.info("Stoping backend...");
    }
}