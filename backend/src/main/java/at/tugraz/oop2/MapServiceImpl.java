package at.tugraz.oop2;

import mapserviceGRPC.MapObject;
import mapserviceGRPC.mapserviceGrpc;
import mapserviceGRPC.req_ID;
import java.util.logging.Logger;

public class MapServiceImpl extends mapserviceGrpc.mapserviceImplBase{
    private static final Logger logger = Logger.getLogger(MapServiceServer.class.getName());

    @Override
    public void getObjID(mapserviceGRPC.req_ID request,
                  io.grpc.stub.StreamObserver<mapserviceGRPC.MapObject> responseObserver){
        long id = request.getID();
        logger.info("recieved: " + id);
        MapObject response = MapObject.newBuilder()
                .setName("sample_amend")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
