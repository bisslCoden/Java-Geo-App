package at.tugraz.oop2;

import lombok.Data;

import java.util.ArrayList;
import java.util.Map;

@Data
public class Listresponse {
    int errno;
    String msg;
    ArrayList<MapObject> entries;
    Map<String, Long> paging;
    Listresponse(){}
}
