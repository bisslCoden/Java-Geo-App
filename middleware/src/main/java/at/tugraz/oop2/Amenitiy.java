package at.tugraz.oop2;

import lombok.Data;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Map;
@Data
public class Amenitiy extends MapObject{
    public Amenitiy(String name, long id, Map<String, String> tags, String type)
    {
        this.name = name;
        this.id = id;
        this.tags = tags;
        this.type = type;
    }
}
