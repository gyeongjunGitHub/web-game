package drowGame.drowGame.dto;

import drowGame.drowGame.entity.ChattingEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class ChattingDTO {
    private String type = "chattingData";
    private Long id;
    private String sender;
    private String receiver;
    private String content;
    private String date;
    private boolean receiver_is_read;

    public ChattingDTO (){}
    public ChattingDTO(ChattingEntity chattingEntity){
        this.id = chattingEntity.getId();
        this.sender = chattingEntity.getSender();
        this.receiver = chattingEntity.getReceiver();
        this.content = chattingEntity.getContent();
        this.date = chattingEntity.getDate().toString();
        this.receiver_is_read = chattingEntity.isReceiver_is_read();

    }
}
