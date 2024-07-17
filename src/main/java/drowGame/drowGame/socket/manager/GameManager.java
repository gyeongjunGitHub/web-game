package drowGame.drowGame.socket.manager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import drowGame.drowGame.dto.QuizDTO;
import drowGame.drowGame.entity.QuizEntity;
import drowGame.drowGame.repository.GameSettingRepository;
import drowGame.drowGame.repository.QuizRepository;
import drowGame.drowGame.service.MemberService;
import drowGame.drowGame.socket.GameRoom;
import drowGame.drowGame.socket.data.SocketRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class GameManager {
    private final QuizRepository quizRepository;
    private final MemberService memberService;
    private final GameSettingRepository gameSettingRepository;
    private final ConcurrentLinkedQueue<String> matchingQueue2Member = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<String> matchingQueue3Member = new ConcurrentLinkedQueue<>();
    private final ConcurrentHashMap<Integer, GameRoom> gameRoomMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<WebSocketSession, Integer> roomIdMap = new ConcurrentHashMap<>();
    private final AtomicInteger roomIdGenerator = new AtomicInteger();
    public boolean addMatchingQueue2Member(String Id){
        this.matchingQueue2Member.add(Id);
        if (this.matchingQueue2Member.size() == 2){
            return true;
        }
        return false;
    }
    public boolean addMatchingQueue3Member(String Id){
        this.matchingQueue3Member.add(Id);
        if (this.matchingQueue3Member.size() == 3){
            return true;
        }
        return false;
    }
    public void removeMatchingQueue2Member(String myId){
        matchingQueue2Member.removeIf(s -> s.equals(myId));
    }
    public void removeMatchingQueue3Member(String myId){
        matchingQueue3Member.removeIf(s -> s.equals(myId));
    }
    public String pollMatchingQueue2Member(){
        return this.matchingQueue2Member.poll();
    }
    public String pollMatchingQueue3Member(){
        return this.matchingQueue3Member.poll();
    }
    public void createGameRoom(List<WebSocketSession> player_session, List<String> memebersNickName){
        int roomId = roomIdGenerator.incrementAndGet();
        GameRoom gameRoom = new GameRoom();

        gameRoom.setPlayer_session(player_session);
        gameRoom.setPlayer_nick_name(memebersNickName);

        int turn = 1;
        double score = 0;
        List<Integer> turnList = new ArrayList<>();
        List<Double> scoreList = new ArrayList<>();
        for (int i = 0; i < player_session.size(); i++){
            turnList.add(turn);
            scoreList.add(score);
            turn++;
        }
        gameRoom.setTurnList(turnList);
        gameRoom.setScore(scoreList);
        gameRoom.setTurn(1);
        gameRoom.setTimer(new Timer());
        gameRoom.setCycle(0);

        //gameRoomMap1 에 정보 추가
        gameRoomMap.put(roomId, gameRoom);
        //roomIdMap 에 정보 추가
        for(WebSocketSession session : player_session){
            roomIdMap.put(session, roomId);
        }
    }
    public int gameRoomMemberCount(int roomId) {
        return gameRoomMap.get(roomId).getPlayer_session().size();
    }
    public int getGameRoomId(WebSocketSession session){
        return roomIdMap.get(session);
    }
    public int getGameTurn(int roomId) {
        return gameRoomMap.get(roomId).getTurn();
    }
    public List<Integer> getMyTurn(int roomId) {
        return gameRoomMap.get(roomId).getTurnList();
    }
    public List<WebSocketSession> getPlayerSession(int roomId){
        return gameRoomMap.get(roomId).getPlayer_session();
    }
    public void startBeforeGameTimer(WebSocketSession session){
        int roomId = getGameRoomId(session);
        int turn = getGameTurn(roomId);
        List<WebSocketSession> sameRoomMemberSession = getPlayerSession(roomId);
        /////////////////////////////////////////////
        TimerTask task = new TimerTask() {
            int count = 3;
            @Override
            public void run() {
                if (count >= 0) {
                    for(WebSocketSession wss : sameRoomMemberSession){
                        String roomIsFull = "{\"timeCount\" : \"" + count + "\"}";
                        sendMessage(wss, roomIsFull);
                    }
                    count--;
                } else {
                    //캔슬 후 재사용이 불가능 삭제 후 다시 충전 ㅋㅋ
                    gameRoomMap.get(roomId).getTimer().cancel();
                    gameRoomMap.get(roomId).setTimer(new Timer());

                    //Quiz, turn 정보 전송
                    SocketRequest sr = new SocketRequest();
                    sr.setType("quizData");
                    QuizDTO quiz = getQuizDTO();

                    gameRoomMap.get(roomId).setQuizDTO(quiz);

                    quiz.setYourTurn(turn);
                    sr.setData(quiz);
                    sendMessageSameRoom(0, session, sr);
                }
            }
        };
        gameRoomMap.get(roomId).getTimer().scheduleAtFixedRate(task, 0, 1000);
    }
    public void startGameRoundTimer(WebSocketSession session){
        int round_time = gameSettingRepository.findByName("round_time").getValue();

        int roomId = getGameRoomId(session);
        int currentTurn = getGameTurn(roomId);
        List<WebSocketSession> sameRoomMemberSession = getPlayerSession(roomId);

        //보낼 턴 증가 시키기
        increaseTurn(roomId);

        int maxCycle = 2;
        int turn = getGameTurn(roomId);
        int cycle = gameRoomMap.get(roomId).getCycle();
        TimerTask task = new TimerTask() {
            int count = round_time;
            @Override
            public void run() {
                if (count >= 0) {
                    for(WebSocketSession wss : sameRoomMemberSession){
                        String roomIsFull = "{\"timeCount\" : \"" + count + "\"}";
                        sendMessage(wss, roomIsFull);
                    }
                    count--;
                } else {
                    if(cycle == maxCycle && currentTurn == gameRoomMemberCount(roomId)){
                        gameOverProc(roomId, session);
                    }else {
                        nextTurnProc(roomId, turn, session);
                    }
                }
            }
        };
        gameRoomMap.get(roomId).getTimer().scheduleAtFixedRate(task, 0, 1000);
    }
    public void answerCheck(WebSocketSession session, String answer, int timeCount){
        int roomId = getGameRoomId(session);
        List<WebSocketSession> sameRoomMemberSession = getPlayerSession(roomId);
        //정답 일 경우
        if(answer.equals(gameRoomMap.get(roomId).getQuizDTO().getAnswer())){
            gameRoomMap.get(roomId).getTimer().cancel();
            gameRoomMap.get(roomId).setTimer(new Timer());

            double score = 0;
            score = (double) timeCount /6;

            //맞춘사람 index
            int index = 1000;
            for(int i = 0; i<gameRoomMemberCount(roomId); i++){
                if (session.equals(gameRoomMap.get(roomId).getPlayer_session().get(i))){
                    index = i;
                }
            }

            //score set
            gameRoomMap.get(roomId).getScore().set(index, gameRoomMap.get(roomId).getScore().get(index) + score);

            SocketRequest sr = new SocketRequest();
            sr.setType("score");
            sr.setData(gameRoomMap.get(roomId).getScore());
            sendMessageSameRoom(0, session, sr);

            int maxCycle = 2;
            int cycle = gameRoomMap.get(roomId).getCycle();
            int turn = getGameTurn(roomId);
            int currentTurn = getCurrentTurn(turn, roomId);
            TimerTask task = new TimerTask() {
                int count = 5;
                @Override
                public void run() {
                    if (count >= 0) {
                        for(WebSocketSession wss : sameRoomMemberSession){
                            String roomIsFull = "{\"timeCount\" : \"" + count + "\"}";
                            sendMessage(wss, roomIsFull);
                        }
                        count--;
                    } else {
                        if(cycle == maxCycle && currentTurn == gameRoomMemberCount(roomId)){
                            gameOverProc(roomId, session);
                        }else {
                            nextTurnProc(roomId, turn, session);
                        }
                    }
                }
            };
            gameRoomMap.get(roomId).getTimer().scheduleAtFixedRate(task, 0, 1000);
        }
    }
    public void sendMessageSameRoom(int num, WebSocketSession session, SocketRequest socketRequest) {
        int roomId = getGameRoomId(session);
        if (num == 0) {
            for (WebSocketSession wss : getPlayerSession(roomId)) {
                sendMessage(wss, dtoToJson(socketRequest));
            }
        }
        if (num == 1) {
            for (WebSocketSession wss : getPlayerSession(roomId)){
                if(!session.equals(wss)){
                    sendMessage(wss, dtoToJson(socketRequest));
                }
            }
        }
    }
    public void increaseTurn(int roomId){
        int turn = getGameTurn(roomId);
        int cycle = gameRoomMap.get(roomId).getCycle();
        int memberCount = gameRoomMemberCount(roomId);
        if(turn == 1){
            gameRoomMap.get(roomId).setCycle(cycle+1);
        }

        if(turn + 1 > memberCount){
            gameRoomMap.get(roomId).setTurn(1);
        }else {
            gameRoomMap.get(roomId).setTurn(turn + 1);
        }
    }
    public int getCurrentTurn(int turn, int roomId){
        int roomMemberCount = gameRoomMemberCount(roomId);
        if(turn == 1){
            return roomMemberCount;
        }else {
            turn = turn - 1;
            return turn;
        }
    }
    public void sendMessage(WebSocketSession wss, String message){
        try{
            wss.sendMessage(new TextMessage(message));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public QuizDTO getQuizDTO() {
        int min = 1;
        int max = 7955;
        int randomQuizNumber = ThreadLocalRandom.current().nextInt(min, max + 1);

        QuizEntity quiz = quizRepository.getQuizEntity(randomQuizNumber);

        QuizDTO quizDTO = new QuizDTO();
        quizDTO.setNum(quiz.getNum());
        quizDTO.setQuiz(quiz.getQuiz());
        quizDTO.setAnswer(quiz.getAnswer());
        return quizDTO;
    }
    public String dtoToJson(Object object){
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    public void gameOverProc(int roomId, WebSocketSession session){
        gameRoomMap.get(roomId).getTimer().cancel();
        gameRoomMap.get(roomId).setTimer(new Timer());

        SocketRequest sr = new SocketRequest();
        sr.setType("finalScore");
        List<GameRoom.FinalScore> finalScoreList = GameRoom.finalScore(gameRoomMap.get(roomId));
        sr.setData(finalScoreList);
        sendMessageSameRoom(0, session, sr);

        //2명이서 게임
        if(finalScoreList.size() == 2){
            //1등 ranking_point +10, game_point +40
            //2등 ranking_point +5 , game_point +30
            for (int i = 0; i < 2; i++){
                String nick_name = finalScoreList.get(i).getNick_name();
                if(i == 0){
                    memberService.updateRankingAndGamePoint(nick_name, 10, 40);
                }
                if(i == 1){
                    memberService.updateRankingAndGamePoint(nick_name, 5, 30);
                }
            }
        }
        if(finalScoreList.size() == 3){
            //1등 ranking_point +11, game_point +55
            //2등 ranking_point +8 , game_point +45
            //3등 ranking_point +6 , game_point +35
            for (int i = 0; i < 3; i++){
                String nick_name = finalScoreList.get(i).getNick_name();
                if(i == 0){
                    memberService.updateRankingAndGamePoint(nick_name, 11, 55);
                }
                if(i == 1){
                    memberService.updateRankingAndGamePoint(nick_name, 8, 45);
                }
                if(i == 2){
                    memberService.updateRankingAndGamePoint(nick_name, 6, 35);
                }
            }
        }
        if(finalScoreList.size() == 4){
            //1등 ranking_point +16, game_point +58
            //2등 ranking_point +12 , game_point +50
            //3등 ranking_point +9 , game_point +43
            //3등 ranking_point +7 , game_point +37
        }
        //gameRoom 제거
        removeGameRoom(roomId);
    }
    public void removeGameRoom(int roomId){
        gameRoomMap.remove(roomId);
    }


    ///////////////////////////////////////////////////////////////////////////////////
//    public boolean isDuringGame(WebSocketSession mySession){
//        for (WebSocketSession wss : gameRoomMap.keySet()) {
//            if (mySession.equals(wss)) {
//                return true;
//            }
//        }
//        return false;
//    }
//    public void removeSessionGameRoom(WebSocketSession mySession) {
//        for (WebSocketSession wss : gameRoomMap.keySet()) {
//            if (mySession.equals(wss)) {
//                gameRoomMap.remove(wss);
//            }
//        }
//    }
    public void nextTurnProc(int roomId, int turn, WebSocketSession session){
        gameRoomMap.get(roomId).getTimer().cancel();
        gameRoomMap.get(roomId).setTimer(new Timer());

        //Quiz, turn 정보 전송
        SocketRequest sr = new SocketRequest();
        sr.setType("nextTurn");
        QuizDTO quiz = getQuizDTO();

        gameRoomMap.get(roomId).setQuizDTO(quiz);

        quiz.setYourTurn(turn);

        //퀴즈 전송
        sr.setData(quiz);
        sendMessageSameRoom(0, session, sr);

        //clear 전송
        sr.setType("clear");
        sendMessageSameRoom(0, session, sr);
    }
}
