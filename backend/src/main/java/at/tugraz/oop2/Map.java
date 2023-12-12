package at.tugraz.oop2;

import com.google.protobuf.ByteString;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class Map {
    // singleton structure
    private static Map _instance = null;
    static Map getInstance() {
        if (_instance == null)
            _instance = new Map();
        return _instance; };

    // constructor
    private Map() {}

    // methods
    public void load(String location) {  _data = MapLoader.load(location); };

    public Amenity getAmenity(Long id) {
        return _data.getAmenity(id);
    }
    public Amenity[] getAmenities(Rectangle2D.Double bbox, String type, Integer skip, Integer take) {
        return _data.getAmenities(bbox, type, skip, take);
    }
    public Amenity[] getAmenities(Point2D.Double point, Double distance,String type, Integer skip, Integer take) {
        return null;
    }
    public Road getRoad(Long id) {
        return _data.getRoad(id);
    }
    public Road[] getRoads(Rectangle2D.Double bbox, String type, Integer skip, Integer take) {
        return _data.getRoads(bbox, type, skip, take);
    }
    public ByteString getTile(Double x, Double y, Double z, List<String> filter) {
        // TODO: change return type
        //return MapRenderer.getTile(_data, x, y, z);
        return  null;
    }

    // members
    private MapData _data;
}

/*
def buildGeom(members:list, tags: dict{str,str}) -> GeometryCollection:
  if "multipolygon" in tags:
    multi_polygons=[]
    inners=[]
    outer=None
    for(i=0; i<members.lenght;):
      closed_circle=getNextClosed(i, members)
      # this iterates over the next memebers and returns a polygon
      # if it is able to find a combination of coordinated in the next
      # (one or more) *same* role-types,
      # i.e. a closed ring of only outer or inner line segments
      if closed_circle:
        if closed_circle.last_role=="outer":
          if outer:
            multi_polygons+=[buildMultipolygon([outer, ...inners])] #multipolygons usually have the first ring as the outer one
          outer=closed_circle.polygon

        elif closed_circle.last_role=="inner":
          inners+=[closed_circle.polygon]

        i=closed_circle.last_member_idx+1
      else
        raise error
    # if there is still a an outer ring pick it up and add it
    if outer:
      multi_polygons+=[buildMultipolygon([outer, ...inners])]
    return buildGeometrycollection(multi_polygons)
  else:
    return buildGeometrycollection(members)
    # just multiple geometries
*/

/*

MapData
-> MapLoader
-> MapRenderer





*/