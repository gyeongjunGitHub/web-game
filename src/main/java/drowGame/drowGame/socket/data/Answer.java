package drowGame.drowGame.socket.data;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Answer {
    private String answer;
    private String sender;
    private int timeCount;
}
