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
    Map<String, String> tags = new HashMap<>();
    String type;
    public MapObject(){}
    public void setGeo(String type, ArrayList<Point2D.Double> coords, String Crstype, Map<String, String> Crsprop)
    {
        this.geom.type = type;
        this.geom.coordinates = coords;
        this.geom.crs.type = Crstype;
        this.geom.crs.properties = Crsprop;
    }
    class Geometry {
        String type;
        ArrayList<Point2D.Double> coordinates = new ArrayList<>();
        Crs crs = new Crs();
        public Geometry(){}
        class Crs {
            String type;
            Map<String, String> properties = new HashMap<>();
            public Crs(){}
        }
    }


}





