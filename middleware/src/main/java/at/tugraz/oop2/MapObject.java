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
    Geometry geom;
    Map<String, String> tags;
    String type;

    MapObject(){}


}

/*class Pointd{
    Double x_;
    Double y_;
    Pointd(Double x, Double y){
        x_ = x;
        y_ = y;
    }
}*/

@Data
class Geometry {
    String type;
    ArrayList<double[]> coordinates;
    crsK crs;

    Geometry(){}
    Geometry(String type, ArrayList<double[]> coords, crsK crs){
        this.type = type;
        this.coordinates = coords;
        this.crs = crs;
    }

}
@Data
class crsK {
    String type;
    Map<String, String> properties;
    crsK(){
        type = "name";
        this.properties = new HashMap<>();
        properties.put("name", "EPSG:0");
    }
}


