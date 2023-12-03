package at.tugraz.oop2;

import com.google.protobuf.ByteString;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import mapserviceGRPC.*;
import org.w3c.dom.css.Rect;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class MapServiceImpl extends mapserviceGrpc.mapserviceImplBase{
    private static final Logger logger = Logger.getLogger(MapServiceServer.class.getName());

    @Override
    public void getObjID(mapserviceGRPC.req_ID request,
                  io.grpc.stub.StreamObserver<MapObjectRPC> responseObserver){
        long id = request.getID();
        logger.info("recieved getObjID request for ID: " + id);
        //Here we need to connect to the database and fetch the id and stuff...
        MapObjectRPC response;
        if (request.getAmenity())
            response =  gRPCBackend.buildResponse((MapObject) MapData.instance().getAmenity(id), true);
        else
            response = gRPCBackend.buildResponse((MapObject) MapData.instance().getRoad(id), false);
        if (response == null)
        {
            Status err = Status.INTERNAL.withDescription("404");
            responseObserver.onError(err.asRuntimeException());
        }
        else
            responseObserver.onNext(response);
    }

    @Override
    public void getObjBbox(req_Obj_bbox request, StreamObserver<res_ObjArea> responseObserver) {
        Rectangle2D.Double BBox = new Rectangle2D.Double(request.getBboxTl().getX(), request.getBboxTl().getY(),
                (request.getBboxBr().getX()-request.getBboxTl().getX()),
                (request.getBboxBr().getY() - request.getBboxTl().getY()));
        MapObject[] result;
        res_ObjArea.Builder response = res_ObjArea.newBuilder();
        if (request.getAmenity())
            result = MapData.instance().getAmenities(BBox);
        else
            result = MapData.instance().getRoads(BBox);
        if (result == null)
        {
            Status err = Status.INTERNAL.withDescription("404");
            responseObserver.onError(err.asRuntimeException());
        }
        else
        {
            for (var obj : result)
                response.addObjects(gRPCBackend.buildResponse(obj, request.getAmenity()));

            response.setTotal(result.length);
            responseObserver.onNext(response.build());
        }
    }

    @Override
    public void getAmenityPoint(req_amenity_point request, StreamObserver<res_ObjArea> responseObserver) {
        System.out.println("in amend req");
        Point2D.Double point = new Point2D.Double(request.getPoint().getX(), request.getPoint().getY());
        double range = request.getDist();
        MapServiceServer.logger.info(String.format("\tGot request for (%f|%f) and %f.", point.x, point.y, range));
        MapObject[] result = MapData.instance().getAmenities(point, range);
        res_ObjArea.Builder response = res_ObjArea.newBuilder();
        if (result == null)
        {
            MapServiceServer.logger.info("\tERROR: could not find specified Point. Sending back Error...");
            Status err = Status.INTERNAL.withDescription("404");
            responseObserver.onError(err.asRuntimeException());
        }
        else
        {
            for (var obj : result)
                response.addObjects(gRPCBackend.buildResponse(obj, true));
            response.setTotal(result.length);
            responseObserver.onNext(response.build());
        }

    }

    @Override
    public void getImage (req_image request, StreamObserver<PNG_image> responseObserver)
    {
        List<String> filters = new ArrayList<>();
        PNG_image response = null;
        for(var s : request.getFiltersList())
            filters.add(s);
        ByteString g = MapData.instance().getTile(request.getZoom(),
                new Point2D.Double(request.getTile().getX(), request.getTile().getY()), filters);
        response = PNG_image.newBuilder().setImageData(g).build();
        responseObserver.onNext(response);
    }
}
