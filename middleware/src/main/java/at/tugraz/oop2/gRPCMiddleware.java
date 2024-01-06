package at.tugraz.oop2;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import mapserviceGRPC.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

public class gRPCMiddleware {

    //------------------------------------------------------------------------------------------------------------------
    // Function for formulating and sending a request for an Object by ID
    // @param the ID to look for
    // @param amenity as this function is used for requesting both roads and amenities it is important to state which
    //                Type to look for
    //------------------------------------------------------------------------------------------------------------------
    static String requestObjID(long id, boolean amenity)
    {
        String response;
        var client = MapApplication.getStub();
        mapserviceGRPC.req_ID request = req_ID.newBuilder()
                .setID(id)
                .setAmenity(amenity)
                .build();
        try {
            //DEBUG: sending request
            System.out.println("Requesting " + (amenity ? "Amenitiy" : "Road") + " with ID " + id);
            response = client.getObjID(request).getJSON();
        }
        catch (StatusRuntimeException g){
            if (g.getStatus() == Status.NOT_FOUND)
                throw new GeoExcept(HttpStatus.NOT_FOUND, "Obj with ID " + id + " was not found");
            else
                throw new GeoExcept(HttpStatus.INTERNAL_SERVER_ERROR, "Could not reach backend");
        }
        catch (Exception e){
            throw e;
        }
        //DEBUG Verfiy that the response went through
        System.out.println("Sucessfully received a Response.");
        return response;
    }

    //------------------------------------------------------------------------------------------------------------------
    // Function for formulating and sending a request for Objects in a Specific Bounding Box
    // @param type The type of amenity/Road to look for (restaurant, highway, university etc.)
    // @param type bbox_tl top left corner point of the BBox
    // @param type bbox_br bottom right corner point of the BBox
    // @param amenity as mentioned before whether its amenity or road
    // @param take paging info how many instances are requested
    // @param skip paging ingo how many instances are to be skipped
    //------------------------------------------------------------------------------------------------------------------
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
        }
        catch (StatusRuntimeException g){
            if (g.getStatus() == Status.NOT_FOUND)
                throw new GeoExcept(HttpStatus.NOT_FOUND, "Bbox is Empty");
            else
                throw new GeoExcept(HttpStatus.INTERNAL_SERVER_ERROR, "Could not reach backend");
        }
        catch (Exception e){
            throw e;
        }
        //DEBUG Verfiy that the response went through
        System.out.println("Sucessfully received a Response.");
        return  response;
    }

    //------------------------------------------------------------------------------------------------------------------
    // Function for formulating and sending a request for Amenities in a certain distance from a Point
    // @param type The type of amenity to look for (restaurant, university etc.)
    // @param point The point from where to look for amens
    // @param dist the max distance an amen can be away from the point
    // @param take paging info how many instances are requested
    // @param skip paging ingo how many instances are to be skipped
    //------------------------------------------------------------------------------------------------------------------
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
        }
        catch (StatusRuntimeException g){
            if (g.getStatus() == Status.NOT_FOUND)
                throw new GeoExcept(HttpStatus.NOT_FOUND, "Area around Point is Empty");
            else
                throw new GeoExcept(HttpStatus.INTERNAL_SERVER_ERROR, "Could not reach backend");
        }
        catch (Exception e){
            throw e;
        }
        //DEBUG Verfiy that the response went through
        System.out.println("Sucessfully received a Response.");
        return response;
    }

    //------------------------------------------------------------------------------------------------------------------
    // Function for formulating and sending a request for a PNG file of a Tile
    // @param type The type of amenity to look for (restaurant, university etc.)
    // @param point The point from where to look for amens
    // @param dist the max distance an amen can be away from the point
    // @param take paging info how many instances are requested
    // @param skip paging ingo how many instances are to be skipped
    //------------------------------------------------------------------------------------------------------------------
    static PNG_image request_Image(int x, int y, int z, List<String> filters)
    {
        PNG_image resp_PNG = null;
        var client = MapApplication.getStub();
        req_image.Builder request = req_image.newBuilder().setX(x).setY(y).setZ(z);
        for(var s : filters)
            request.addFilters(s);
        try {
            resp_PNG = client.getImage(request.build());
        }
        catch (StatusRuntimeException g){
            if (g.getStatus() == Status.NOT_FOUND)
                throw new GeoExcept(HttpStatus.NOT_FOUND, "Actually this should not be thrown i think");
            else
                throw new GeoExcept(HttpStatus.INTERNAL_SERVER_ERROR, "Could not reach backend");
        }
        catch (Exception e){
            throw e;
        }
        //DEBUG Verfiy that the response went through
        System.out.println("Sucessfully received a Response.");
        return resp_PNG;
    }
    static String requestRoute(Long start, Long end, boolean length)
    {
        resJSON response;
        var client = MapApplication.getStub();
        req_route.Builder request = req_route.newBuilder().setStartID(start).setEndID(end).setLength(length);
        try {
            response = client.calcRoute(request.build());
        }
        catch (StatusRuntimeException g){
            if (g.getStatus() == Status.NOT_FOUND)
                throw new GeoExcept(HttpStatus.NOT_FOUND, "Some Node is not Present");
            else
                throw new GeoExcept(HttpStatus.INTERNAL_SERVER_ERROR, "Could not reach backend");
        }
        catch (Exception e){
            throw e;
        }
        return response.getJSON();
    }

    static String requestUsage(double[] bbox_tl, double[] bbox_br)
    {
        resJSON response;
        req_use.Builder req = req_use.newBuilder()
                .setBboxTl(Coordinate.newBuilder().setX(bbox_tl[0]).setY(bbox_tl[1]).build())
                .setBboxBr(Coordinate.newBuilder().setX(bbox_br[0]).setY(bbox_br[1]).build());
        var client = MapApplication.getStub();
        try {
            response = client.getUsage(req.build());
        }
        catch (StatusRuntimeException g){
            if (g.getStatus() == Status.NOT_FOUND)
                throw new GeoExcept(HttpStatus.NOT_FOUND, "This BBox for Usage is empty");
            else
                throw new GeoExcept(HttpStatus.INTERNAL_SERVER_ERROR, "Could not reach backend");
        }
        catch (Exception e){
            throw e;
        }
        return response.getJSON();
    }

}
