package at.tugraz.oop2;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import mapserviceGRPC.*;

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
            Status err = Status.INTERNAL.withDescription("could not find");
            responseObserver.onError(err.asRuntimeException());
        }
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getObjBbox(req_Obj_bbox request, StreamObserver<res_ObjArea> responseObserver) {

    }

    @Override
    public void getAmenitiyPoint(req_amenity_point request, StreamObserver<res_ObjArea> responseObserver) {

    }
}
