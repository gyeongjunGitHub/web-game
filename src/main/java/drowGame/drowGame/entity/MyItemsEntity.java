package drowGame.drowGame.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "my_items")
@Getter
public class MyItemsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private String name;
    @Column
    private int price;
    @Column
    private String member_id;
}
