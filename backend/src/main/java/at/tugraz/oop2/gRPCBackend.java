package at.tugraz.oop2;

import com.fasterxml.jackson.databind.ObjectMapper;
import mapserviceGRPC.resJSON;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

public class gRPCBackend {
    //------------------------------------------------------------------------------------------------------------------
    // Convert the recieved MapObject from Data to JSON grpc string which gets sent back to the client
    // @param received_obj the obj returned by the MapData class
    //------------------------------------------------------------------------------------------------------------------
    static resJSON buildResponseID(MapObject recieved_obj){
        if (recieved_obj == null)
            return null;

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
    }

    //------------------------------------------------------------------------------------------------------------------
    // Same here but for the Point/Dist; Bbox requests
    // @param received_objs the objects returned by the MapData class
    // @param paging the paging info which gets included in the Listresponse JSON
    //------------------------------------------------------------------------------------------------------------------
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

    static resJSON buildResponseUsage(Usages result)
    {
        if(result == null)
            throw new GeoExcept(HttpStatus.NOT_FOUND, "not found");
        ObjectMapper objMapp = new ObjectMapper();
        String jsonString = null;
        try {
            jsonString = objMapp.writeValueAsString(result);
            System.out.println(jsonString);
        }catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        resJSON response = resJSON.newBuilder().setJSON(jsonString).build();
        return response;
    }

    static resJSON builResponseRoute(Route result)
    {
        if(result == null)
            throw new GeoExcept(HttpStatus.NOT_FOUND, "not found");
        ObjectMapper objMap = new ObjectMapper();
        String json = null;
        try {
            json = objMap.writeValueAsString(result);
        }catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        resJSON respone = resJSON.newBuilder().setJSON(json).build();
        return respone;
    }
}
