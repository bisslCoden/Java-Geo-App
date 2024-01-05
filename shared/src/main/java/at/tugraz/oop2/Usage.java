package at.tugraz.oop2;

import lombok.Data;

@Data
public class Usage {
    Usage(String type, Double share, Double area) {
        this.type = type;
        this.share = share;
        this.area = area;
    }
    String type;
    Double share;
    Double area;
}