package drowGame.drowGame.socket;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class SocketRequest {
    private String request;
    private String response;
    private String receiver;
    private String sender;
    private String data;

    private int[] coordinate;
    private String color;

    private int yourTurn;
    private int cycle;

    private String quiz;
    private String answer;


    private List<String> roomUsers;
    private List<String> roomUsersNickName;

}
