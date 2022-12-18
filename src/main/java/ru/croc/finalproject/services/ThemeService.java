package ru.croc.finalproject.services;

import ru.croc.finalproject.models.Theme;

import java.util.List;

public interface ThemeService {
    List<Theme> findAll();
    void saveTheme(Theme theme);
    void deleteAll();
}
