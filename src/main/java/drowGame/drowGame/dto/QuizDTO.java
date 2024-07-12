package drowGame.drowGame.dto;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizDTO {
    private int num;
    private String quiz;
    private String answer;
    private int yourTurn;
}
