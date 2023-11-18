package at.tugraz.oop2;

import lombok.Data;

import java.util.Map;
@Data
public class Amenitiy extends MapObject{
    Amenitiy(String name, long id, Geometry geo, Map<String, String> tags, String type)
    {
        this.name = name;
        this.id = id;
        this.geom = geo;
        this.tags = tags;
        this.type = type;
    }
}
