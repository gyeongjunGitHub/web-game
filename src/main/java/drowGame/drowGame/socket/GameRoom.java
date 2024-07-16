package drowGame.drowGame.socket;

import drowGame.drowGame.dto.QuizDTO;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;

@Getter
@Setter
public class GameRoom {

    //game room 멤버 세션 정보
    private List<WebSocketSession> player_session;
    private List<String> player_nick_name;
    //game room 멤버 턴 정보
    private List<Integer> turnList;
    //game room 멤버 점수 정보
    private List<Double> score;

    private int turn;
    private Timer timer;
    private QuizDTO quizDTO;
    private int cycle;


    public static List<FinalScore> finalScore(GameRoom gameRoom) {
        List<FinalScore> finalScoreList = new ArrayList<>();

        while (!gameRoom.getScore().isEmpty()){
            Double max = gameRoom.getScore().get(0);
            int max_index = 0;
            for (int i = 0; i < gameRoom.getScore().size(); i++) {
                if ((gameRoom.getScore().get(i)) > max){
                    max = gameRoom.getScore().get(i);
                    max_index = i;
                }
            }

            FinalScore finalScore = new FinalScore(gameRoom.player_nick_name.get(max_index), gameRoom.getScore().get(max_index));
            finalScoreList.add(finalScore);
            gameRoom.getPlayer_nick_name().remove(max_index);
            gameRoom.getScore().remove(max_index);
        }
        return finalScoreList;
    }

    @Getter
    @Setter
    public static class FinalScore{
        private String nick_name;
        private Double score;
        public FinalScore(){}
        public FinalScore(String nick_name, Double score){
            this.nick_name = nick_name;
            this.score = score;
        }
    }

}
