package ru.croc.finalproject.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.croc.finalproject.models.Card;

@Repository
public interface CardRepository extends JpaRepository<Card, Integer> {
}
