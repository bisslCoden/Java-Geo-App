package at.tugraz.oop2;

import com.fasterxml.jackson.databind.ObjectMapper;
import mapserviceGRPC.resJSON;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import java.util.HashMap;
import java.util.Map;

//import mapserviceGRPC.Geometry;
//import mapserviceGRPC.MapObjectRPC;

public class gRPCBackend {
    static resJSON buildResponseID(MapObject recieved_obj){
        if (recieved_obj == null)
            return null;
//        GeometryFactory fac = new GeometryFactory();
//
//        Amenity a = new Amenity("sample",123,new HashMap<String, String>() {{
//            put("s1", "s2");
//        }},
//                "restaurant");
//        a.geom = fac.createPoint(new org.locationtech.jts.geom.Coordinate(1, 1));
//        Amenity b = new Amenity("sample2",1223,new HashMap<String, String>() {{
//            put("s12", "s22");
//        }},
//                "restau2rant2");
//
//        a.geom = fac.createPoint(new org.locationtech.jts.geom.Coordinate(1, 1));
//        b.geom = fac.createLineString(new org.locationtech.jts.geom.Coordinate[]{ new org.locationtech.jts.geom.Coordinate(0.1, 2.3), new Coordinate(2.4, 2.54)});
//        Listresponse test = new Listresponse();
//        test.entries.add(a);
//        test.entries.add(b);
//        test.entries.add(a);
//        test.paging.put("skip", 2L);
//        test.paging.put("take", 23L);
//

        String jsonString = null;
        ObjectMapper objMapper = new ObjectMapper();
        try{
            jsonString = objMapper.writeValueAsString(recieved_obj);
            System.out.println(jsonString);

        }catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        resJSON response = resJSON.newBuilder().setJSON(jsonString).build();
        return response;

//        MapObjectRPC.Builder response = MapObjectRPC.newBuilder();
//        Geometry.Builder resp_geo = Geometry.newBuilder();
//        //CRS.Builder resp_crs = CRS.newBuilder();
//
//        response.setName(recieved_obj.getName());
//        response.setID(recieved_obj.getId());
//        response.setType(recieved_obj.getType());
//        response.setAmenity(is_amend);
//        for (var tag_pair : recieved_obj.getTags().entrySet())
//            response.putTags(tag_pair.getKey(), tag_pair.getValue());
//
//        resp_geo.setType(recieved_obj.getGeom().getGeometryType());
//        for (var coord : recieved_obj.getGeom().getCoordinates())
//            resp_geo.addCoords(Coordinate.newBuilder().setX(coord.getX()).setY(coord.getY()).build());

        //resp_crs.setType(recieved_obj.getGeom().getCentroid().getGeometryType());
        /*for (var prop : recieved_obj.getGeom().getFactory().get  .getProperties().entrySet())
            resp_crs.putProperties(prop.getKey(), prop.getValue());*/
        //resp_crs.build();

        //resp_geo.setCrs(resp_crs);
//        resp_geo.build();
//
//        response.setGeo(resp_geo);
//        if (!is_amend)
//        {
//            Road t = (Road) recieved_obj;
//            for(var child : t.getChild_ids())
//                response.addChildren(child);
//        }
//        return  response.build();
    }
    static resJSON buildResponseArea(MapObject[] recieved_objs, Map<String, Long> paging) {
        Listresponse result = new Listresponse();
        for (var obj : recieved_objs){
            result.getEntries().add(obj);
        }
        for (var pag : paging.entrySet())
        {
            result.getPaging().put(pag.getKey(), pag.getValue());
        }
        String jsonString = null;
        ObjectMapper objMapper = new ObjectMapper();
        try{
            jsonString = objMapper.writeValueAsString(result);
            System.out.println(jsonString);
        }catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        resJSON response = resJSON.newBuilder().setJSON(jsonString).build();
        return response;
    }
}
