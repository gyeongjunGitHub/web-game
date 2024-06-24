package drowGame.drowGame.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter
public class RequestDTO {
    private String request;
    private String response;
    private String receiver;
    private String sender;
    private String data;

    private int x;  //x좌표
    private int y;  //y좌표
    private int lastX;  //lastX좌표
    private int lastY;  //lsatY좌표
    private String color;

}
