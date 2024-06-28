package drowGame.drowGame.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "quiz")
@Getter
@Setter
public class QuizEntity {
    @Id
    @Column(name = "num")
    private int num;

    @Column(name = "quiz")
    private String quiz;

    @Column(name = "answer")
    private String answer;
}
