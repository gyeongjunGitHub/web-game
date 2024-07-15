package drowGame.drowGame.socket.manager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import drowGame.drowGame.dto.QuizDTO;
import drowGame.drowGame.entity.QuizEntity;
import drowGame.drowGame.repository.QuizRepository;
import drowGame.drowGame.socket.GameRoom;
import drowGame.drowGame.socket.Turn;
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
    private final ConcurrentLinkedQueue<String> matchingQueue = new ConcurrentLinkedQueue<>();
    private final ConcurrentHashMap<WebSocketSession, GameRoom> gameRoomMap = new ConcurrentHashMap<WebSocketSession, GameRoom>();
    private final ConcurrentHashMap<Integer, Timer> gameRoomTimer = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, Turn> gameRoomTurn = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, QuizDTO> gameRoomQuiz = new ConcurrentHashMap<>();
    private final AtomicInteger roomIdGenerator = new AtomicInteger();
    public boolean addMatchingQueue(String Id){
        this.matchingQueue.add(Id);
        if (this.matchingQueue.size() == 2){
            return true;
        }
        return false;
    }
    public void removeMatchingQueue(String myId){
        matchingQueue.removeIf(s -> s.equals(myId));
    }
    public String pollMatchingQueue(){
        return this.matchingQueue.poll();
    }
    public int getTurn(WebSocketSession wss){
        return this.gameRoomMap.get(wss).getTurn();
    }
    public void createGameRoom(List<WebSocketSession> player_session) {
        int roomId = roomIdGenerator.incrementAndGet();
        int turn = 1;
        for (WebSocketSession ws : player_session){
            GameRoom gameRoom = new GameRoom();
            gameRoom.setRoomId(roomId);
            gameRoom.setTurn(turn++);
            gameRoomMap.put(ws, gameRoom);
        }
        //타이머 생성
        gameRoomTimer.put(roomId, new Timer());
        //턴 정보 생성
        gameRoomTurn.put(roomId, new Turn(1,0));
    }

    public boolean isDuringGame(WebSocketSession mySession){
        for (WebSocketSession wss : gameRoomMap.keySet()) {
            if (mySession.equals(wss)) {
                return true;
            }
        }
        return false;
    }
    public void removeSessionGameRoom(WebSocketSession mySession) {
        for (WebSocketSession wss : gameRoomMap.keySet()) {
            if (mySession.equals(wss)) {
                gameRoomMap.remove(wss);
            }
        }
    }
    public int getRoomMemberCount(int roomId){
        int count = 0;
        for (WebSocketSession wss : gameRoomMap.keySet()){
            if(roomId == gameRoomMap.get(wss).getRoomId()){
                count++;
            }
        }
        return count;
    }
    public int getGameRoomId(WebSocketSession session){
        return gameRoomMap.get(session).getRoomId();
    }
    public List<WebSocketSession> getSameRoomMemberSession(WebSocketSession session){
        List<WebSocketSession> sameRoomMemberSession = new ArrayList<>();

        int myRoomId = gameRoomMap.get(session).getRoomId();
        for (WebSocketSession wss : gameRoomMap.keySet()) {
            if (myRoomId == gameRoomMap.get(wss).getRoomId()) {
                sameRoomMemberSession.add(wss);
            }
        }
        return sameRoomMemberSession;
    }
    public List<WebSocketSession> getSameRoomMemberSession(int roomId){
        List<WebSocketSession> sameRoomMemberSession = new ArrayList<>();
        for (WebSocketSession wss : gameRoomMap.keySet()) {
            if (roomId == gameRoomMap.get(wss).getRoomId()) {
                sameRoomMemberSession.add(wss);
            }
        }
        return sameRoomMemberSession;
    }
    public void sendMessage(WebSocketSession wss, String message){
        try{
            wss.sendMessage(new TextMessage(message));
        }catch (Exception e){
            System.out.println("무언가 에러");
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
    public void sendMessageSameRoom(int num, WebSocketSession session, SocketRequest socketRequest) {
        if (num == 0) {
            for (WebSocketSession wss : getSameRoomMemberSession(session)) {
                sendMessage(wss, dtoToJson(socketRequest));
            }
        }
        if (num == 1) {
            for (WebSocketSession wss : getSameRoomMemberSession(session)){
                if(!session.equals(wss)){
                    sendMessage(wss, dtoToJson(socketRequest));
                }
            }
        }
    }
    public String dtoToJson(Object object){
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    public void startGameRoundTimer(WebSocketSession session){
        List<WebSocketSession> sameRoomMemberSession = getSameRoomMemberSession(session);
        int roomId = getGameRoomId(session);
        int currentTurn = gameRoomTurn.get(roomId).getTurn();

        //보낼 턴 증가 시키기
        gameRoomTurn.put(roomId, Turn.increaseTurn(gameRoomTurn.get(roomId), getRoomMemberCount(roomId)));

        //사이클 체크 합시다 2사이클 까지만 하는걸로
        int maxCycle = 2;
        int turn = gameRoomTurn.get(roomId).getTurn();
        int cycle = gameRoomTurn.get(roomId).getCycle();
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
                    if(cycle == maxCycle && currentTurn == getRoomMemberCount(roomId)){
                        gameRoomTimer.get(roomId).cancel();
                        gameRoomTimer.remove(roomId);
                        gameRoomTimer.put(roomId, new Timer());
                        SocketRequest sr = new SocketRequest();
                        sr.setType("gameOver");
                        sendMessageSameRoom(0, session, sr);
                    }else {
                        gameRoomTimer.get(roomId).cancel();
                        gameRoomTimer.remove(roomId);
                        gameRoomTimer.put(roomId, new Timer());
                        //Quiz, turn 정보 전송
                        SocketRequest sr = new SocketRequest();
                        sr.setType("nextTurn");
                        QuizDTO quiz = getQuizDTO();
                        gameRoomQuiz.put(roomId, quiz);
                        quiz.setYourTurn(turn);
                        sr.setData(quiz);
                        sendMessageSameRoom(0, session, sr);
                        sr.setType("clear");
                        sendMessageSameRoom(0, session, sr);
                    }
                }
            }
        };
        gameRoomTimer.get(roomId).scheduleAtFixedRate(task, 0, 1000);
    }
    public void startBeforeGameTimer(WebSocketSession session){
        int roomId = getGameRoomId(session);
        int turn = gameRoomTurn.get(roomId).getTurn();

        List<WebSocketSession> sameRoomMemberSession = getSameRoomMemberSession(session);
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
                    gameRoomTimer.get(roomId).cancel();
                    gameRoomTimer.remove(roomId);
                    gameRoomTimer.put(roomId, new Timer());

                    //Quiz, turn 정보 전송
                    SocketRequest sr = new SocketRequest();
                    sr.setType("quizData");
                    QuizDTO quiz = getQuizDTO();
                    gameRoomQuiz.put(roomId, quiz);
                    quiz.setYourTurn(turn);
                    sr.setData(quiz);
                    sendMessageSameRoom(0, session, sr);
                }
            }
        };
        gameRoomTimer.get(roomId).scheduleAtFixedRate(task, 0, 1000);
    }


    public void answerCheck(WebSocketSession session, String answer){
        int roomId = getGameRoomId(session);
        List<WebSocketSession> sameRoomMemberSession = getSameRoomMemberSession(session);

        //정답 일 경우
        if(answer.equals(gameRoomQuiz.get(roomId).getAnswer())){
            gameRoomTimer.get(roomId).cancel();
            gameRoomTimer.remove(roomId);
            gameRoomTimer.put(roomId, new Timer());

            int maxCycle = 2;
            int cycle = gameRoomTurn.get(roomId).getCycle();
            int turn = gameRoomTurn.get(roomId).getTurn();
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
                        if(cycle == maxCycle && currentTurn == getRoomMemberCount(roomId)){
                            gameRoomTimer.get(roomId).cancel();
                            gameRoomTimer.remove(roomId);
                            gameRoomTimer.put(roomId, new Timer());
                        }else {
                            gameRoomTimer.get(roomId).cancel();
                            gameRoomTimer.remove(roomId);
                            gameRoomTimer.put(roomId, new Timer());

                            //Quiz, turn 정보 전송
                            SocketRequest sr = new SocketRequest();
                            sr.setType("nextTurn");
                            QuizDTO quiz = getQuizDTO();
                            gameRoomQuiz.put(roomId, quiz);
                            quiz.setYourTurn(turn);
                            sr.setData(quiz);
                            sendMessageSameRoom(0, session, sr);
                            sr.setType("clear");
                            sendMessageSameRoom(0, session, sr);
                        }
                    }
                }
            };
            gameRoomTimer.get(roomId).scheduleAtFixedRate(task, 0, 1000);
        }
    }
    public int getCurrentTurn(int turn, int roomId){
        int roomMemberCount = getRoomMemberCount(roomId);
        if(turn == 1){
            return roomMemberCount;
        }else {
            turn = turn - 1;
            return turn;
        }
    }
    public void removeTimer(WebSocketSession session) {
        int roomId = getGameRoomId(session);
        gameRoomTimer.get(roomId).cancel();
        gameRoomTimer.remove(roomId);
        gameRoomTimer.put(roomId, new Timer());
    }
}
