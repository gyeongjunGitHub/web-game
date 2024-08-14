package drowGame.drowGame.socket.TypeProc.Message;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Component
public class MessageProc {
    public void sendMessage(WebSocketSession wss, String message){
        try{
            if(wss != null){
                wss.sendMessage(new TextMessage(message));
            }
        }catch (Exception e){
            e.printStackTrace();
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
}
