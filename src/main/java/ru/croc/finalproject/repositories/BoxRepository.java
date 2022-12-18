package ru.croc.finalproject.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.croc.finalproject.models.Box;

@Repository
public interface BoxRepository extends JpaRepository<Box, Integer> {
}
