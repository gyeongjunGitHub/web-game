package drowGame.drowGame.socket.TypeProc.Chatting;

import drowGame.drowGame.dto.ChattingDTO;
import drowGame.drowGame.entity.ChattingEntity;
import drowGame.drowGame.repository.ChattingRepository;
import drowGame.drowGame.socket.TypeProc.Message.MessageProc;
import drowGame.drowGame.socket.data.SocketRequest;
import drowGame.drowGame.socket.manager.SocketSessionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.WebSocketSession;

@Component
@RequiredArgsConstructor
public class ChattingProc {
    private final SocketSessionManager sm;
    private final MessageProc messageProc;
    private final ChattingRepository chattingRepository;
    @Transactional
    public void sendChatting(WebSocketSession session, SocketRequest socketRequest) {

        String myId = sm.getMyId(session);
        ChattingDTO chatting = socketRequest.typeChatting(socketRequest);
        chatting.setSender(myId);
        ChattingEntity chattingEntity = new ChattingEntity(chatting);
        ChattingDTO saveResult = new ChattingDTO(chattingRepository.chatContentSave(chattingEntity));
        saveResult.setType("/chatting/sendChatting");

        WebSocketSession[] sessions = new WebSocketSession[2];
        sessions[0] = sm.findReceiverSession(saveResult.getReceiver());
        sessions[1] = sm.findReceiverSession(myId);
        for (WebSocketSession wss : sessions) {
            if (wss != null) {
                messageProc.sendMessage(wss, messageProc.dtoToJson(saveResult));
            }
        }
    }
}
