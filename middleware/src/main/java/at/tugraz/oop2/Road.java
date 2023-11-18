package at.tugraz.oop2;

import lombok.Data;

import java.util.ArrayList;
import java.util.Map;

@Data
public class Road extends MapObject {
    ArrayList<Long> child_ids;
    Road(String name, long id, Geometry geo, Map<String, String> tags, String type, ArrayList<Long> childids)
    {
        this.name = name;
        this.id = id;
        this.geom = geo;
        this.tags = tags;
        this.type = type;
        this.child_ids = childids;
    }
}

