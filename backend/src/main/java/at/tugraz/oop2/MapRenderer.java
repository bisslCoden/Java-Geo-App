package at.tugraz.oop2;

import org.json.simple.JSONArray;
import org.locationtech.jts.geom.Coordinate;
import com.google.protobuf.ByteString;
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
    // basic strokes
    static private final BasicStroke stroke_2px = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);
    static private final BasicStroke stroke_3px = new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);

    // background
    static private final Color color_background = new Color(255, 255, 255);

    // roads
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
            RenderLayer(frame, layers.get(index), image, gfx, data, entities);
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

    static private void RenderLayer(Rectangle2D.Double boundingBox, String layer, BufferedImage iamge, Graphics2D gfx, MapData data, List<Long> entities) {
        // TODO: complete rework
        // - implement function to be used by all MapObjects
        // - implement rendering of point
        Info info = Lookup.getOrDefault(layer, new Info(stroke_2px, color_residential));
        gfx.setColor(info.color);
        if(info.stroke != null) // TODO: maybe can be removed...
            gfx.setStroke(info.stroke);

        for(Road road : data._roads) {
            if(!road.tags.containsValue(layer)) continue;
            if(!data.isInside(boundingBox, road.geom)) continue;

            Coordinate[] coordinates = road.geom.getCoordinates();

            Coordinate origin = new Coordinate(boundingBox.x, boundingBox.y);

            if(coordinates.length < 2) {
                break;
            }
            entities.add(road.id);
            if(coordinates[0].equals(coordinates[coordinates.length - 1])) { // polygon
                int[] xs = new int[coordinates.length];
                int[] ys = new int[coordinates.length];

                for(int index = 0; index < coordinates.length; ++index) {
                    Coordinate c0 = new Coordinate(coordinates[index]);

                    c0.x = (c0.x - origin.x) * 512 / boundingBox.width;
                    c0.y = (c0.y - origin.y) * 512 / boundingBox.height;
                    xs[index] = ((int)c0.x);
                    ys[index] = ((int)c0.y);

                }
                gfx.fillPolygon(xs, ys, coordinates.length);
            }
            else { // line string
                for(int index = 0; index < coordinates.length - 1; ++index) {
                    Coordinate c0 = new Coordinate(coordinates[index]);
                    Coordinate c1 = new Coordinate(coordinates[index + 1]);

                    c0.x = (c0.x - origin.x) * 512 / boundingBox.width;
                    c0.y = (c0.y - origin.y) * 512 / boundingBox.height;
                    c1.x = (c1.x - origin.x) * 512 / boundingBox.width;
                    c1.y = (c1.y - origin.y) * 512 / boundingBox.height;
                    gfx.drawLine((int)c0.x, (int)c0.y, (int)c1.x, (int)c1.y);
                }
            }

        }
        for(Amenity amenity : data._amenities) {
            if(!amenity.tags.containsValue(layer)) continue;
            if(!data.isInside(boundingBox, amenity.geom)) continue;

            Coordinate[] coordinates = amenity.geom.getCoordinates();

            Coordinate origin = new Coordinate(boundingBox.x, boundingBox.y);

            if(coordinates.length < 2) {
                break;
            }
            entities.add(amenity.id);
            if(coordinates[0].equals(coordinates[coordinates.length - 1])) { // polygon
                int[] xs = new int[coordinates.length];
                int[] ys = new int[coordinates.length];

                for(int index = 0; index < coordinates.length; ++index) {
                    Coordinate c0 = new Coordinate(coordinates[index]);

                    c0.x = (c0.x - origin.x) * 512 / boundingBox.width;
                    c0.y = (c0.y - origin.y) * 512 / boundingBox.height;
                    xs[index] = ((int)c0.x);
                    ys[index] = ((int)c0.y);

                }
                gfx.fillPolygon(xs, ys, coordinates.length);
                //break;
            }
            else { // line string
                for(int index = 0; index < coordinates.length - 1; ++index) {
                    Coordinate c0 = new Coordinate(coordinates[index]);
                    Coordinate c1 = new Coordinate(coordinates[index + 1]);

                    c0.x = (c0.x - origin.x) * 512 / boundingBox.width;
                    c0.y = (c0.y - origin.y) * 512 / boundingBox.height;
                    c1.x = (c1.x - origin.x) * 512 / boundingBox.width;
                    c1.y = (c1.y - origin.y) * 512 / boundingBox.height;

                    gfx.drawLine((int)c0.x, (int)c0.y, (int)c1.x, (int)c1.y);
                }
            }

        }
        for(MapObject other : data._others) {
            if(!other.tags.containsValue(layer)) continue;
            if(!data.isInside(boundingBox, other.geom)) continue;

            Coordinate[] coordinates = other.geom.getCoordinates();

            Coordinate origin = new Coordinate(boundingBox.x, boundingBox.y);

            if(coordinates.length < 2) {
                break;
            }
            entities.add(other.id);
            if(coordinates[0].equals(coordinates[coordinates.length - 1])) { // polygon
                int[] xs = new int[coordinates.length];
                int[] ys = new int[coordinates.length];

                for(int index = 0; index < coordinates.length; ++index) {
                    Coordinate c0 = new Coordinate(coordinates[index]);

                    c0.x = (c0.x - origin.x) * 512 / boundingBox.width;
                    c0.y = (c0.y - origin.y) * 512 / boundingBox.height;

                    xs[index] = ((int)c0.x);
                    ys[index] = ((int)c0.y);
                }
                gfx.fillPolygon(xs, ys, coordinates.length);
                //break;
            }
            else { // line string
                for(int index = 0; index < coordinates.length - 1; ++index) {
                    Coordinate c0 = new Coordinate(coordinates[index]);
                    Coordinate c1 = new Coordinate(coordinates[index + 1]);

                    c0.x = (c0.x - origin.x) * 512 / boundingBox.width;
                    c0.y = (c0.y - origin.y) * 512 / boundingBox.height;
                    c1.x = (c1.x - origin.x) * 512 / boundingBox.width;
                    c1.y = (c1.y - origin.y) * 512 / boundingBox.height;

                    gfx.drawLine((int)c0.x, (int)c0.y, (int)c1.x, (int)c1.y);
                }
            }
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
