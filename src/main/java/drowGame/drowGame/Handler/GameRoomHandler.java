package drowGame.drowGame.Handler;

import lombok.Getter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class GameRoomHandler {
    private final ConcurrentHashMap<Integer, GameRoom> gameRooms = new ConcurrentHashMap<Integer, GameRoom>();
    private final AtomicInteger roomIdGenerator = new AtomicInteger();

    public void createGameRoom(String player1, String player2){
        int roomId = roomIdGenerator.incrementAndGet();
        GameRoom gameRoom = new GameRoom(roomId, player1, player2);
        gameRooms.put(roomId, gameRoom);
        for(GameRoom g : gameRooms.values()){
            System.out.println(g.getRoomId());
            System.out.println(g.getPlayer1());
            System.out.println(g.getPlayer2());
        }
        return;
    }

}

@Getter
class GameRoom{
    private final int roomId;
    private final String player1;
    private final String player2;

    public GameRoom(int roomId, String player1, String player2) {
        this.roomId = roomId;
        this.player1 = player1;
        this.player2 = player2;
    }
    public int getRoomId() {
        return roomId;
    }
    public String getPlayer1() {
        return player1;
    }
    public String getPlayer2() {
        return player2;
    }

}