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
    @Column(name = "original_file_name")
    private String original_file_name;
    @Column(name = "stored_file_name")
    private String stored_file_name;
    @Column
    private String member_id;

    public MyItemsEntity(){}

    public MyItemsEntity(MyItemsDTO m) {
        this.name = m.getName();
        this.price = m.getPrice();
        this.count = m.getCount();
        this.original_file_name = m.getOriginal_file_name();
        this.stored_file_name = m.getStored_file_name();
        this.member_id = m.getMember_id();
    }
}
