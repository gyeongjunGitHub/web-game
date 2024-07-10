package drowGame.drowGame.entity;

import drowGame.drowGame.dto.MyItemsDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "my_items")
@Getter
@Setter
public class MyItemsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private String name;
    @Column
    private int price;
    @Column
    private int count;
    @Column
    private String member_id;

    public MyItemsEntity(){}

    public MyItemsEntity(MyItemsDTO m) {
        this.name = m.getName();
        this.price = m.getPrice();
        this.count = m.getCount();
        this.member_id = m.getMember_id();
    }
}
