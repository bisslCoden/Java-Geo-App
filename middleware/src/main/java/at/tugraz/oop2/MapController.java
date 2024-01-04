package at.tugraz.oop2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;
import lombok.Data;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.geojson.GeoJsonWriter;
import org.springframework.web.bind.annotation.*;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class MapController {


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
    String getAmenityPoint(
            @RequestParam(name = "point.x") Double pointX,
            @RequestParam(name = "point.y") Double pointY,
            @RequestParam(name = "point.d") Long pointD,
            @RequestParam(name = "skip", defaultValue = "0") Long skip,
            @RequestParam(name = "take", defaultValue = "50") Long take,
            @RequestParam(name = "amenity", defaultValue = "A") String amenity)
    {
        System.out.println("start?");
        System.out.println(String.format("Point: %f %f; %d\nskip: %d, take: %d, amenity: %s", pointX, pointY, pointD,
                skip, take, amenity));

        /*
        //SAMPLE CODE EXAMPLE REQUEST
        Parser.parsed_params pp = new parsed_params();
        pp.bbox = true;
        pp.bbox_tl = new double[]{15.45534, 47.05938};
        pp.dist = 100;
        pp.skip = (long)0;
        pp.take = (long)2;
        pp.type = "restaurant";
        //SAMPLE END
        */
        Parser.parsed_params pp;
        String requested_List;
        try{
            pp = Parser.checkBoundsPoint(pointX, pointY, pointD, skip, take, amenity);
            requested_List = gRPCMiddleware.requestAmenPoint(pp.getType(), pp.getPoint(), pp.getDist(),
                    pp.getTake(), pp.getSkip());
        }catch (Exception e)
        {
            throw e;
        }
        return requested_List;

    }
    @GetMapping("/amenities")
    String  getAmenityBBox(
            @RequestParam(name = "bbox.tr.x") Double bboxTLX,
            @RequestParam(name = "bbox.tr.y") Double bboxTLY,
            @RequestParam(name = "bbox.bl.x") Double bboxBRX,
            @RequestParam(name = "bbox.bl.y") Double bboxBRY,
            @RequestParam(name = "skip", defaultValue = "0") Long skip,
            @RequestParam(name = "take", defaultValue = "50") Long take,
            @RequestParam(name = "amenity", defaultValue = "A") String amenity)
    {
        System.out.println("start?");
        System.out.println(String.format("Points: %f %f; %f %f\nskip: %d, take: %d, amenity: %s", bboxTLX, bboxTLY, bboxBRX,
                bboxBRY, skip, take, amenity));

        /*
        //SAMPLE CODE EXAMPLE REQUEST
        Parser.parsed_params pp = new parsed_params();
        pp.bbox = true;
        pp.bbox_tl = new double[]{15.45534, 47.05938};
        pp.dist = 100;
        pp.skip = (long)0;
        pp.take = (long)2;
        pp.type = "restaurant";
        //SAMPLE END
        */
        Parser.parsed_params pp;
        String requested_List;
        try{
            pp = Parser.checkBoundsBBox(bboxTLX, bboxTLY, bboxBRX, bboxBRY, skip, take, amenity);
            requested_List = gRPCMiddleware.requestObjBbox(pp.getType(), pp.getBbox_tl(), pp.getBbox_br(),
                    true, pp.getTake(), pp.getSkip());
        }catch (Exception e)
        {
            throw e;
        }
        return requested_List;
    }


    //------------------------------------------------------------------------------------------------------------------
    // GET Mapping for BBox Requests for roads
    // @param params the parameters fetched from the HTTP Request
    //------------------------------------------------------------------------------------------------------------------
    @GetMapping("/roads")
    String getObjectRoad(
            @RequestParam(name = "bbox.tr.x") Double bboxTLX,
            @RequestParam(name = "bbox.tr.y") Double bboxTLY,
            @RequestParam(name = "bbox.bl.x") Double bboxBRX,
            @RequestParam(name = "bbox.bl.y") Double bboxBRY,
            @RequestParam(name = "skip", defaultValue = "0") Long skip,
            @RequestParam(name = "take", defaultValue = "50") Long take,
            @RequestParam(name = "road", defaultValue = "A") String road)
    {
        System.out.println(String.format("Points: %f %f; %f %f\nskip: %d, take: %d, amenity: %s", bboxTLX, bboxTLY, bboxBRX,
                bboxBRY, skip, take, road));
        Parser.parsed_params pp;
        String requested_List;
        try{
            pp = Parser.checkBoundsBBox(bboxTLX, bboxTLY, bboxBRX, bboxBRY, skip, take, road);
            requested_List = gRPCMiddleware.requestObjBbox(pp.getType(), pp.getBbox_tl(), pp.getBbox_br(),
                    false, pp.getTake(), pp.getSkip());
        }catch (Exception e)
        {
            throw e;
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
            requested_amend = gRPCMiddleware.requestObjID(Parser.parseID(id), true);
        } catch (Exception e){
            throw e;
        }
        return requested_amend;
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
            requested_road = gRPCMiddleware.requestObjID(Parser.parseID(id), false);
        } catch (Exception e){
            throw e;
        }
        return requested_road;
    }

    @GetMapping("/tile/{z}/{x}/{y}.png")
    byte[] getIMG(@PathVariable int z, @PathVariable int x, @PathVariable int y, @RequestParam List<String> filters)
    {
        mapserviceGRPC.PNG_image response = null;
        try {
            response = gRPCMiddleware.request_Image(x, y, z, filters);
        }catch (Exception e){
            System.out.println("Exception caught: " + e.getMessage());
            return null;
        }
        return response.getImageData().toByteArray();
    }
}
