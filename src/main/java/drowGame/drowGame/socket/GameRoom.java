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
    //member status
    private List<Integer> status;

    private int turn;
    private Timer timer;
    private QuizDTO quizDTO;
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
    public static int getMyTurn(WebSocketSession session, GameRoom gameRoom){
        int index = 0;
        for(int i = 0; i<gameRoom.getPlayer_session().size(); i++){
            if(session.equals(gameRoom.getPlayer_session().get(i))){
                index = i;
            }
        }
        return gameRoom.getTurnList().get(index);
    }
    public static int removeMemberInfo(WebSocketSession session, GameRoom gameRoom){
        int index = 0;
        for(int i = 0; i<gameRoom.getPlayer_session().size(); i++){
            if(session.equals(gameRoom.getPlayer_session().get(i))){
                index = i;
            }
        }

//        System.out.println("게임을 나가는 유저의 turn : " + gameRoom.getTurnList().get(index));

        gameRoom.getPlayer_session().remove(index);
        gameRoom.getPlayer_nick_name().remove(index);
        gameRoom.getScore().remove(index);
        gameRoom.getTurnList().remove(gameRoom.getTurnList().size()-1);

//        System.out.println(gameRoom.getPlayer_session().toString());
//        System.out.println(gameRoom.getPlayer_nick_name().toString());
//        System.out.println(gameRoom.getScore().toString());
//        System.out.println(gameRoom.getTurnList().toString());

        return gameRoom.getPlayer_session().size();
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
