package at.tugraz.oop2;

import mapserviceGRPC.*;

import java.util.logging.Logger;

public class MapServiceImpl extends mapserviceGrpc.mapserviceImplBase{
    private static final Logger logger = Logger.getLogger(MapServiceServer.class.getName());

    @Override
    public void getObjID(mapserviceGRPC.req_ID request,
                  io.grpc.stub.StreamObserver<mapserviceGRPC.MapObject> responseObserver){
        long id = request.getID();
        logger.info("recieved getObjID request for ID: " + id);
        //Here we need to connect to the database and fetch the id and stuff...
        Coordinate sample_cord = Coordinate.newBuilder()
                .setX(0.01111)
                .setY(2.3344)
                .build();
        MapObject sample_response = MapObject.newBuilder()
                .setName("sample_amend")
                .setID(id)
                .setAmenity(true)
                .setGeo(Geometry.newBuilder()
                        .setType("point")
                        .addCoords(sample_cord)
                        .setCrs(CRS.newBuilder()
                                .setType("idk")
                                .build())
                        .build())
                .build();
        responseObserver.onNext(sample_response);
        responseObserver.onCompleted();
    }
}
