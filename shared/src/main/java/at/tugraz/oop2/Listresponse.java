package at.tugraz.oop2;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Data
public class Listresponse {
    ArrayList<MapObject> entries = new ArrayList<>();
    Map<String, Long> paging = new HashMap<>();
    Listresponse(){}
}
