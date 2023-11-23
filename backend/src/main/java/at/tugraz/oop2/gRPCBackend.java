package at.tugraz.oop2;

import mapserviceGRPC.CRS;
import mapserviceGRPC.Coordinate;
import mapserviceGRPC.Geometry;
import mapserviceGRPC.MapObjectRPC;

public class gRPCBackend {
    static MapObjectRPC buildResponse(MapObject recieved_obj, boolean is_amend){
        if (recieved_obj == null)
            return null;

        MapObjectRPC.Builder response = MapObjectRPC.newBuilder();
        Geometry.Builder resp_geo = Geometry.newBuilder();
        CRS.Builder resp_crs = CRS.newBuilder();

        response.setName(recieved_obj.getName());
        response.setID(recieved_obj.getId());
        response.setType(recieved_obj.getType());
        response.setAmenity(is_amend);
        for (var tag_pair : recieved_obj.getTags().entrySet())
            response.putTags(tag_pair.getKey(), tag_pair.getValue());

        resp_geo.setType(recieved_obj.getGeom().getType());
        for (var coord : recieved_obj.getGeom().getCoordinates())
            resp_geo.addCoords(Coordinate.newBuilder().setX(coord[0]).setY(coord[1]).build());

        resp_crs.setType(recieved_obj.getGeom().getCrs().getType());
        for (var prop : recieved_obj.getGeom().getCrs().getProperties().entrySet())
            resp_crs.putProperties(prop.getKey(), prop.getValue());
        resp_crs.build();

        resp_geo.setCrs(resp_crs);
        resp_geo.build();

        response.setGeo(resp_geo);
        if (!is_amend)
        {
            Road t = (Road) recieved_obj;
            for(var child : t.getChild_ids())
                response.addChildren(child);
        }
        return  response.build();
    }
}
