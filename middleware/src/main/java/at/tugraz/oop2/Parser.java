package at.tugraz.oop2;


import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Data
public class Parser {
    public static final Long DEFAULT_TAKE = 50L;
    public static final Long DEFAULT_SKIP = 0L;
    public static final String DEFAULT_TYPE = "A";
    public static final int REQ_MAX_PARAMS = 7;
    public static final Long MAX_PARAM = Long.MAX_VALUE;
    public static final int REQ_MIN_PARAMS = 3;
    public static final String[] allowedParams = {"bbox.tl.x", "bbox.tl.y", "bbox.br.x",
            "bbox.br.y", "point.x", "point.y", "point.d", "take", "skip", "amenity", "road"};



    public static long parseID(long id){
        try {
            checkV(id);
        }catch (Exception e)
        {
            throw e;
        }
        return id;
    }

    public static boolean parseWeight(String input)
    {
        if (input.equals("length"))
            return true;
        else if (input.equals("time"))
            return false;
        else throw new GeoExcept(HttpStatus.BAD_REQUEST, "weight neither time or length");
    }

    public static parsed_params checkBoundsPoint(Double pointX, Double pointY,Double Dist, Long skip, Long take, String type)
    {
        //DEBUG
        try {
            checkX(pointX);
            checkY(pointY);
            checkV(skip);
            checkV(take);
            if (Dist <= 0)
                throw new GeoExcept(HttpStatus.BAD_REQUEST, "Parameter out of bounds");

        }catch (Exception e)
        {
            throw e;
        }
        return new parsed_params(new double[]{pointX, pointY}, Dist, skip, take, type);
    }

    public static parsed_params checkBoundsBBox(Double BBox_TLX, Double BBox_TLY, Double BBox_BRX, Double BBox_BRY,
                                                Long skip, Long take, String type)
    {
        try {
            checkBBox(BBox_TLX,  BBox_TLY, BBox_BRX, BBox_BRY);
            checkV(skip);
            checkV(take);
        }catch (Exception e)
        {
            throw e;
        }
        return new parsed_params(new double[] {BBox_TLX, BBox_TLY}, new double[] {BBox_BRX, BBox_BRY}, skip, take, type);
    }

    public static void checkBBox(Double BBox_TLX, Double BBox_TLY, Double BBox_BRX, Double BBox_BRY)
    {
        try
        {
            checkX(BBox_TLX);
            checkY(BBox_TLY);
            checkX(BBox_BRX);
            checkY(BBox_BRY);
        }catch (Exception e){
            throw e;
        }
        if (BBox_BRX <= BBox_TLX)
            throw new GeoExcept(HttpStatus.BAD_REQUEST, "Bbox bounds make no sense");
        else if(BBox_BRY >= BBox_TLY)
            throw new GeoExcept(HttpStatus.BAD_REQUEST, "Bbox bounds make no sense");

    }

    private static void checkX(Double x)
    {

        if (x < 12.000 || x > 18.000)
            throw new GeoExcept(HttpStatus.BAD_REQUEST, "X Coords out of bounds");
    }
    private static void checkY(Double y)
    {
        if (y < 42.000 || y > 51.000)
            throw new GeoExcept(HttpStatus.BAD_REQUEST, "Y Coords out of bounds");
    }

    private static void checkV(Long v)
    {
        if(v < 0 || v > MAX_PARAM)
            throw new GeoExcept(HttpStatus.BAD_REQUEST, "Parameter out of bounds");
    }
    @Data
    public static class parsed_params {
        Boolean bbox = null;
        String type = DEFAULT_TYPE;
        double[] bbox_tl = {0, 0};
        double[] bbox_br = {0, 0};
        double[] point = {0, 0};
        double dist = 0L;
        Long skip = DEFAULT_SKIP;
        Long take = DEFAULT_TAKE;
        parsed_params(double[] point, double dist, Long skip, Long take, String type)
        {
            this.bbox = false;
            this.point = point;
            this.dist = dist;
            this.skip = skip;
            this.take = take;
            this.type = type;
        }
        parsed_params(double[] bbox_tl, double[] bbox_br, Long skip, Long take, String type)
        {
            this.bbox = true;
            this.bbox_tl = bbox_tl;
            this.bbox_br = bbox_br;
            this.skip = skip;
            this.take = take;
            this.type = type;
        }
    }
}
