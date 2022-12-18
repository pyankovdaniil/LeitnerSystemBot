package ru.croc.finalproject.services.implementations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.croc.finalproject.models.Theme;
import ru.croc.finalproject.repositories.ThemeRepository;
import ru.croc.finalproject.services.ThemeService;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ThemeServiceImplementation implements ThemeService {
    private final ThemeRepository themeRepository;

    @Autowired
    public ThemeServiceImplementation(ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    @Override
    public List<Theme> findAll() {
        return themeRepository.findAll();
    }

    @Override
    @Transactional
    public void saveTheme(Theme theme) {
        themeRepository.save(theme);
    }

    @Override
    @Transactional
    public void deleteAll() {
        themeRepository.deleteAll();
    }
}
