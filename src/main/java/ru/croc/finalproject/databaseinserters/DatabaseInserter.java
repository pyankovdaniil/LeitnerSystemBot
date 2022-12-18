package ru.croc.finalproject.databaseinserters;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.croc.finalproject.models.Box;
import ru.croc.finalproject.models.Card;
import ru.croc.finalproject.models.Theme;
import ru.croc.finalproject.services.ThemeService;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@Transactional
@Slf4j
public class DatabaseInserter {
    private final ThemeService themeService;

    @Autowired
    public DatabaseInserter(ThemeService themeService) {
        this.themeService = themeService;
    }

    public void insertThemes() {
        themeService.deleteAll();

        String themeFilesLocation = "src/main/resources/themefiles";
        File themeFilesDirectory = new File(themeFilesLocation);
        try {
            for (File file : Objects.requireNonNull(themeFilesDirectory.listFiles())) {
                List<String> words = new ArrayList<>(Files.lines(Paths
                        .get(themeFilesLocation + '\\' + file.getName()), StandardCharsets.UTF_16).toList());

                Theme theme = new Theme();
                theme.setName(StringUtils.capitalize(words.get(0)));

                List<Box> boxes = new ArrayList<>();
                for (int i = 1; i < 4; i++) {
                    Box box = new Box();
                    box.setNumber(i);
                    box.setTheme(theme);

                    boxes.add(box);
                }

                theme.setBoxes(boxes);
                words.remove(0);

                List<Card> cards = new ArrayList<>();
                for (String line : words) {
                    String[] wordsInLine = line.split(",");

                    Card card = new Card();
                    card.setWord(StringUtils.capitalize(wordsInLine[0]));
                    card.setTranslation(StringUtils.capitalize(wordsInLine[1]));
                    card.setBox(boxes.get(0));

                    cards.add(card);
                }

                boxes.get(0).setCards(cards);

                themeService.saveTheme(theme);
            }
        } catch (NullPointerException ignored) {
        } catch (IOException e) {
            log.error("Exception while working with input files: " + e.getMessage());
        }
    }
}
