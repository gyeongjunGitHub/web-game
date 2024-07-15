package drowGame.drowGame.socket.data;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class RequestType {
    List<String> request1 = new ArrayList<>();
    List<String> request2 = new ArrayList<>();
//    List<String> request3 = new ArrayList<>();

    public RequestType(){
        this.request1.add("gameOver");

        this.request2.add("rollBack");
        this.request2.add("clear");
        this.request2.add("push");
        this.request2.add("sendCoordinate");
    }
}
