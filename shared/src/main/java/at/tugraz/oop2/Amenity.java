package at.tugraz.oop2;

import lombok.Data;
import org.locationtech.jts.geom.Geometry;

import java.util.Map;
@Data
public class Amenity extends MapObject{
    public Amenity(String name, long id, Map<String, String> tags, String type)
    {
        this.name = name;
        this.id = id;
        this.tags = tags;
        this.type = type;
    }

    public Amenity(long id, String name, Map<String, String> tags, String type, Geometry geom)
    {
        this.id = id;
        this.name = name;
        this.tags = tags;
        this.type = type;
        this.geom = geom;
    }
}
