package at.tugraz.oop2;

import java.util.logging.Logger;

public class MapServiceServer {
    private static final Logger logger = Logger.getLogger(MapServiceServer.class.getName());
 
    public static void main(String[] args) {
        logger.info("Starting backend...");
        DataLoader.Load("data/styria_reduced.osm");
        logger.info("Stoping backend...");
    }
}