package at.tugraz.oop2;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.geojson.GeoJsonWriter;

import java.io.IOException;

public class GeoSerializer extends JsonSerializer<Geometry> {
    @Override
    public void serialize(Geometry value, JsonGenerator gen, SerializerProvider serializers) throws IOException{
        GeoJsonWriter writer = new GeoJsonWriter();
        String GeoJson = writer.write(value);

        ObjectMapper objMap = new ObjectMapper();
        JsonNode node = objMap.readTree(GeoJson);
        gen.writeTree(node);
    }
}
