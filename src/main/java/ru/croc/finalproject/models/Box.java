package ru.croc.finalproject.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "box")
@Getter
@Setter
public class Box {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "number")
    private int number;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "theme_id")
    private Theme theme;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "box_id")
    private List<Card> cards;

    @Override
    public String toString() {
        return "Box{" +
                "id=" + id +
                ", number=" + number +
                ", theme=" + theme +
                '}';
    }
}
