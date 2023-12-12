package at.tugraz.oop2;

import lombok.Data;

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
}
