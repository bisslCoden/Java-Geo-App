package at.tugraz.oop2;

import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.Server;


import java.util.logging.Logger;

public class MapServiceServer {
    private static final Logger logger = Logger.getLogger(MapServiceServer.class.getName());
    private Server server;

    public static void main(String[] args) {
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
            System.out.println(e);
            port = 8020;
        }
        //server = Grpc.newServerBuilderForPort(port, InsecureServerCredentials.create())
        //        .addService(new mapserviceImpl());
        MapLogger.backendStartup(port, data_path);
        logger.info("ended here");
    }
}



//}