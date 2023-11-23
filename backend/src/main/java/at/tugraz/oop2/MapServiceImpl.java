package at.tugraz.oop2;

import io.grpc.Status;
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
        MapObjectRPC response =  gRPCBackend.buildResponse((MapObject) MapData.instance().getAmenity(id), true);
        if (response == null)
        {
            Status err = Status.INTERNAL.withDescription("could not find");
            responseObserver.onError(err.asRuntimeException());
        }
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }


}
