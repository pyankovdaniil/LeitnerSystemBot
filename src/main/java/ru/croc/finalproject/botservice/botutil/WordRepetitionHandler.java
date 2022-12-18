package ru.croc.finalproject.botservice.botutil;

import com.vdurmont.emoji.EmojiParser;
import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.croc.finalproject.botservice.TelegramBot;
import ru.croc.finalproject.models.Box;
import ru.croc.finalproject.models.Card;
import ru.croc.finalproject.models.Theme;
import ru.croc.finalproject.services.CardService;
import ru.croc.finalproject.services.ThemeService;

import java.util.ArrayList;
import java.util.List;

@Component
@Transactional
@Setter
@Getter
public class WordRepetitionHandler {
    private final ThemeService themeService;
    private final CardService cardService;
    private final BotMessageSender botMessageSender;

    private List<Theme> themes;
    private Theme chosenTheme;
    private Box chosenBox;

    private List<Card> moveBackCards;
    private List<Card> moveForwardCards;

    @Autowired
    public WordRepetitionHandler(ThemeService themeService, CardService cardService,
                                 BotMessageSender botMessageSender) {
        this.themeService = themeService;
        this.cardService = cardService;
        this.botMessageSender = botMessageSender;
    }

    public void selectTheme(long chatId, TelegramBot telegramBot) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));

        message.setText("""
                Отлично! Перед тем, как повторять слова, выберите тему:
                """);

        if (themes == null) {
            List<Theme> themes = themeService.findAll();
            setThemes(themes);
        }

        List<String> buttonsText = new ArrayList<>();
        List<String> buttonsCallbackData = new ArrayList<>();

        for (Theme theme : themes) {
            buttonsText.add(EmojiParser.parseToUnicode(":brain: " + theme.getName()));
            buttonsCallbackData.add("selected_theme " + theme.getName());
        }

        setButtonsAndExecute(message, buttonsText, buttonsCallbackData, telegramBot);
    }

    public void selectBox(Theme theme, EditMessageText editMessageText, TelegramBot telegramBot) {
        editMessageText.setText("""
                Отлично! Теперь выберите номер коробки:
                """);

        List<Box> boxes = theme.getBoxes();

        List<String> buttonsText = new ArrayList<>();
        List<String> buttonsCallbackData = new ArrayList<>();

        for (Box box : boxes) {
            buttonsText.add(EmojiParser.parseToUnicode(":card_file_box: " + box.getNumber() + " (Кол-во слов: " +
                    box.getCards().size() + ")"));
            buttonsCallbackData.add("selected_box " + box.getId());
        }

        setButtonsAndExecute(editMessageText, buttonsText, buttonsCallbackData, telegramBot);
    }

    public void showWord(Box box, int repeatedWords, EditMessageText editMessageText, TelegramBot telegramBot) {
        editMessageText.setParseMode(ParseMode.HTML);

        if (repeatedWords == box.getCards().size()) {
            if (repeatedWords != 0) {
                editMessageText.setText(EmojiParser.parseToUnicode("""
                        :tada: <b>Поздравляем! Все карточки из текущей коробки были повторены!</b> :tada:
                                                
                        :one: Информация о карточках была успешно обновлена
                        :two: Не забывайте проверять даты повторения других коробок
                        """));

                if (box.getNumber() != 1) {
                    Box newBox = chosenTheme.getBoxes().get(box.getNumber() - 2);
                    newBox.getCards().addAll(moveBackCards);
                    box.getCards().removeAll(moveBackCards);
                    for (Card card : moveBackCards) {
                        card.setBox(newBox);
                        cardService.saveCard(card);
                    }
                }

                if (box.getNumber() != 3) {
                    Box newBox = chosenTheme.getBoxes().get(box.getNumber());
                    newBox.getCards().addAll(moveForwardCards);
                    box.getCards().removeAll(moveForwardCards);
                    for (Card card : moveForwardCards) {
                        card.setBox(newBox);
                        cardService.saveCard(card);
                    }
                }
            } else {
                editMessageText.setText(EmojiParser.parseToUnicode("""
                        :frowning: <b>Упс! В этой коробке нет никаких слов...</b> :frowning:
                                                
                        Либо все слова из этой коробки выучены, либо сюда не попало еще ни одного слова
                        """));
            }
            botMessageSender.sendMessage(editMessageText, telegramBot);
            return;
        }

        List<Card> cards = box.getCards();

        Card currentCard = cards.get(repeatedWords);

        editMessageText.setText(EmojiParser.parseToUnicode(":hourglass_flowing_sand: <u>Слово №"
                + (repeatedWords + 1) + " из " + cards.size() + "</u> :hourglass_flowing_sand:\n\n" +
                "И это слово: :point_right: <b><i>" + currentCard.getWord() + "</i></b> :point_left:\n\n" +
                "Вспомни перевод этого слова и затем нажми на кнопку \"Показать перевод\":point_down:"));


        List<String> buttonsText = List.of(EmojiParser.parseToUnicode(":thinking: Показать перевод :thinking:"));
        List<String> buttonsCallbackData = List.of("show_translate " + repeatedWords);

        setButtonsAndExecute(editMessageText, buttonsText, buttonsCallbackData, telegramBot);
    }

    @Synchronized
    public void showWordTranslate(Box box, int repeatedWords, EditMessageText editMessageText,
                                  TelegramBot telegramBot) {
        List<Card> cards = box.getCards();

        Card currentCard = cards.get(repeatedWords);

        editMessageText.setParseMode(ParseMode.HTML);
        editMessageText.setText(EmojiParser.parseToUnicode(":hourglass_flowing_sand: <u>Слово №" +
                (repeatedWords + 1) + " из " + cards.size() + "</u> :hourglass_flowing_sand:\n\nИ его перевод: " +
                ":point_right: <b><i>" + currentCard.getTranslation() + "</i></b> :point_left:\n\n" +
                "Ты ответил правильно?\nЕсли да, то " + "жми :white_check_mark:, если нет, то :x:"));

        List<String> buttonsText = List.of(EmojiParser.parseToUnicode(":white_check_mark: Правильно"),
                EmojiParser.parseToUnicode(":x: Неправильно"));

        String callbackData = "show_word " + (repeatedWords + 1);
        List<String> buttonsCallbackData = List.of(callbackData + " correct", callbackData + " incorrect");

        if (repeatedWords == 0) {
            moveBackCards = new ArrayList<>();
            moveForwardCards = new ArrayList<>();
        }

        setButtonsAndExecute(editMessageText, buttonsText, buttonsCallbackData, telegramBot);
    }

    public void moveCardBack(Card card) {
        if (chosenBox.getNumber() != 1) {
            moveBackCards.add(card);
        }
    }

    public void moveCardForward(Card card) {
        if (chosenBox.getNumber() != 3) {
            moveForwardCards.add(card);
        }
    }

    private InlineKeyboardMarkup setButtons(List<String> buttonsText, List<String> buttonsCallbackData) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> allButtons = new ArrayList<>();

        for (int i = 0; i < buttonsText.size(); i++) {
            List<InlineKeyboardButton> buttons = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(buttonsText.get(i));
            button.setCallbackData(buttonsCallbackData.get(i));
            buttons.add(button);

            allButtons.add(buttons);
        }

        markup.setKeyboard(allButtons);

        return markup;
    }

    private void setButtonsAndExecute(SendMessage sendMessage, List<String> buttonsText,
                                      List<String> buttonsCallbackData, TelegramBot telegramBot) {
        sendMessage.setReplyMarkup(setButtons(buttonsText, buttonsCallbackData));
        botMessageSender.sendMessage(sendMessage, telegramBot);
    }

    private void setButtonsAndExecute(EditMessageText editMessageText, List<String> buttonsText,
                                      List<String> buttonsCallbackData, TelegramBot telegramBot) {
        editMessageText.setReplyMarkup(setButtons(buttonsText, buttonsCallbackData));
        botMessageSender.sendMessage(editMessageText, telegramBot);
    }
}
