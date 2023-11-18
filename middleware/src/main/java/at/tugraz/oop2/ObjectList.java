package at.tugraz.oop2;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Data
public class ObjectList {
    private static ObjectList instance = null;
    Map<String, Long> paging_sample;
    ObjectList(){
        Objects = new ArrayList<>();
        paging_sample = new HashMap<>();
        paging_sample.put("skip", 0L);
        paging_sample.put("take", 2L);
        paging_sample.put("total", 3L);

    }
    public static ObjectList getInstance()
    {
        if (instance == null)
            instance = new ObjectList();
        return instance;
    }
    private ArrayList<MapObject> Objects;
    public void addObj(MapObject obj){
        Objects.add(obj);
    }
    public Amenitiy getAmend(){
        return (Amenitiy) Objects.get(0);
    }
    public Road getRoad(){
        return (Road) Objects.get(Objects.size()-1);
    }
    public Listresponse getList(Class<?> Obj){
        Listresponse resp = new Listresponse();
        resp.entries = new ArrayList<>();
        for (MapObject obj : Objects){
            if (Obj.isInstance(obj)){
                resp.entries.add(obj);
            }
        }
        resp.paging = paging_sample;
        return  resp;
    }
}
