package at.tugraz.oop2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.awt.geom.Point2D;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

@SpringBootApplication
public class MapApplication {
    public static void main(String[] args) {
        var serverport = System.getenv().getOrDefault("JMAP_MIDDLEWARE_PORT", "8010");
        var backend = System.getenv().getOrDefault("JMAP_BACKEND_TARGET", "localhost:8020");
        int port;
        //int backend_port;
        try {
             port = Integer.parseInt(serverport);
             //backend_port = Integer.parseInt(backend);
             if(port < 0 || port > 65535)
                 throw new Exception("Invalid range");
             //if(backend_port < 0 || backend_port > 65535)
             //    throw  new Exception("Invalid range");
        }catch (Exception e)
        {
            System.out.println(e);
            port = 8010;
            //backend = "8020";
        }

        MapLogger.middlewareStartup(port, backend);
        
        //System.out.println("Serverport is" + serverport);
        var app = new SpringApplication((MapApplication.class));
        app.setDefaultProperties(Collections.singletonMap("server.port", port));

        //create samples with dummy data
        app.run();
    }
}