package drowGame.drowGame.dto;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Request {
    List<String> request1 = new ArrayList<>();
    List<String> request2 = new ArrayList<>();
//    List<String> request3 = new ArrayList<>();

    public Request(){
        this.request1.add("answer");
        this.request1.add("gameOver");
        this.request1.add("timeCount");

        this.request2.add("rollBack");
        this.request2.add("clear");
        this.request2.add("all_clear");
        this.request2.add("push");
        this.request2.add("sendCoordinate");

//        this.request3.add("nextTurn");
//        this.request3.add("gameStart");
//        this.request3.add("addFriendRequest");
//        this.request3.add("addFriendResponse");
//        this.request3.add("matchingStartDrowGame");
//        this.request3.add("matchingCancleDrowGame");
//        this.request3.add("sendMessage");
    }
}
