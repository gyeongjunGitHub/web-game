package drowGame.drowGame.service;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class SocketService {
    public void sendLoginMemberList(WebSocketSession webSocketSession,
                                    HashMap<String, WebSocketSession> sessionMap,
                                    ConcurrentHashMap<String, String> socketSessionAndMemberID) {

        for(String loginMemberKey : sessionMap.keySet()){
            WebSocketSession wss = sessionMap.get(loginMemberKey);
            for(String membersKey : sessionMap.keySet()){
                String memberInfo = "{\"member\" : \"" + socketSessionAndMemberID.get(membersKey) + "\"}";
                try{
                    wss.sendMessage(new TextMessage(memberInfo));
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
}
