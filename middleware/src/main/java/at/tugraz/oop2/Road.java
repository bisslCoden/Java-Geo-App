package at.tugraz.oop2;

import lombok.Data;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Map;

@Data
public class Road extends MapObject {
    ArrayList<Long> child_ids = new ArrayList<>();
    public Road(String name, long id, Map<String, String> tags, String type, ArrayList<Long> child_ids)
    {
        this.name = name;
        this.id = id;
        this.tags = tags;
        this.type = type;
        this.child_ids = child_ids;
    }

}

