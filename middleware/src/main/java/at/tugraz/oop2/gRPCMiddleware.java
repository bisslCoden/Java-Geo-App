package at.tugraz.oop2;

import mapserviceGRPC.*;

import java.util.List;

public class gRPCMiddleware {
    static String requestObjID(long id, boolean amenity)
    {
        String response;
        var client = MapApplication.getStub();
        mapserviceGRPC.req_ID request = req_ID.newBuilder()
                .setID(id)
                .setAmenity(amenity)
                .build();
        try {
            System.out.println("sending?");
            response = client.getObjID(request).getJSON();
        }catch (Exception e){
            System.out.println("ECEEEPT" + e.getMessage());
            return null;
        }
        System.out.println("got my response");
        return response;
    }
    static String requestObjBbox(String type, double[] bbox_tl, double[] bbox_br, boolean amenity,
                                              Long take, Long skip)
    {
        String response = null;
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
            response = client.getObjBbox(request).getJSON();
        }catch (Exception e){
            System.out.println("ECEEEPT" + e.getMessage());
            return null;
        }
        return  response;
    }
    static String requestAmenPoint(String type, double[] point, double dist,
                                                Long take, Long skip)
    {
        String response = null;
        var client = MapApplication.getStub();
        mapserviceGRPC.req_amenity_point request = req_amenity_point.newBuilder()
                .setPoint (Coordinate.newBuilder().setX(point[0]).setY(point[1]))
                .setDist(dist)
                .setType(type)
                .setSkip(skip)
                .setTake(take)
                .build();
        try {
            response = client.getAmenityPoint(request).getJSON();
        }catch (Exception e) {
            throw e;
        }
        return response;
    }

    static PNG_image request_Image(double zoom,double[] point, List<String> filters){
        PNG_image resp_PNG = null;
        var client = MapApplication.getStub();
        req_image.Builder request = req_image.newBuilder()
                .setTile(Coordinate.newBuilder().setX(point[0]).setY(point[1]).build())
                .setZoom(zoom);
        for(var s : filters)
            request.addFilters(s);
        try {
            resp_PNG = client.getImage(request.build());
        }
        catch (Exception e)
        {
            throw e;
        }
        return resp_PNG;
    }

}
