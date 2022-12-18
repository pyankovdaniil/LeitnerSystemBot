package ru.croc.finalproject.botservice.botutil;

import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.croc.finalproject.botservice.TelegramBot;

@Component
@Slf4j
public class BotMessageSender {
    public static final String HELP_TEXT = EmojiParser.parseToUnicode("""
            Это бот для изучения английских слов по системе Лейтнера. :brain:
                        
            В бот загружены карточки со словами по разным темам.
              :one: Выберите нужную вам тему и нужную группу карточек по частоте повторения.
              :two: Повторите все слова из этой группы и они автоматически распределятся по новым группам.
              :three: Не забывайте вести календарь с датами повторения групп ваших карточек!
                        
            :arrow_right: Нажмите /begin чтобы начать проверять карточки
                        
            Удачи в изучении слов! :green_heart:
            """);

    public void sendMessage(SendMessage message, TelegramBot telegramBot) {
        try {
            telegramBot.execute(message);
        } catch (TelegramApiException e) {
            log.error("Error while sending message occurred: " + e.getMessage());
        }
    }

    public void sendMessage(EditMessageText editMessageText, TelegramBot telegramBot) {
        try {
            telegramBot.execute(editMessageText);
        } catch (TelegramApiException e) {
            log.error("Error while executing edit message occurred: " + e.getMessage());
        }
    }
}
