package at.tugraz.oop2;

import mapserviceGRPC.MapObjectRPC;

import java.util.ArrayList;

public class Utils {
    static Listresponse transformListResponse(mapserviceGRPC.res_ObjArea response, Long skip,
                                       Long take)
    {
        Listresponse reply = new Listresponse();
        for (var res : response.getObjectsList())
            reply.getEntries().add(getObjFromResponse(res));
        reply.getPaging().put("skip", skip);
        reply.getPaging().put("take", take);
        reply.getPaging().put("total", response.getTotal());
        return reply;
    }
    static MapObject getObjFromResponse(MapObjectRPC response){
        MapObject req_obj;
        if (response == null)
            return null;
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
