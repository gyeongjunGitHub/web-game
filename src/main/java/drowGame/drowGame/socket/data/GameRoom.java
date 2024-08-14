package drowGame.drowGame.socket.data;

import drowGame.drowGame.dto.QuizDTO;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

@Getter
@Setter
public class GameRoom {

    //세션 정보
    private List<WebSocketSession> player_session;
    //닉네임
    private List<String> player_nick_name;
    //유저 별 순서
    private List<Integer> turnList;
    //점수
    private List<Double> score;
    //on/offline 상태
    private List<Integer> status;
    //현재 몇 번째 순서인지
    private int turn;
    //Timer
    private Timer timer;
    //quiz 정보
    private QuizDTO quizDTO;
    //cycle
    private int cycle;


    public static String findMemberNickname(WebSocketSession session, GameRoom gameRoom){
        int index = 0;
        for(int i = 0; i<gameRoom.getPlayer_session().size(); i++){
            if(session.equals(gameRoom.getPlayer_session().get(i))){
                index = i;
            }
        }
        return gameRoom.getPlayer_nick_name().get(index);
    }
    public static int getLastTurn(GameRoom gameRoom){
        int lastTurn = 0;
        for(int i = 0; i<gameRoom.getStatus().size(); i++){
            if(gameRoom.getStatus().get(i) == 1){
                lastTurn = gameRoom.getTurnList().get(i);
            }
        }
        return lastTurn;
    }
    public static void updateStatus(String nick_name, GameRoom gameRoom){
        int index = 0;
        for(int i = 0; i<gameRoom.getPlayer_session().size(); i++){
            if(nick_name.equals(gameRoom.getPlayer_nick_name().get(i))){
                index = i;
            }
        }

        gameRoom.getStatus().set(index, 0);
    }
    public static int getStatus(int turn, GameRoom gameRoom){
        return gameRoom.getStatus().get(turn-1);
    }
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
