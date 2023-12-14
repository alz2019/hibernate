package org.alz2019.model;

import lombok.Data;
import org.alz2019.annotation.Column;
import org.alz2019.annotation.Id;
import org.alz2019.annotation.ManyToOne;
import org.alz2019.annotation.Table;

@Data
@Table("cards")
public class Card {
    @Id
    private Long id;

    private String number;

    @ManyToOne
    @Column("user_id")
    private User user;
}
