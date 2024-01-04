package at.tugraz.oop2;

public class Route {
    Route(Double length, Double time, Road[] roads) {
        this.length = length;
        this.time = time;
        this.roads = roads;
    }
    Double length;
    Double time;
    Road[] roads;
}
