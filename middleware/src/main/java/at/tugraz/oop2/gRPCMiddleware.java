package at.tugraz.oop2;

import mapserviceGRPC.*;

public class gRPCMiddleware {
    static MapObjectRPC requestObjID(long id)
    {
        MapObjectRPC response;
        var client = MapApplication.getStub();
        mapserviceGRPC.req_ID request = req_ID.newBuilder()
                .setID(id)
                .build();
        try {
            response = client.getObjID(request);
        }catch (Exception e){
            System.out.println(e);
            return null;
        }
        return response;
    }
    static mapserviceGRPC.res_ObjArea requestObjBbox(String type, double[] bbox_tl, double[] bbox_br, boolean amenity,
                                              Long take, Long skip)
    {
        mapserviceGRPC.res_ObjArea response = null;
        var client = MapApplication.getStub();
        mapserviceGRPC.req_Obj_bbox request = req_Obj_bbox.newBuilder()
                .setBboxTl(Coordinate.newBuilder().setX(bbox_tl[0]).setY(bbox_tl[1]))
                .setBboxBr(Coordinate.newBuilder().setX(bbox_br[0]).setY(bbox_br[1]))
                .setAmenity(amenity)
                .setType(type)
                .setSkip(skip)
                .setTake(take)
                .build();
        try {
            response = client.getObjBbox(request);
        }catch (Exception e){
            System.out.println(e);
            return null;
        }
        return  response;
    }
    static mapserviceGRPC.res_ObjArea requestAmenPoint(String type, double[] point, double dist,
                                                Long take, Long skip)
    {
        mapserviceGRPC.res_ObjArea response = null;
        var client = MapApplication.getStub();
        mapserviceGRPC.req_amenity_point request = req_amenity_point.newBuilder()
                .setPoint (Coordinate.newBuilder().setX(point[0]).setY(point[1]))
                .setDist(dist)
                .setType(type)
                .setSkip(skip)
                .setTake(take)
                .build();
        try {
            response = client.getAmenitiyPoint(request);
        }catch (Exception e) {
            System.out.println(e);
            return null;
        }
        return response;
    }

}
