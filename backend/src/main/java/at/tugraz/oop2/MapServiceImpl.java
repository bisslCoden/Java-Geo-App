package at.tugraz.oop2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import mapserviceGRPC.*;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
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

    @Override
    public void getObjID(mapserviceGRPC.req_ID request,
                  io.grpc.stub.StreamObserver<resJSON> responseObserver){
        long id = request.getID();
        logger.info("recieved getObjID request for ID: " + id);
        System.out.println("going into search....");
        //Here we need to connect to the database and fetch the id and stuff...
        MapObject result;
        resJSON response;
        if (request.getAmenity())
        {
            System.out.println("requesting Ameni");
            result = Map.getInstance().getAmenity(id);
        }
        else
        {
            System.out.println("requesting Road");
            result = Map.getInstance().getRoad(id);
        }

        GeometryFactory fac = new GeometryFactory();
        result.setGeom(fac.createLineString(new Coordinate[]{new Coordinate(12.54, 2.4), new Coordinate(22.5, 43.1)}));
        response = gRPCBackend.buildResponseID(result);
        if (response == null)
        {
            Status err = Status.INTERNAL.withDescription("404");
            responseObserver.onError(err.asRuntimeException());
        }
        else
        {
            System.out.println("sending a sucefful reply");
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getObjBbox(req_Obj_bbox request, StreamObserver<resJSON> responseObserver) {
        Rectangle2D.Double BBox = new Rectangle2D.Double(request.getBboxTl().getX(), request.getBboxTl().getY(),
                (request.getBboxBr().getX()-request.getBboxTl().getX()),
                (request.getBboxBr().getY() - request.getBboxTl().getY()));
        MapObject[] result;
        if (request.getAmenity()) {
            result = Map.getInstance().getAmenities(BBox, request.getType(), (int) request.getSkip(), (int) request.getTake());
        }
        else {
            result = Map.getInstance().getRoads(BBox, request.getType(), (int) request.getSkip(), (int) request.getTake());
        }
        HashMap<String, Long> paging = new HashMap<>();
        paging.put("skip",request.getSkip());
        paging.put("take",request.getTake());
        paging.put("total", (long) result.length);


        if (result == null)
        {
            Status err = Status.INTERNAL.withDescription("404");
            responseObserver.onError(err.asRuntimeException());
        }
        else
        {
            resJSON response = gRPCBackend.buildResponseArea(result, paging);
            responseObserver.onNext(response);
        }
    }

    @Override
    public void getAmenityPoint(req_amenity_point request, StreamObserver<resJSON> responseObserver) {
        System.out.println("in amend req");
        Point2D.Double point = new Point2D.Double(request.getPoint().getX(), request.getPoint().getY());
        double range = request.getDist();
        MapServiceServer.logger.info(String.format("\tGot request for (%f|%f) and %f.", point.x, point.y, range));
        MapObject[] result = Map.getInstance().getAmenities(point, range, request.getType(), (int) request.getSkip(),
                (int)request.getTake());

        HashMap<String, Long> paging = new HashMap<>();
        paging.put("skip",request.getSkip());
        paging.put("take",request.getTake());
        paging.put("total", (long) result.length);

        if (result == null)
        {
            Status err = Status.INTERNAL.withDescription("404");
            responseObserver.onError(err.asRuntimeException());
        }
        else
        {
            resJSON response = gRPCBackend.buildResponseArea(result, paging);
            responseObserver.onNext(response);
        }

    }

    @Override
    public void getImage (req_image request, StreamObserver<PNG_image> responseObserver)
    {
        List<String> filters = new ArrayList<>();
        PNG_image response = null;
        for(var s : request.getFiltersList())
            filters.add(s);
        ByteString g = Map.getInstance().getTile(request.getTile().getX(), request.getTile().getY(), request.getZoom(),
                filters);
        response = PNG_image.newBuilder().setImageData(g).build();
        responseObserver.onNext(response);
    }
}
