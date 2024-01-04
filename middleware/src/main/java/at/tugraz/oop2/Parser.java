package at.tugraz.oop2;


import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Data
public class Parser {
    public static final Long DEFAULT_TAKE = 50L;
    public static final Long DEFAULT_SKIP = 0L;
    public static final String DEFAULT_TYPE = "A";
    public static final int REQ_MAX_PARAMS = 7;
    public static final Long MAX_PARAM = 700000000L;
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

    public static parsed_params checkBoundsPoint(Double pointX, Double pointY,Long Dist, Long skip, Long take, String type)
    {
        //DEBUG
        try {
            checkX(pointX);
            checkY(pointY);
            checkV(Dist);
            checkV(skip);
            checkV(take);
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
            checkX(BBox_TLX);
            checkY(BBox_TLY);
            checkX(BBox_BRX);
            checkY(BBox_BRY);
            checkV(skip);
            checkV(take);
            if (BBox_BRX < BBox_TLX)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Parameters");
            else if(BBox_TLY < BBox_BRY)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Parameters");
        }catch (Exception e)
        {
            throw e;
        }
        return new parsed_params(new double[] {BBox_TLX, BBox_TLY}, new double[] {BBox_BRX, BBox_BRY}, skip, take, type);
    }

    private static void checkX(Double x)
    {
        if (x < 12.000 || x > 18.000)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Parameters");
    }
    private static void checkY(Double y)
    {
        if (y < 42.000 || y > 51.000)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Parameters");
    }

    private static void checkV(Long v)
    {
        if(v < 0 || v > MAX_PARAM)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Parameters");
    }
    /*
    public static parsed_params parseObjReq(Map<String, String> input, boolean amenity)
    {
        Queue<Pair<String, String>> params = new LinkedList<>();
        for(var c : input.entrySet())
        {
            params.add(Pair.of(c.getKey(), c.getValue()));
        }
        if(input.size() < REQ_MIN_PARAMS || input.size() > REQ_MAX_PARAMS)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Parameters");
        parsed_params pp_cur = new parsed_params();
        Pair<String, String> cur;
        while ((cur = params.poll()) != null)
        {
            //DEBUG
            System.out.println("cur par: " + cur.getKey() + "  " + cur.getValue());
            int index = 0;
            if((index = findIndex(cur.getKey())) != -1)
            {
                //DEBUG
                System.out.println(String.format("index: %d", index));
                try
                {
                    switch (index){
                        case 0:
                            checkBBox(pp_cur, true);
                            if (pp_cur.bbox_tl[0] == 0)
                                pp_cur.bbox_tl[0] = coord(cur.getValue());
                            else
                                throw new RuntimeException();
                            break;

                        case 1:
                            checkBBox(pp_cur, true);
                            if (pp_cur.bbox_tl[1] == 0)
                                pp_cur.bbox_tl[1] = coord(cur.getValue());
                            else
                                throw new RuntimeException();
                            break;

                        case 2:
                            checkBBox(pp_cur, true);
                            if (pp_cur.bbox_br[0] == 0)
                                pp_cur.bbox_br[0] = coord(cur.getValue());
                            else
                                throw new RuntimeException();
                            break;

                        case 3:
                            checkBBox(pp_cur, true);
                            if (pp_cur.bbox_br[1] == 0)
                                pp_cur.bbox_br[1] = coord(cur.getValue());
                            else
                                throw new RuntimeException();
                            break;

                        case 4:
                            //DEBUG
                            checkBBox(pp_cur, false);
                            System.out.println("after bbox");
                            if (pp_cur.point[0] == 0)
                                pp_cur.point[0] = coord(cur.getValue());
                            else
                                throw new RuntimeException("double param");
                            break;


                        case 5:
                            checkBBox(pp_cur, false);
                            if (pp_cur.point[1] == 0)
                                pp_cur.point[1] = coord(cur.getValue());
                            else
                                throw new RuntimeException();
                            break;

                        case 6:
                            checkBBox(pp_cur, false);
                            if (pp_cur.dist == 0)
                                pp_cur.dist = value(cur.getValue());
                            else
                                throw new RuntimeException();
                            break;

                        case 7:
                            if (pp_cur.take.equals(DEFAULT_TAKE))
                                pp_cur.take = value(cur.getValue());
                            else
                                throw new RuntimeException();
                            break;

                        case 8:
                            if (pp_cur.skip.equals(DEFAULT_SKIP))
                                pp_cur.skip = value(cur.getValue());
                            else
                                throw new RuntimeException();
                            break;

                        case 9:
                            if (!amenity)
                                throw new RuntimeException();
                            if (pp_cur.type.equals(DEFAULT_TYPE))
                                pp_cur.type = cur.getValue();
                            else
                                throw new RuntimeException();
                            break;

                        case 10:
                            if (amenity)
                                throw new RuntimeException();
                            if (pp_cur.type.equals(DEFAULT_TYPE))
                                pp_cur.type = cur.getValue();
                            else
                                throw new RuntimeException();
                            break;
                    }
                }
                catch (Exception e)
                {
                    System.out.println(e.getMessage());
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Parameters");
                }
            }
            else
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Parameters");
        }
        return pp_cur;
    }

    private static Double coord(String value)
    {
        Double valu;
        try{
            valu = Double.parseDouble(value);
            if(valu < 10.00 || valu > 50.00)
                throw new RuntimeException("value out of bounds");
        }catch (Exception e)
        {
            //DEBUG
            System.out.println("conversion error");
            throw e;
        }
        return valu;
    }
    private static Long value(String value)
    {
        Long valu;
        try{
            valu = Long.parseLong(value);
            if(valu < 0 || valu > MAX_PARAM)
                throw new RuntimeException("jo");
        }catch (Exception e)
        {
            throw e;
        }
        return valu;
    }
    private static void checkBBox(parsed_params pp, boolean bbox)
    {
        if (pp.bbox == null)
            pp.bbox = bbox;
        else if(pp.bbox == false && bbox)
            throw new RuntimeException("bbox");
        else if(pp.bbox == true && !bbox)
            throw new RuntimeException("bbox");

    }
    private static int findIndex(String input)
    {
        for (int i = 0; i < allowedParams.length; i++) {
            if (allowedParams[i].equals(input)) {
                return i;
            }
        }
        return -1;
    }
    */
    @Data
    public static class parsed_params {
        Boolean bbox = null;
        String type = DEFAULT_TYPE;
        double[] bbox_tl = {0, 0};
        double[] bbox_br = {0, 0};
        double[] point = {0, 0};
        Long dist = 0L;
        Long skip = DEFAULT_SKIP;
        Long take = DEFAULT_TAKE;
        parsed_params(double[] point, Long dist, Long skip, Long take, String type)
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
