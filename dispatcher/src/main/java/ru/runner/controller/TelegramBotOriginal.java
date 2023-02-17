package ru.runner.controller;

import com.vdurmont.emoji.EmojiParser;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.runner.repositories.UserData.UserRepository;
import ru.runner.repositories.UserData.UserService;
import ru.runner.repositories.foodCalculation.FoodCalculationService;

import java.util.ArrayList;
import java.util.List;

import static ru.runner.textConstants.ConstantsForMessages.*;
import static ru.runner.textConstants.Stickers.ConstantsForStickers.WELCOME_CAT_STICKER;

@Component("telegramBotOriginal")
@EnableScheduling
@Log4j
public class TelegramBotOriginal extends TelegramLongPollingBot {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private FoodCalculationService foodCalculationService;


    @Value("${bot.name}")
    private String bot_name;
    @Value("${bot.token}")
    private String bot_token;

    @Override
    public String getBotUsername() {
        return bot_name;
    }

    @Override
    public String getBotToken() {
        return bot_token;
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message command;

        if (update.getMessage() != null && update.hasMessage() && update.getMessage().hasText()) {

            String messageText = update.getMessage().getText();
            command = update.getMessage();
            long chatID = update.getMessage().getChatId();

            log.debug(messageText); // <-logging the messages

            if (messageText.equals("/start")) {
                welcomeMessageReply(command);
                return;
            }
            if (userService.isUserExists(chatID)) {

                firstLevelMenuCommands(messageText, chatID);

                amountEntry(messageText, chatID);
            } else {
                userService.userIsNotExistedPleaseRegister(chatID);
            }
        } else if (update.hasCallbackQuery()) {

            var chatID = update.getCallbackQuery().getMessage().getChatId();
            command = update.getCallbackQuery().getMessage();
            var callBackData = update.getCallbackQuery().getData();

            switch (callBackData) {
                case "REGISTRATION":
                    userService.registryUser(command);
                    log.info("user with chatID :" + chatID + " saved");
                    break;
                case "BOTINFO":
                    messageExecutor(GENERAL_INFO_ABOUT_BOT_MESSAGE, chatID);
                    break;
                case "DELETEALLDATA":
                    if (userService.isUserExists(chatID)) {
                        userService.askUserOneMoreTomeAboutDelete(chatID);
                    } else {
                        userService.userIsNotExistedPleaseRegister(chatID);
                    }
                    break;
                case "FUNCTIONS":
                    if (userService.isUserExists(chatID)) {
                        messageExecutor(EmojiParser.parseToUnicode(":mouse2:"), chatID);
                        break;
                    }
                default:
                    userService.userIsNotExistedPleaseRegister(chatID);
                    break;
            }
        }
    }


    private void amountEntry(String messageText, Long chatID) {
        if (foodCalculationService.getAllowanceToAddAmount(chatID)) {
            if (messageText.matches("\\d+")) {
                foodCalculationService.setFoodAmount(Integer.parseInt(messageText), chatID);
                foodCalculationService.eatTimeCommandReply(chatID);
            }

        }

    }


    private void firstLevelMenuCommands(String messageText, Long chatID) {
        if (!messageText.matches("\\d+")) {
            switch (messageText) {
                case "Удалить последнее кормление":
                    foodCalculationService.deleteLastEntry(chatID);
                    break;
                case "Добавить кормление":
                    nutritionTypeKeyboardReply(chatID);
                    foodCalculationService.addNewNutrition(chatID);
                    break;
                case "Записи за сегодня":
                    messageExecutor(foodCalculationService.showAllEntriesForToday(chatID), chatID);
                    break;
                case "Записи за вчера":
                    messageExecutor(foodCalculationService.showAllEntriesForYesterday(chatID), chatID);
                    break;
                case "сброс":
                case "Сброс":
                case "СБРОС":
                    userService.deleteAllData(chatID);
                    break;
                case "Nan Гипоаллергенный":
                    foodCalculationService.setFoodType("Nan Гипоаллергенный", chatID);
                    enterAmountTypeKeyboardReply(chatID);
                    break;
                case "Nan OptiPro":
                    foodCalculationService.setFoodType("Nan OptiPro", chatID);
                    enterAmountTypeKeyboardReply(chatID);
                    break;
                case "Nan Кисломолочный":
                    foodCalculationService.setFoodType("Nan Кисломолочный", chatID);
                    enterAmountTypeKeyboardReply(chatID);
                    break;
                default:
                    defaultMessageReplyWithoutKeyboard(chatID, IDK_THE_COMMAND_MESSAGE);

            }
        }

    }

    private ReplyKeyboardRemove deleteKeyBoard() {
        ReplyKeyboardRemove replyKeyboardRemove = new ReplyKeyboardRemove();
        replyKeyboardRemove.setRemoveKeyboard(true);
        return replyKeyboardRemove;
    }

    private void welcomeMessageReply(Message command) {
        var chatID = command.getChatId();

        SendMessage message = new SendMessage();
        var messageTEXT = "Привет, " + command.getChat().getFirstName() + WELCOME_MESSAGE;
        message.setChatId(String.valueOf(chatID));
        message.setText(messageTEXT);

        stickerExecutor(WELCOME_CAT_STICKER, chatID);

        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine0 = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine1 = new ArrayList<>();

        var register = new InlineKeyboardButton();
        var deleteAllData = new InlineKeyboardButton();
        var botInfo = new InlineKeyboardButton();
        var functions = new InlineKeyboardButton();


        register.setText("Регистрация");
        deleteAllData.setText("Сброс");
        botInfo.setText("Информация");
        functions.setText("Функции");


        register.setCallbackData("REGISTRATION");
        deleteAllData.setCallbackData("DELETEALLDATA");
        botInfo.setCallbackData("BOTINFO");
        functions.setCallbackData("FUNCTIONS");


        rowInLine0.add(botInfo);
        rowInLine0.add(register);
        rowInLine0.add(deleteAllData);
        rowInLine1.add(functions);

        rowsInLine.add(rowInLine0);
        rowsInLine.add(rowInLine1);


        markupInLine.setKeyboard(rowsInLine);
        message.setReplyMarkup(markupInLine);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }

    }

    public void defaultMessageReplyWithoutKeyboard(Long chatID, String messageText) {
        SendMessage message = new SendMessage();
        message.setChatId(chatID);
        message.setText(messageText);
        defaultKeyboardReply(chatID, message);
        message.setReplyMarkup(deleteKeyBoard());
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    private void defaultKeyboardReply(Long chatID, SendMessage message) {
        if (userService.isUserExists(chatID)) {
            ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
            List<KeyboardRow> keyboardRows = new ArrayList<>();
            KeyboardRow row0 = new KeyboardRow();
            row0.add("Удалить последнее кормление");
            row0.add("Добавить кормление");
            keyboardRows.add(row0);

            KeyboardRow row1 = new KeyboardRow();
            row1.add("Записи за вчера");
            row1.add("Записи за сегодня");
            keyboardRows.add(row1);
            keyboardMarkup.setKeyboard(keyboardRows);
            message.setReplyMarkup(keyboardMarkup);
        }
    }

    private void enterAmountTypeKeyboardReply(Long chatID) {
        SendMessage message = new SendMessage();
        message.setChatId(chatID);
        message.setText("Введите количество смеси:");
        message.setReplyMarkup(deleteKeyBoard());
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(message);
        }

    }

    private void nutritionTypeKeyboardReply(Long chatID) {
        SendMessage message = new SendMessage();
        message.setChatId(chatID);
        message.setText("Какая смесь?");
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row0 = new KeyboardRow();
        row0.add("Nan Гипоаллергенный");
        row0.add("Nan OptiPro");
        row0.add("Nan Кисломолочный");
        keyboardRows.add(row0);
        keyboardMarkup.setKeyboard(keyboardRows);
        message.setReplyMarkup(keyboardMarkup);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void messageExecutor(String messageText, Long chatID) {
        SendMessage message = new SendMessage();
        message.setChatId(chatID);
        message.setText(messageText);
        defaultKeyboardReply(chatID, message);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }

    }

    public void stickerExecutor(String stickerID, Long chatID) {
        SendSticker sticker = new SendSticker();
        sticker.setChatId(String.valueOf(chatID));
        InputFile stick = new InputFile();
        stick.setMedia(stickerID);
        sticker.setSticker(stick);
        try {
            execute(sticker);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }
    


}
