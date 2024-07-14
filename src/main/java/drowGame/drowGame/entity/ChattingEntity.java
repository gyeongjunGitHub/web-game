package drowGame.drowGame.entity;

import drowGame.drowGame.dto.ChattingDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "chatting")
@Getter
@Setter
public class ChattingEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sender")
    private String sender;

    @Column(name = "receiver")
    private String receiver;

    @Column(name = "content")
    private String content;

    @Column(name = "date")
    private LocalDateTime date;

    @Column(name = "receiver_is_read")
    private boolean receiver_is_read;

    public ChattingEntity(){}
    public ChattingEntity(ChattingDTO chattingDTO){
        this.sender = chattingDTO.getSender();
        this.receiver = chattingDTO.getReceiver();
        this.content = chattingDTO.getContent();
        date = LocalDateTime.now();
    }
}
