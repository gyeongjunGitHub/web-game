package drowGame.drowGame.socket.data;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MatchingInfo {
    private List<String> roomUsers;
    private List<String> roomUsersNickName;
    private String response;
    private int yourTurn;
}
