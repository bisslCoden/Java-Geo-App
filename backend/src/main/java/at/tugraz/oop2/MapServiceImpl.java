package at.tugraz.oop2;

import mapserviceGRPC.mapserviceGrpc;
import mapserviceGRPC.req_ID;

public class MapServiceImpl extends mapserviceGrpc.mapserviceImplBase{
    @Override
    public void getObjID(mapserviceGRPC.req_ID request,
                  io.grpc.stub.StreamObserver<mapserviceGRPC.MapObject> responseObserver){}
}
