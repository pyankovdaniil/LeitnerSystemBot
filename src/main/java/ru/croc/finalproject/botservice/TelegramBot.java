package ru.croc.finalproject.botservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.croc.finalproject.botservice.botutil.BotMessageSender;
import ru.croc.finalproject.botservice.botutil.WordRepetitionHandler;
import ru.croc.finalproject.config.BotConfig;
import ru.croc.finalproject.databaseinserters.DatabaseInserter;
import ru.croc.finalproject.models.Box;
import ru.croc.finalproject.models.Card;
import ru.croc.finalproject.models.Theme;

import java.util.List;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {
    private final BotConfig botConfig;
    private final BotMessageSender messageSender;
    private final WordRepetitionHandler wordRepetitionHandler;

    @Autowired
    public TelegramBot(BotConfig botConfig, BotMessageSender messageSender, DatabaseInserter databaseInserter,
                       WordRepetitionHandler wordRepetitionHandler) {
        this.botConfig = botConfig;
        this.messageSender = messageSender;
        this.wordRepetitionHandler = wordRepetitionHandler;

        databaseInserter.insertThemes();

        List<BotCommand> menuCommands = List.of(new BotCommand("/start", "Get a welcome message"),
                new BotCommand("/help", "Info how to use this bot"),
                new BotCommand("/begin", "Start repetition of words"));

        try {
            this.execute(new SetMyCommands(menuCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error while setting menu commands: " + e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getBotToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            String userName = update.getMessage().getChat().getFirstName();

            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));

            switch (messageText) {
                case "/start" -> {
                    message.setText("Привет, " + userName + "! Добро пожаловать :)");
                    messageSender.sendMessage(message, this);
                }
                case "/help" -> {
                    message.setText(BotMessageSender.HELP_TEXT);
                    messageSender.sendMessage(message, this);
                }
                case "/begin" -> wordRepetitionHandler.selectTheme(chatId, this);
                default -> {
                    message.setText("Sorry, I don't know this command");
                    messageSender.sendMessage(message, this);
                }
            }
        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            EditMessageText editMessageText = new EditMessageText();
            editMessageText.setChatId(String.valueOf(chatId));
            editMessageText.setMessageId((int) messageId);

            String[] splitCallbackData = callbackData.split(" ");

            List<Theme> themes = wordRepetitionHandler.getThemes();

            switch (splitCallbackData[0]) {
                case "selected_theme" -> {
                    for (Theme theme : themes) {
                        if (theme.getName().equals(splitCallbackData[1])) {
                            wordRepetitionHandler.setChosenTheme(theme);
                            wordRepetitionHandler.selectBox(theme, editMessageText, this);
                        }
                    }
                }
                case "selected_box" -> {
                    for (Box box : wordRepetitionHandler.getChosenTheme().getBoxes()) {
                        if (box.getId() == Integer.parseInt(splitCallbackData[1])) {
                            wordRepetitionHandler.setChosenBox(box);
                            wordRepetitionHandler.showWord(box, 0,
                                    editMessageText, this);
                        }
                    }
                }
                case "show_word" -> {
                    Box box = wordRepetitionHandler.getChosenBox();
                    Card card = box.getCards().get(Integer.parseInt(splitCallbackData[1]) - 1);
                    if (splitCallbackData[2].equals("correct")) {
                         wordRepetitionHandler.moveCardForward(card);
                    } else {
                        wordRepetitionHandler.moveCardBack(card);
                    }
                    wordRepetitionHandler.showWord(box, Integer.parseInt(splitCallbackData[1]),
                            editMessageText, this);
                }
                case "show_translate" -> {
                    Box box = wordRepetitionHandler.getChosenBox();
                    wordRepetitionHandler.showWordTranslate(box, Integer.parseInt(splitCallbackData[1]),
                            editMessageText, this);
                }
            }
        }
    }
}
