package at.tugraz.oop2;

import com.google.protobuf.RpcCallback;
import mapserviceGRPC.MapObjectRPC;
import mapserviceGRPC.req_ID;
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
    Amenitiy getAmenityID(@PathVariable long id)
    {
        Amenitiy requested_amend = (Amenitiy) getObjFromResponse(sendIDRequest(id));
        System.out.println("attempting to return amend");
        return requested_amend;
    }
    @GetMapping("/roads")
    Listresponse getObjectRoad(@RequestParam Map<String, String> params) {
        return ObjectList.getInstance().getList(Road.class);
    }

    @GetMapping("/roads/{id}")
    Road getObjectRoadID(@PathVariable long id) {
        Road requested_road = (Road) getObjFromResponse(sendIDRequest(id));
        return requested_road;
    }

    MapObjectRPC sendIDRequest(long id)
    {
        var client = MapApplication.getStub();
        mapserviceGRPC.req_ID request = req_ID.newBuilder()
                .setID(id)
                .build();
        MapObjectRPC response = client.getObjID(request);
        return response;
    }

    MapObject getObjFromResponse(MapObjectRPC response){
        MapObject req_obj;
        if (response.getAmenity())
        {
            req_obj = new Amenitiy(response.getName(), response.getID(),response.getTagsMap(),
                    response.getType());
        }
        else{
            req_obj = new Road(response.getName(), response.getID(),response.getTagsMap(),
                    response.getType(), (ArrayList<Long>) response.getChildrenList());
        }

        System.out.println("now tryin to get the coords");
        ArrayList<double[]> coords = new ArrayList<>();
        for (mapserviceGRPC.Coordinate coord : response.getGeo().getCoordsList()) {
            coords.add(new double[]{coord.getX(), coord.getY()});
        }
        req_obj.setGeo(response.getGeo().getType(), coords, response.getGeo().getCrs().getType(),
                response.getGeo().getCrs().getPropertiesMap());
        System.out.println("got all i need");
        return req_obj;
    }
}
