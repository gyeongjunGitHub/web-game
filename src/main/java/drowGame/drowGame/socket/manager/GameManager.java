package drowGame.drowGame.socket.manager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import drowGame.drowGame.dto.QuizDTO;
import drowGame.drowGame.entity.QuizEntity;
import drowGame.drowGame.repository.GameSettingRepository;
import drowGame.drowGame.repository.QuizRepository;
import drowGame.drowGame.service.MemberService;
import drowGame.drowGame.socket.GameRoom;
import drowGame.drowGame.socket.data.MatchingInfo;
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

    // 2인 게임 매칭 큐
    private final ConcurrentLinkedQueue<String> matchingQueue2Member = new ConcurrentLinkedQueue<>();
    // 3인 게임 매칭 큐
    private final ConcurrentLinkedQueue<String> matchingQueue3Member = new ConcurrentLinkedQueue<>();
    // 생성된 game room 담을 map
    private final ConcurrentHashMap<Integer, GameRoom> gameRoomMap = new ConcurrentHashMap<>();

    // member 의 rooId 값을 찾기 위해 WebSocketSession, roomId 저장
    private final ConcurrentHashMap<WebSocketSession, Integer> roomIdMap = new ConcurrentHashMap<>();

    // roomId generator
    private final AtomicInteger roomIdGenerator = new AtomicInteger();

    public List<String> getQueueSize(int inGameMemberSize){
        if(inGameMemberSize == 2){
            List<String> queueMember = new ArrayList<>();
            for(String member : matchingQueue2Member){
                queueMember.add(member);
            }
            return queueMember;
        }
        if (inGameMemberSize == 3){
            List<String> queueMember = new ArrayList<>();
            for(String member : matchingQueue3Member){
                queueMember.add(member);
            }
            return queueMember;
        }
        return null;
    }
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
        int status = 1;
        List<Integer> turnList = new ArrayList<>();
        List<Double> scoreList = new ArrayList<>();
        List<Integer> statusList = new ArrayList<>();
        for (int i = 0; i < player_session.size(); i++){
            turnList.add(turn);
            scoreList.add(score);
            statusList.add(status);
            turn++;
        }
        gameRoom.setTurnList(turnList);
        gameRoom.setScore(scoreList);
        gameRoom.setStatus(statusList);
        gameRoom.setTurn(1);
        gameRoom.setTimer(new Timer());
        gameRoom.setCycle(0);

        //gameRoomMap 에 정보 추가
        gameRoomMap.put(roomId, gameRoom);
        //roomIdMap 에 정보 추가
        for(WebSocketSession session : player_session){
            roomIdMap.put(session, roomId);
        }
    }
    public int gameRoomMemberCount(int roomId) {
        return gameRoomMap.get(roomId).getPlayer_session().size();
    }
    public int gameRoomOnlineMemberCount(int roomId){
        int count = 0;
        List<Integer> statusList = gameRoomMap.get(roomId).getStatus();
        for(int i = 0; i<statusList.size(); i++){
            if(statusList.get(i) == 1){
                count++;
            }
        }
        return count;
    }
    public int getGameRoomId(WebSocketSession session){
        return roomIdMap.get(session);
    }
    public int getGameTurn(int roomId) {
        return gameRoomMap.get(roomId).getTurn();
    }
    public List<Integer> getTurnList(int roomId) {
        return gameRoomMap.get(roomId).getTurnList();
    }
    public List<WebSocketSession> getPlayerSession(int roomId){
        return gameRoomMap.get(roomId).getPlayer_session();
    }
    public List<Integer> getStatusList(int roomId){
        return gameRoomMap.get(roomId).getStatus();
    }
    public void startBeforeGameTimer(WebSocketSession session){
        int roomId = getGameRoomId(session);
        List<WebSocketSession> sameRoomMemberSession = getPlayerSession(roomId);
        /////////////////////////////////////////////
        TimerTask task = new TimerTask() {
            int count = 3;
            @Override
            public void run() {
                if (count >= 0) {
                    List<WebSocketSession> playerSession = getPlayerSession(roomId);
                    List<Integer> statusList = getStatusList(roomId);
                    for(int i = 0; i<playerSession.size(); i++){
                        if(statusList.get(i) == 1){
                            getStatusList(roomId);
                            String roomIsFull = "{\"timeCount\" : \"" + count + "\"}";
                            sendMessage(playerSession.get(i), roomIsFull);
                        }
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

                    quiz.setYourTurn(getGameTurn(roomId));
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

        //보낼 턴 증가 시키기
        increaseTurn(roomId);

        int maxCycle = 2;
        int cycle = gameRoomMap.get(roomId).getCycle();
        TimerTask task = new TimerTask() {
            int count = round_time;
            @Override
            public void run() {
                if (count >= 0) {
                    List<WebSocketSession> playerSession = getPlayerSession(roomId);
                    List<Integer> statusList = getStatusList(roomId);
                    for(int i = 0; i<playerSession.size(); i++){
                        if(statusList.get(i) == 1){
                            getStatusList(roomId);
                            String roomIsFull = "{\"timeCount\" : \"" + count + "\"}";
                            sendMessage(playerSession.get(i), roomIsFull);
                        }
                    }
                    count--;
                } else {
                    if(cycle == maxCycle && currentTurn == GameRoom.getLastTurn(gameRoomMap.get(roomId))){
                        gameOverProc(roomId, session);
                    }else {
                        if(GameRoom.getStatus(getGameTurn(roomId), gameRoomMap.get(roomId)) == 0){
                            increaseTurn(roomId);
                        }
                        nextTurnProc(roomId, getGameTurn(roomId), session);
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
                        List<WebSocketSession> playerSession = getPlayerSession(roomId);
                        List<Integer> statusList = getStatusList(roomId);
                        for(int i = 0; i<playerSession.size(); i++){
                            if(statusList.get(i) == 1){
                                getStatusList(roomId);
                                String roomIsFull = "{\"timeCount\" : \"" + count + "\"}";
                                sendMessage(playerSession.get(i), roomIsFull);
                            }
                        }
                        count--;
                    } else {
                        if(cycle == maxCycle && currentTurn == GameRoom.getLastTurn(gameRoomMap.get(roomId))){
                            gameOverProc(roomId, session);
                        }else {
                            if(GameRoom.getStatus(getGameTurn(roomId), gameRoomMap.get(roomId)) == 0){
                                increaseTurn(roomId);
                            }
                            nextTurnProc(roomId, getGameTurn(roomId), session);
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
            List<WebSocketSession> playerSession = getPlayerSession(roomId);
            List<Integer> statusList = getStatusList(roomId);
            for(int i = 0; i<getPlayerSession(roomId).size(); i++){
                if(statusList.get(i) == 1){
                    sendMessage(playerSession.get(i), dtoToJson(socketRequest));
                }
            }
        }
        if (num == 1) {
            List<WebSocketSession> playerSession = getPlayerSession(roomId);
            List<Integer> statusList = getStatusList(roomId);
            for(int i = 0; i<getPlayerSession(roomId).size(); i++){
                if(statusList.get(i) == 1){
                    sendMessage(playerSession.get(i), dtoToJson(socketRequest));
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
            if(wss != null){
                wss.sendMessage(new TextMessage(message));
            }
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
        for (WebSocketSession session : roomIdMap.keySet()){
            if(roomIdMap.get(session) == roomId){
                roomIdMap.remove(session);
            }
        }
    }

    public boolean isDuringGame(WebSocketSession mySession){
        if(roomIdMap.get(mySession) == null){
            return false;
        }
        return true;
    }
    public void leaveGameProc(WebSocketSession mySession){
        int roomId = getGameRoomId(mySession);
        String nick_name = GameRoom.findMemberNickname(mySession, gameRoomMap.get(roomId));
        GameRoom.updateStatus(nick_name, gameRoomMap.get(roomId));

        if(gameRoomOnlineMemberCount(roomId) <= 1){
            gameRoomMap.get(roomId).getTimer().cancel();
            SocketRequest sr = new SocketRequest();
            sr.setType("alone");
            sendMessageSameRoom(1, mySession, sr);
            removeGameRoom(roomId);
        }

    }
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
