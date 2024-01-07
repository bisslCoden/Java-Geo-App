package at.tugraz.oop2;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.Server;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.geojson.GeoJsonWriter;


import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;

public class MapServiceServer {
    public static final Logger logger = Logger.getLogger(MapServiceServer.class.getName());
    private Server server;

    public static void main(String[] args) throws IOException, InterruptedException {
        logger.info("Starting backend...");
        var backend_port = System.getenv().getOrDefault("JMAP_BACKEND_PORT", "8020");
        var data_path = System.getenv().getOrDefault("JMAP_BACKEND_OSMFILE", "data/styria_reduced.osm");
        int port;

        try {
            port = Integer.parseInt(backend_port);
            if (port < 0 || port > 65535)
                throw  new Exception("port not right!");
        }catch (Exception e)
        {
            logger.info("There was a Problem with port parsing. Reverting to default...");
            port = 8020;
        }

        //Getting the backend server Rolling :D
        try {
            MapLogger.backendStartup(port, data_path);
            final MapServiceServer server = new MapServiceServer();
            Map.getInstance().load(data_path);

            server.start(port);
        }
        catch (Exception e){
            logger.info("FATAL: Something went wrong in booting up the Server or loading the Dataset! "
                    + e.getMessage());
        }

        logger.info("Stoping backend...");
    }

    //------------------------------------------------------------------------------------------------------------------
    // Setting up the gRPC channel
    // @param port the gRPC NETWORK port to connect to the Middleware
    //------------------------------------------------------------------------------------------------------------------
    private void start(int port) throws InterruptedException, IOException{
        server = Grpc.newServerBuilderForPort(port, InsecureServerCredentials.create())
                .addService(new MapServiceImpl())
                .build()
                .start();
        logger.info("Server started successfully!");
        server.awaitTermination();
    }
}

