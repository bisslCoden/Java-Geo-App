package at.tugraz.oop2;

import io.grpc.Channel;
import io.grpc.Grpc;
import mapserviceGRPC.MapObject;
import mapserviceGRPC.req_ID;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import mapserviceGRPC.mapserviceGrpc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.awt.geom.Point2D;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

@SpringBootApplication
public class MapApplication {
    private static mapserviceGrpc.mapserviceBlockingStub blockingStub;

    public static void main(String[] args) {
        var serverport = System.getenv().getOrDefault("JMAP_MIDDLEWARE_PORT", "8010");
        var backend = System.getenv().getOrDefault("JMAP_BACKEND_TARGET", "localhost:8020");
        int port;
        //int backend_port;
        try {
             port = Integer.parseInt(serverport);
             if(port < 0 || port > 65535)
                 throw new Exception("Invalid range");
        }catch (Exception e)
        {
            System.out.println(e);
            port = 8010;
            //backend = "8020";
        }

        MapLogger.middlewareStartup(port, backend);
        String backend_port = "localhost:8020";
        ManagedChannel chann = Grpc.newChannelBuilder(backend_port, InsecureChannelCredentials.create())
                .build();
        create_backend_conn(chann);
        req_ID request = req_ID.newBuilder()
                .setID(12)
                .build();
        MapObject response;
        response = blockingStub.getObjID(request);

        System.out.println(response.getName());
        System.out.println("Serverport is" + serverport);
        var app = new SpringApplication((MapApplication.class));
        app.setDefaultProperties(Collections.singletonMap("server.port", port));

        //create samples with dummy data
        app.run();
    }
    private static void create_backend_conn(Channel channel)
    {
        blockingStub = mapserviceGrpc.newBlockingStub(channel);
    }
}