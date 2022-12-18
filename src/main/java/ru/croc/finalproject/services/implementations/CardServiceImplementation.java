package ru.croc.finalproject.services.implementations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.croc.finalproject.models.Card;
import ru.croc.finalproject.repositories.CardRepository;
import ru.croc.finalproject.services.CardService;

@Service
@Transactional(readOnly = true)
public class CardServiceImplementation implements CardService {
    private final CardRepository cardRepository;

    @Autowired
    public CardServiceImplementation(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    @Override
    @Transactional
    public void saveCard(Card card) {
        cardRepository.save(card);
    }
}
