package org.alz2019.model;

import lombok.Data;
import lombok.ToString;
import org.alz2019.annotation.Column;
import org.alz2019.annotation.Id;
import org.alz2019.annotation.OneToMany;
import org.alz2019.annotation.Table;

import java.util.ArrayList;
import java.util.List;

@Data
@Table("users")
@ToString(exclude = "cards")
public class User {
    @Id
    private Long id;

    @Column("first_name")
    private String firstName;

    @Column("last_name")
    private String lastName;

    @OneToMany
    private List<Card> cards = new ArrayList<>();
}
