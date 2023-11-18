package at.tugraz.oop2;


import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;
import org.locationtech.jts.geom.Coordinate;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
@Data
public class MapObject {
    String name;
    long id;
    Geometry geom = new Geometry();
    Map<String, String> tags;
    String type;
    public MapObject(){}

    class Geometry {
        String type;
        ArrayList<double[]> coordinates = new ArrayList<>();
        Crs crs = new Crs();
        public Geometry(){}
        class Crs {
            String type;
            Map<String, String> properties = new HashMap<>();
            public Crs(){}
        }


        Geometry(String type, ArrayList<double[]> coords, Crs crs){
            this.type = type;
            this.coordinates = coords;
            this.crs = crs;
        }


    }


}





