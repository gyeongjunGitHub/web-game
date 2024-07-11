package drowGame.drowGame.socket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class GameManager {
    private final ConcurrentLinkedQueue<String> matchingQueue = new ConcurrentLinkedQueue<>();
    private final ConcurrentHashMap<WebSocketSession, GameRoom> gameRoomMap = new ConcurrentHashMap<WebSocketSession, GameRoom>();
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
}
