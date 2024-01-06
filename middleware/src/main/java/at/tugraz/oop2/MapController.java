package at.tugraz.oop2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;
import lombok.Data;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.geojson.GeoJsonWriter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;

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
            @RequestParam(name = "bbox.tl.x", required = false) Double bboxTLX,
            @RequestParam(name = "bbox.tl.y", required = false) Double bboxTLY,
            @RequestParam(name = "bbox.br.x", required = false) Double bboxBRX,
            @RequestParam(name = "bbox.br.y", required = false) Double bboxBRY,
            @RequestParam(name = "point.x", required = false) Double pointX,
            @RequestParam(name = "point.y", required = false) Double pointY,
            @RequestParam(name = "point.d", required = false) Long pointD,
            @RequestParam(name = "skip", defaultValue = "0") Long skip,
            @RequestParam(name = "take", defaultValue = "50") Long take,
            @RequestParam(name = "amenity", defaultValue = "A") String amenity)
    {
        System.out.println(String.format("Point: %f %f; %d\nBBox: %f %f; %f %f\nskip: %d, take: %d, amenity: %s", pointX,
                pointY, pointD, bboxTLX, bboxTLY, bboxBRX, bboxBRY ,skip, take, amenity));
        Boolean bbox = null;
        if (bboxTLX != null && bboxTLY != null && bboxBRX != null && bboxBRY != null)
        {
            if (pointX != null && pointY != null && pointD != null)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Parameters");
            else bbox = true;
        }
        else if (pointX != null && pointY != null && pointD != null)
            bbox = false;
        else
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Parameters");

        Parser.parsed_params pp;
        String requested_List;
        try{
            if (bbox)
            {
                pp = Parser.checkBoundsBBox(bboxTLX, bboxTLY, bboxBRX, bboxBRY, skip, take, amenity);
                requested_List = gRPCMiddleware.requestObjBbox(pp.getType(), pp.getBbox_tl(), pp.getBbox_br(),
                        true, pp.getTake(), pp.getSkip());
            }
            else
            {
                pp = Parser.checkBoundsPoint(pointX, pointY, pointD, skip, take, amenity);
                requested_List = gRPCMiddleware.requestAmenPoint(pp.getType(), pp.getPoint(), pp.getDist(),
                        pp.getTake(), pp.getSkip());
            }
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
            @RequestParam(name = "bbox.tl.x") Double bboxTLX,
            @RequestParam(name = "bbox.tl.y") Double bboxTLY,
            @RequestParam(name = "bbox.br.x") Double bboxBRX,
            @RequestParam(name = "bbox.br.y") Double bboxBRY,
            @RequestParam(name = "skip", defaultValue = "0") Long skip,
            @RequestParam(name = "take", defaultValue = "50") Long take,
            @RequestParam(name = "road", defaultValue = "A") String road)
    {
        System.out.println(String.format("BBox: %f %f; %f %f\nskip: %d, take: %d, road: %s",
                bboxTLX, bboxTLY, bboxBRX, bboxBRY ,skip, take, road));
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
            /*ObjectMapper m = new ObjectMapper();
            String res;
            System.out.println("now throwing" + e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "works?", e);
            try{
                res = m.writeValueAsString(new ));
            }catch (Exception a)
            {
                throw e;
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        */
        }
        return requested_road;
    }

    @ExceptionHandler (GeoExcept.class)
    public ResponseEntity<GeoExcept.ErrorMSG> handleCustomException(GeoExcept Exc)
    {
        return ResponseEntity.status(Exc.getStatus()).body(Exc.getMsg());
    }
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<GeoExcept.ErrorMSG> handleBadInput(NoHandlerFoundException Exc)
    {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new GeoExcept.ErrorMSG("No Handler found"));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<GeoExcept.ErrorMSG> handleInputexc(Exception Exc)
    {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GeoExcept.ErrorMSG("Parameters Invalid"));
    }

    @GetMapping("/tile/{z}/{x}/{y}.png")
    byte[] getIMG(@PathVariable int z, @PathVariable int x, @PathVariable int y,
                  @RequestParam (name = "filter", defaultValue = "motorway") List<String> filters)
    {
        //DEBUG
        System.out.println(String.format("request for tile z: %d x: %d y: %d", z, x, y));
        for(var f : filters)
            System.out.println(f);
        mapserviceGRPC.PNG_image response = null;
        try {
            response = gRPCMiddleware.request_Image(x, y, z, filters);
        }catch (Exception e){
            throw e;
        }
        return response.getData().toByteArray();
    }

    @GetMapping("/route")
    String getRoute(
            @RequestParam (name = "from") Long startID,
            @RequestParam (name = "to") Long endID,
            @RequestParam (name = "weighting", defaultValue = "length") String weight)
    {
        String responseBody = null;
        try {
            Long start = Parser.parseID(startID);
            Long end = Parser.parseID(endID);
            boolean length = Parser.parseWeight(weight);
            responseBody = gRPCMiddleware.requestRoute(start, end, length);
        }catch (Exception e)
        {
            throw e;
        }
        return  responseBody;
    }

    @GetMapping("/usage")
    String getUse(
            @RequestParam(name = "bbox.tl.x") Double bboxTLX,
            @RequestParam(name = "bbox.tl.y") Double bboxTLY,
            @RequestParam(name = "bbox.br.x") Double bboxBRX,
            @RequestParam(name = "bbox.br.y") Double bboxBRY
    )
    {
        String response;
        try {
            Parser.checkBBox(bboxTLX, bboxTLY, bboxBRX, bboxBRY);
            response = gRPCMiddleware.requestUsage(new double[]{bboxTLX, bboxTLY},
                    new double[] {bboxBRX, bboxBRY});
        }catch (Exception e)
        {
            throw e;
        }
        return response;
    }
}
