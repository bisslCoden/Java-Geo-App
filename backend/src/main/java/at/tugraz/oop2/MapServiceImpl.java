package at.tugraz.oop2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.extern.java.Log;
import mapserviceGRPC.*;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.http.HttpStatus;
import org.w3c.dom.css.Rect;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

public class MapServiceImpl extends mapserviceGrpc.mapserviceImplBase{
    private static final Logger logger = Logger.getLogger(MapServiceServer.class.getName());

    //------------------------------------------------------------------------------------------------------------------
    // Mapping for the gRPC functions for sending back a reply to the client which requests an Object by ID
    // @param request the received GRPC request containing the ID
    // @param responseObserver the Stream on which to send back the JSON
    //------------------------------------------------------------------------------------------------------------------
    @Override
    public void getObjID(mapserviceGRPC.req_ID request,
                  io.grpc.stub.StreamObserver<resJSON> responseObserver)
    {
        long id = request.getID();
        //DEBUG: check if ID makes sense
        logger.info("Recieved getObjID request for ID: " + id);
        MapObject result;
        resJSON response;

        //Here the respective Amenity/Road gets fetched
        if (request.getAmenity())
        {
            result = Map.getInstance().getAmenity(id);
        }
        else
        {
            result = Map.getInstance().getRoad(id);
        }

        response = gRPCBackend.buildResponseID(result);

        //Here we still throw a runtime exception, however we could also create an error MSG here, sezialize it and send
        //it as a normal reply
        if (response == null)
        {
            //DEBUG
            logger.info("Could not find the Object with id: " + id);
            responseObserver.onError(Status.NOT_FOUND.asRuntimeException());
        }
        else
        {
            logger.info("Found the respective Object: " + id + " ; now sending back reply!");
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    // Mapping for the gRPC functions for sending back a reply to the client which requests Objects via BBox request
    // @param request the received GRPC request containing information about the specified bbox etc.
    // @param responseObserver the Stream on which to send back the JSON
    //------------------------------------------------------------------------------------------------------------------
    @Override
    public void getObjBbox(req_Obj_bbox request, StreamObserver<resJSON> responseObserver)
    {
        //creating BBox frame
        double width = request.getBboxBr().getX() - request.getBboxTl().getX();
        double height = request.getBboxBr().getY() - request.getBboxTl().getY();
        Rectangle2D.Double BBox = new Rectangle2D.Double(request.getBboxTl().getX(), request.getBboxTl().getY(),
                                                        width, height);
        //DEBUG: check if the BBox got created correctly
        logger.info(String.format("\tGot Bbox request with points TL: (%f|%f), BR: (%f|%f), w: %f, h: %f",
                request.getBboxTl().getX(), request.getBboxTl().getY(), request.getBboxBr().getX(), request.getBboxBr().getY(),
                width, height));
        MapObject[] result;
        Long total[] = new Long[1];
        //Calling Map function to get the Amenities in the specified box
        if (request.getAmenity())
        {
            result = Map.getInstance().getAmenities(BBox, request.getType(), request.getSkip(), request.getTake(), total);
        }
        else
        {
            result = Map.getInstance().getRoads(BBox, request.getType(),  request.getSkip(),  request.getTake(), total);
        }
        //Creating the paging map for Listresponse
        HashMap<String, Long> paging = new HashMap<>();
        paging.put("skip",request.getSkip());
        paging.put("take",request.getTake());
        paging.put("total", total[0]);

        //same thing here: this is still an exception if nothing can be found in the box, but we could formulate it as
        //errorJSON and serialize it
        if (result.length == 0)
        {
            responseObserver.onError(Status.NOT_FOUND.asRuntimeException());
        }
        else
        {
            //DEBUG: Just for debugging, see what we got
            logger.info("BBox: Successfully found some Amenities/Roads; Sending back " + result.length);
            resJSON response = gRPCBackend.buildResponseArea(result, paging);
            responseObserver.onNext(response);
            responseObserver.onCompleted();

        }
    }

    //------------------------------------------------------------------------------------------------------------------
    // Mapping for the gRPC functions for sending back a reply to the client which requests Amenities via Point/Dist
    // @param request the received GRPC request containing information about the specified Point/Dist etc.
    // @param responseObserver the Stream on which to send back the JSON
    //------------------------------------------------------------------------------------------------------------------
    @Override
    public void getAmenityPoint(req_amenity_point request, StreamObserver<resJSON> responseObserver)
    {
        //creating Point Dist params
        Point2D.Double point = new Point2D.Double(request.getPoint().getX(), request.getPoint().getY());
        double range = request.getDist();

        //DEBUG: See how the Point arrives in Backend
        Long total[] = new Long[1];
        logger.info(String.format("\tGot request for (%f|%f) and %f.", point.x, point.y, range));
        MapObject[] result = Map.getInstance().getAmenities(point, range, request.getType(), request.getSkip(),
                request.getTake(), total);

        HashMap<String, Long> paging = new HashMap<>();
        paging.put("skip",request.getSkip());
        paging.put("take",request.getTake());
        paging.put("total", total[0]);

        System.out.println("The length of output: " + result.length);
        //Nothing was found in this case
        if (result.length == 0)
        {
            responseObserver.onError(Status.NOT_FOUND.asRuntimeException());
        }
        else
        {
            //DEBUG: Just for debugging, see what we got
            logger.info("Point/Dist: Successfully found some Amenities; Sending back " + result.length);
            resJSON response = gRPCBackend.buildResponseArea(result, paging);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

    }

    //------------------------------------------------------------------------------------------------------------------
    // Mapping for the gRPC functions for sending back a reply to the client which requests a PNG
    // @param request the received GRPC request containing information about the specified x,y,z of the tile to render
    // @param responseObserver the Stream on which to send back the PNG
    //------------------------------------------------------------------------------------------------------------------
    @Override
    public void getImage (req_image request, StreamObserver<PNG_image> responseObserver)
    {
        //DEBUG: check for correct info
        logger.info(String.format("\tGot request for (x %d|y %d|z %d)", request.getX(), request.getY(), request.getZ()));
        List<String> layers = new ArrayList<>();
        PNG_image response = null;
        layers.addAll(request.getFiltersList());
        ByteString g = Map.getInstance().getTile(request.getX(), request.getY(), request.getZ(), layers);
        System.out.println("Bystring result with length " + g.size());
        response = PNG_image.newBuilder().setData(g).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
    @Override
    public void getUsage(req_use request, StreamObserver<resJSON> responseObserver)
    {
        //DEBUG
        logger.info(String.format("\tGot usage request for BboxTL: (%f|%f), BBoxBR (%f|%f)",
                request.getBboxTl().getX(), request.getBboxTl().getY(), request.getBboxBr().getX(),
                request.getBboxBr().getY()));
        resJSON response = null;
        double width = request.getBboxBr().getX() - request.getBboxTl().getX();
        double height = request.getBboxBr().getY() - request.getBboxTl().getY();
        Rectangle2D.Double BBox = new Rectangle2D.Double(request.getBboxTl().getX(), request.getBboxTl().getY(),
                width, height);
        Usages result = Map.getInstance().getUsage(BBox);
        try {
            response = gRPCBackend.buildResponseUsage(result);
        }catch (Exception e){
            System.out.println(e.getMessage());
            responseObserver.onError(Status.NOT_FOUND.asRuntimeException());
        }
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void calcRoute (req_route request, StreamObserver<resJSON> responseObserver)
    {
        logger.info(String.format("\tGot usage Route from node: %d to %d, Focus on %s",
                request.getStartID(), request.getEndID(), request.getLength() ? "length" : "time"));
        resJSON response = null;
        try {
            Route result =  Map.getInstance().getRoute(request.getStartID(), request.getEndID(), request.getLength());
            response = gRPCBackend.builResponseRoute(result);
        }catch (GeoExcept g)
        {
            if(g.status == HttpStatus.NOT_FOUND)
            {
                System.out.println(g.msg);
                responseObserver.onError(Status.NOT_FOUND.asRuntimeException());
            }
            else {
                System.out.println(g.msg);
                responseObserver.onError(Status.INVALID_ARGUMENT.asRuntimeException());
            }
        }
        catch (Exception e){
            System.out.println("Wooow something went real wront: " + e.getMessage());
            responseObserver.onError(Status.INTERNAL.asRuntimeException());
        }
        if(response != null) {
            System.out.println(response);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}
