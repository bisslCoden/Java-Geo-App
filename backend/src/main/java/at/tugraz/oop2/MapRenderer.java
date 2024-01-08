package at.tugraz.oop2;

import org.json.simple.JSONArray;
import org.locationtech.jts.geom.Coordinate;
import com.google.protobuf.ByteString;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;

import javax.imageio.ImageIO;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.File;

import java.awt.image.BufferedImage;
import java.awt.geom.Rectangle2D;
import java.awt.*;

import java.util.*;
import java.util.List;

public class MapRenderer {
    // dimensions 512x512 pixels
    static private final int WIDTH = 512;
    static private final int HEIGHT = 512;

    // basic strokes
    static private final BasicStroke stroke_2px = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);
    static private final BasicStroke stroke_3px = new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);

    // background
    static private final Color color_background = new Color(255, 255, 255);

    // roadss
    static private final Color color_trunk = new Color(255, 140, 0);
    static private final Color color_road = new Color(128, 128, 128);
    static private final Color color_motorway = new Color(255, 0, 0);
    static private final Color color_primary = new Color(255, 165, 0);
    static private final Color color_secondary = new Color(255, 255, 0);

    // land usage
    static private final Color color_forest = new Color(173, 209, 158);
    static private final Color color_residential = new Color(223, 233, 233);
    static private final Color color_vineyard = new Color(172, 224, 161);
    static private final Color color_grass = new Color(205, 235, 176);
    static private final Color color_railway = new Color(235, 219, 233);

    // nature
    static private final Color color_water = new Color(0, 128, 255);
    static private class Info {
        Info(BasicStroke stroke, Color color) {
            this.color = color;
            this.stroke = stroke;
        }
        Color color;
        BasicStroke stroke;
    }

    static private final HashMap<String, Info> Lookup = new HashMap<String, Info>() {{
        put("motorway", new Info(stroke_3px, color_motorway));
        put("trunk", new Info(stroke_2px, color_trunk));
        put("primary", new Info(stroke_2px, color_primary));
        put("secondary", new Info(stroke_2px, color_secondary));
        put("road", new Info(stroke_2px, color_road));
        put("forest", new Info(null, color_forest));
        put("residential", new Info(null, color_residential));
        put("vineyard", new Info(null, color_vineyard));
        put("grass", new Info(null, color_grass));
        put("railway", new Info(null, color_railway));
        put("water", new Info(null, color_water));
    }};



    static ByteString getTile(Integer x, Integer y, Integer z, List<String> layers, MapData data) {
        MapLogger.backendLogMapRequest(x, y, z, layers);
        System.out.print("[MapRenderer]: started rendering tile...");
        // create image
        BufferedImage image = new BufferedImage(512, 512, BufferedImage.TYPE_INT_RGB);
        Graphics2D gfx = image.createGraphics();

        // create bounding box
        Rectangle2D.Double frame = tileToBoundingBox(x, y, z);

        // draw to image
        gfx.setBackground(color_background);
        gfx.clearRect(0, 0, 512, 512);

        ArrayList<Long> entities = new ArrayList<>();
        for(int index = layers.size() - 1; index >= 0; --index) {
            RenderLayer(frame, layers.get(index), gfx, data, entities);
        }


        // write image to buffer in portable network graphic format
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", buffer);

            // debug code TODO: disable
            ImageIO.write(image, "png", new File("data/tile_debug_render.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println(" Done!");
        Collections.sort(entities);
        MapLogger.backendLogMapEntities(entities);
        // convert to and return ByteString
        return ByteString.copyFrom(buffer.toByteArray());
    }

    static private void RenderLayer(Rectangle2D.Double frame, String layer, Graphics2D gfx, MapData data, List<Long> entities) {

        // set draw style
        Info info = Lookup.getOrDefault(layer, new Info(stroke_2px, color_residential));
        gfx.setColor(info.color);
        if(info.stroke != null) // TODO: maybe can be removed...
            gfx.setStroke(info.stroke);

        for(Amenity amenity : data._amenities) {
            if(!amenity.tags.containsValue(layer)) continue;
            if(!data.isInside(frame, amenity.geom)) continue;

            renderGeometry(amenity.geom, frame, gfx);
        }

        for(Road road : data._roads) {
            if(!road.tags.containsValue(layer)) continue;
            if(!data.isInside(frame, road.geom)) continue;

            renderGeometry(road.geom, frame, gfx);
        }

        for(MapObject other : data._others) {
            if(!other.tags.containsValue(layer)) continue;
            if(!data.isInside(frame, other.geom)) continue;

            renderGeometry(other.geom, frame, gfx);
        }
    }

    static private void renderGeometry(Geometry geom, Rectangle2D.Double frame, Graphics2D gfx) {
        List<List<Coordinate>> line_lists = new ArrayList<>();
        List<List<Double>> polygon_lists_x = new ArrayList<>();
        List<List<Double>> polygon_lists_y = new ArrayList<>();

        // build coordinate array
        switch(geom.getGeometryType()) {
            case "GeometryCollection": {
                for(int index = 0; index < geom.getNumGeometries(); ++index) {
                    renderGeometry(geom.getGeometryN(index), frame, gfx);
                }
            } break;
            case "MultiPolygon": {
                for(int index = 0; index < geom.getNumGeometries(); ++index) {
                    Geometry geometry = geom.getGeometryN(index);

                    List<Double> xs = new ArrayList<>();
                    List<Double> ys = new ArrayList<>();

                    for(Coordinate coord : geometry.getCoordinates()) {
                        xs.add(coord.x);
                        ys.add(coord.y);
                    }

                    polygon_lists_x.add(xs);
                    polygon_lists_y.add(ys);
                }
            } break;
            case "Polygon": {
                List<Double> xs = new ArrayList<>();
                List<Double> ys = new ArrayList<>();

                for(Coordinate coord : geom.getCoordinates()) {
                    xs.add(coord.x);
                    ys.add(coord.y);
                }

                polygon_lists_x.add(xs);
                polygon_lists_y.add(ys);
            } break;
            case "LineString": {
                line_lists.add(Arrays.stream(geom.getCoordinates()).toList());
            } break;
            case "Point": {
                List<Coordinate> result = new ArrayList<>();

                for(Coordinate coord : geom.getCoordinates()) {
                    result.add(coord);
                    result.add(coord);
                }

                //line_lists.add(result);
            } break;
            default: {
                System.out.println("Something went wrong: Unhandled geometry type!");
                System.exit(0);
            } break;
        }

        // render line coordinates
        for(List<Coordinate> coord_list : line_lists) {
            for(int coord_index = 0; coord_index < coord_list.size() - 1; ++coord_index) {
                // get coordinates
                Coordinate c0 = new Coordinate(coord_list.get(coord_index));
                Coordinate c1 = new Coordinate(coord_list.get(coord_index + 1));

                // translate & scale to canvas space
                c0.x = (c0.x - frame.x) * 512 / frame.width;
                c0.y = (c0.y - frame.y) * 512 / frame.height;
                c1.x = (c1.x - frame.x) * 512 / frame.width;
                c1.y = (c1.y - frame.y) * 512 / frame.height;

                // draw
                gfx.drawLine((int)c0.x, (int)c0.y, (int)c1.x, (int)c1.y);
            }
        }

        // render polygon coordinates
        for(int polygon_index = 0; polygon_index < polygon_lists_x.size(); ++polygon_index) {
            // get coordinates
            List<Double> x_list = polygon_lists_x.get(polygon_index);
            List<Double> y_list = polygon_lists_y.get(polygon_index);
            int[] xs = new int[x_list.size()];
            int[] ys = new int[y_list.size()];

            // translate & scale to canvas space
            for(int coord_index = 0; coord_index < x_list.size(); ++ coord_index) {
                xs[coord_index] = (int)((x_list.get(coord_index) - frame.x) * (double)WIDTH / frame.width);
                ys[coord_index] = (int)((y_list.get(coord_index) - frame.y) * (double)HEIGHT / frame.height);
            }

            // draw
            gfx.fillPolygon(xs, ys, x_list.size());
        }
    }

    static private Rectangle2D.Double tileToBoundingBox(int x, int y, int z) {
        Rectangle2D.Double boundingBox = new Rectangle2D.Double();
        boundingBox.x = tileToLongitude(x, z);
        boundingBox.y = tileToLatitude(y, z);
        boundingBox.width = tileToLongitude(x + 1, z) - boundingBox.x;
        boundingBox.height = tileToLatitude(y + 1, z) - boundingBox.y;
        return boundingBox;
    }

    static private Double tileToLongitude(int x, int z) {
        return x / Math.pow(2.0, z) * 360.0 - 180.0;
    }

    static private Double tileToLatitude(int y, int z) {
        Double n = Math.PI - (2.0 * Math.PI * y) / Math.pow(2.0, z);
        return Math.toDegrees(Math.atan(Math.sinh(n)));
    }
}
