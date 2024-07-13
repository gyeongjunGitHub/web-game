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

    public ChattingDTO (){}
    public ChattingDTO(ChattingEntity chattingEntity){
        this.id = chattingEntity.getId();
        this.sender = chattingEntity.getSender();
        this.receiver = chattingEntity.getReceiver();
        this.content = chattingEntity.getContent();
        this.date = chattingEntity.getDate().toString();
    }
}
