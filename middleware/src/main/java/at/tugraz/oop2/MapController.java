package at.tugraz.oop2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;
import lombok.Data;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.geojson.GeoJsonWriter;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class MapController {
    public static final Long DEFAULT_TAKE = 50L;
    public static final Long DEFAULT_SKIP = 0L;
    public static final String DEFAULT_TYPE = "A";

    @Data
    public class parsed_params{
        boolean bbox;
        String type = DEFAULT_TYPE;
        double[] bbox_tl;
        double[] bbox_br;
        double[] point;
        double dist;
        Long skip = DEFAULT_SKIP;
        Long take = DEFAULT_TAKE;
        parsed_params(){}
    }

    @Data
    public class sample_err{
        int type = 404;
        String msg = "Not implemented but this is an error!";
        sample_err(){}
    }

    //------------------------------------------------------------------------------------------------------------------
    // GET Mapping for BBox/Point Requests for Amenities
    // TODO: Parse the Params attached and differentiate between Point/Dist or BBox request
    // @param params the parameters fetched from the HTTP Request
    //------------------------------------------------------------------------------------------------------------------
    @GetMapping("/amenities")
    String getObjectList(@RequestParam Map<String, String> params)
    {
        //SAMPLE CODE EXAMPLE REQUEST
        parsed_params pp = new parsed_params();
        pp.bbox = false;
        pp.point = new double[]{15.00, 47.134};
        pp.dist = 6.00;
        //SAMPLE END

        String requested_List = null;
        if(pp.isBbox()) {
            try {
                requested_List = gRPCMiddleware.requestObjBbox(pp.getType(), pp.getBbox_tl(), pp.getBbox_br(),
                                true, pp.getTake(), pp.getSkip());
            } catch (Exception e){
                System.out.println("Exception caught: " + e.getMessage());
                return "An Error Occured...";
            }
        }
        else {
            try{
                requested_List = gRPCMiddleware.requestAmenPoint(pp.getType(), pp.getPoint(), pp.getDist(),
                        pp.getTake(), pp.getSkip());
            } catch (Exception e){
                System.out.println("Exception caught: " + e.getMessage());
                return "An Error Occured...";
            }
        }
        return requested_List;
    }

    //------------------------------------------------------------------------------------------------------------------
    // GET Mapping for getting Amenity by ID
    // @param id the Amenity ID to look for
    //------------------------------------------------------------------------------------------------------------------
    @GetMapping("/amenities/{id}")
    String getAmenityID(@PathVariable long id)
    {
        String requested_amend;
        try {
            requested_amend = gRPCMiddleware.requestObjID(id, true);
        } catch (Exception e){
            System.out.println("Exception caught: " + e.getMessage());
            return "An Error Occured...";
        }
        return requested_amend;
    }

    //------------------------------------------------------------------------------------------------------------------
    // GET Mapping for BBox Requests for roads
    // @param params the parameters fetched from the HTTP Request
    //------------------------------------------------------------------------------------------------------------------
    @GetMapping("/roads")
    String getObjectRoad(@RequestParam Map<String, String> params)
    {
        //SAMPLE CODE EXAMPLE REQUEST
        parsed_params pp = new parsed_params();
        pp.bbox = true;
        pp.bbox_tl = new double[]{15.34, 17.45};
        pp.bbox_br = new double[]{19.64, 20.45};
        //SAMPLE END

        String requested_List = null;
            try {
                requested_List = gRPCMiddleware.requestObjBbox(pp.getType(), pp.getBbox_tl(), pp.getBbox_br(),
                        false, pp.getTake(), pp.getSkip());
            } catch (Exception e){
                System.out.println("Exception caught: " + e.getMessage());
                return "An Error Occured...";
            }
        return requested_List;
    }

    //------------------------------------------------------------------------------------------------------------------
    // GET Mapping for getting Road by ID
    // @param id the Road ID to look for
    //------------------------------------------------------------------------------------------------------------------
    @GetMapping("/roads/{id}")
    String getObjectRoadID(@PathVariable long id)
    {
        String requested_road;
        try {
            requested_road = gRPCMiddleware.requestObjID(id, false);
        } catch (Exception e){
            System.out.println("Exception caught: " + e.getMessage());
            return "An Error Occured...";
        }
        return requested_road;
    }

    @GetMapping("/tile/{z}/{x}/{y}.png")
    ByteString getIMG(@PathVariable int z, @PathVariable int x, @PathVariable int y, @RequestParam List<String> filters)
    {
        mapserviceGRPC.PNG_image response = null;
        try {
            response = gRPCMiddleware.request_Image(x, y, z, filters);
        }catch (Exception e){
            System.out.println("Exception caught: " + e.getMessage());
            return null;
        }
        return response.getImageData();
    }
}
