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
        crsK crs1 = new crsK();
        Geometry geo1 = new Geometry("Point", new ArrayList<>(), crs1);
        double[] coord0 = {2.3423342, 4.45345};
        geo1.coordinates.add(coord0);
        geo1.crs.properties.put("name", "EPSG:0");
        Amenitiy amenity1 = new Amenitiy("Athen", 291464594, geo1, new HashMap<>(),"restaurant");
        amenity1.tags.put("addr:country", "AT");
        amenity1.tags.put("cuisine","greek");

        Geometry geo2 = new Geometry("Point", new ArrayList<>(), crs1);
        double[] coord1 = {12.03294,14.4535};
        geo2.coordinates.add(coord1);
        Amenitiy amenity2 = new Amenitiy("Dim Sum", 29328232, geo2, new HashMap<>(),"restaurant");
        amenity2.tags.put("name","Dim Sum");
        amenity2.tags.put("cuisine","asian");

        Geometry geo3 = new Geometry("LineString", new ArrayList<>(), crs1);
        double[] coord2 = {15.2345,32.43535};
        double[] coord3 = {23.435,34.54635};
        double[] coord4 = {55.435,76.55635};

        geo3.coordinates.add(coord2);
        geo3.coordinates.add(coord3);
        geo3.coordinates.add(coord4);
        ArrayList<Long> child_id = new ArrayList<>();
        child_id.add(1231313L);
        child_id.add(234242343L);
        Road road1 = new Road("Sandgasse", 32685265L, geo3, new HashMap<>(), "residential", child_id);
        road1.tags.put("width","6.5");
        road1.tags.put("type", "residential");

        Geometry geo4 = new Geometry("LineString", new ArrayList<>(), crs1);
        double[] coord5 = {23.345345,56.3535};
        double[] coord6 = {22.33345,1.3535};
        double[] coord7 = {4.3445,10.354335};
        geo4.coordinates.add(coord5);
        geo4.coordinates.add(coord6);
        geo4.coordinates.add(coord7);

        ArrayList<Long> child_id2 = new ArrayList<>();
        child_id2.add(6232342L);
        child_id2.add(734231L);
        Road road2 = new Road("Süd Autobahn", 234234532, geo4,new HashMap<>(),"motorway",child_id2);
        road2.tags.put("lit","yes");


        ObjectList list = ObjectList.getInstance();
        list.addObj(amenity1);
        list.addObj(amenity2);
        list.addObj(road1);
        list.addObj(road2);
        app.run();
    }
}