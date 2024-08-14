package drowGame.drowGame.socket.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import drowGame.drowGame.dto.ChattingDTO;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SocketRequest {
    private String type;
    private Object data;

    public AddFriend typeAddFriend(SocketRequest socketRequest){
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(socketRequest.getData());
            return objectMapper.readValue(json, AddFriend.class);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    public AddFriendResponse typeAddFriendResponse(SocketRequest socketRequest){
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(socketRequest.getData());
            return objectMapper.readValue(json, AddFriendResponse.class);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    public NextTurn typeNextTurn(SocketRequest socketRequest){
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(socketRequest.getData());
            return objectMapper.readValue(json, NextTurn.class);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    public Answer typeRequest1(SocketRequest socketRequest){
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(socketRequest.getData());
            return objectMapper.readValue(json, Answer.class);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    public ChattingDTO typeChatting(SocketRequest socketRequest){
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(socketRequest.getData());
            return objectMapper.readValue(json, ChattingDTO.class);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
