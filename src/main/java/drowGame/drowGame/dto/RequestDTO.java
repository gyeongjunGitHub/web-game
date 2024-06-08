package drowGame.drowGame.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter
public class RequestDTO {
    private String request;
    private String receiver;
    private String data;
    private LocalDateTime date;

}
