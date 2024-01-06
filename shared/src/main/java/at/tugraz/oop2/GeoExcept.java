package at.tugraz.oop2;

import jdk.jshell.Snippet;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class GeoExcept extends RuntimeException{
    HttpStatus status;
    ErrorMSG msg;
    @Data
    public static class ErrorMSG{
        String msg;
        ErrorMSG(String msg_i){ this.msg = msg_i; }
    }
    public GeoExcept(HttpStatus status, String msg){
        this.msg = new ErrorMSG(msg);
        this.status = status;
    }
}


