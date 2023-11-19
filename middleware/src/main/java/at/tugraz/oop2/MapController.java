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
    @GetMapping("/amenities")
    Listresponse getObjectList(@RequestParam Map<String, String> params){
        //errorhandling?
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
    Road getObjectRoadID(@PathVariable long id) {
        return ObjectList.getInstance().getRoad();
    }
}
