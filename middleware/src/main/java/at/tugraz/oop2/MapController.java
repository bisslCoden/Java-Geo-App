package at.tugraz.oop2;

import org.apache.catalina.users.GenericRole;
import org.locationtech.jts.geom.Coordinate;
import org.springframework.web.bind.annotation.*;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RestController
public class MapController {
    /*MapObject randomObj()
    {
        ArrayList<Point2D.Double> points = new ArrayList<Point2D.Double>();
        points.add(new Point2D.Double(1.453456,2.32345));
        Geometry randomgeo = new Geometry("Point", points);
        Map<String, String> tags = new HashMap<>();
        tags.put("testtag","passed");
        tags.put("testtag2", "also passed");
        MapObject firsttry = new MapObject("Pizzaiolo",1L, randomgeo, tags, "restaurant");
        return  firsttry;
    }*/
    @GetMapping("/amenities")
    Listresponse getObjectList(@RequestParam Map<String, String> params){
        return ObjectList.getInstance().getList(Amenitiy.class);
    }
    @GetMapping("/amenities/{id}")
    Amenitiy getObjectID(@PathVariable long id)
    {
        return ObjectList.getInstance().getAmend();
    }
    @GetMapping("/roads")
    Listresponse getObjectRoad(@RequestParam Map<String, String> params) {
        return ObjectList.getInstance().getList(Road.class);
    }

    @GetMapping("/roads/{id}")
    Road getObjectRoaID(@PathVariable long id) {
        return ObjectList.getInstance().getRoad();
    }
}
