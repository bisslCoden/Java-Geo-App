package at.tugraz.oop2;

import com.google.protobuf.ByteString;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

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
    @GetMapping("/amenities")
    Listresponse getObjectList(@RequestParam Map<String, String> params){
        //errorhandling implement:
        //check if there is either point + d or bbox points
        //please then set this var to which type we need to process
        parsed_params pp = new parsed_params();
        pp.bbox = false;
        pp.point = new double[]{1.1, 1.1};
        pp.dist = 1.20;
        //Errorhandling: set pp :D

        Listresponse requested_List = null;
        if(pp.isBbox()) {
            try {
                requested_List = Utils.transformListResponse(gRPCMiddleware.requestObjBbox(pp.getType(), pp.getBbox_tl(), pp.getBbox_br(),
                                true, pp.getTake(), pp.getSkip()), pp.getSkip(), pp.getTake());
            } catch (Exception e){
                System.out.println(e);
            }
        }
        else {
            try{
                requested_List = Utils.transformListResponse(gRPCMiddleware.requestAmenPoint(pp.getType(), pp.getPoint(), pp.getDist(),
                        pp.getTake(), pp.getSkip()), pp.getSkip(), pp.getTake());
            } catch (Exception e) {
                System.out.println(e.getMessage());
                Listresponse err = new Listresponse();
                err.setErrno(e.getMessage().equals("INTERNAL: 404") ? "404" : "500");
                err.setMsg("not implemented but err");
                return err;
            }
        }
        return requested_List;
    }
    @GetMapping("/amenities/{id}")
    Amenity getAmenityID(@PathVariable long id)
    {
        //Errorhandling: if anything goes wrong in the backend requested_amend will just be null
        Amenity requested_amend = (Amenity) Utils.getObjFromResponse(gRPCMiddleware.requestObjID(id));
        return requested_amend;
    }
    @GetMapping("/roads")
    Listresponse getObjectRoad(@RequestParam Map<String, String> params) {
        //have to be set if used
        parsed_params pp = new parsed_params();
        //also here errorhandling sets pp

        Listresponse requested_List = null;
            try {
                requested_List = Utils.transformListResponse(gRPCMiddleware.requestObjBbox(pp.getType(), pp.getBbox_tl(), pp.getBbox_br(),
                        false, pp.getTake(), pp.getSkip()), pp.getSkip(), pp.getTake());
            } catch (Exception e){
                System.out.println(e);
            }
        return requested_List;
    }

    @GetMapping("/roads/{id}")
    Road getObjectRoadID(@PathVariable long id) {
        //Errorhandling: same here - if anything goes wrong in the backend requested_road will just be null
        Road requested_road = (Road) Utils.getObjFromResponse(gRPCMiddleware.requestObjID(id));
        return requested_road;
    }

    @GetMapping("/tile/{z}/{x}/{y}.png")
    ByteString getIMG(@PathVariable double z, @PathVariable double x, @PathVariable double y, @RequestParam List<String> filters){
        mapserviceGRPC.PNG_image response = null;
        try {
            response = gRPCMiddleware.request_Image(z, new double[]{x,y}, filters);
        }catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        return response.getImageData();
    }
    //------------------------------------------------------------------------------------
    // Beginning of GRPC requests
    // Code for making reqests to backend
    //------------------------------------------------------------------------------------

}
