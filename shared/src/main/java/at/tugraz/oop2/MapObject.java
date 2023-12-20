package at.tugraz.oop2;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.geojson.GeoJsonWriter;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
@Data
public class MapObject {
    String name;
    long id;
    @JsonSerialize (using = GeoSerializer.class)
    Geometry geom;// = new GeometryFactory().createEmpty(2);
    Map<String, String> tags = new HashMap<>();
    String type;

    public MapObject() {
    }
}





