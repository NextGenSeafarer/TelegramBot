package ru.runner.controller;

import com.vdurmont.emoji.EmojiParser;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import ru.runner.repositories.UserData.UserService;
import ru.runner.repositories.UserData.keychain.UserKeyChainGeneration;
import ru.runner.repositories.foodCalculation.FoodCalculationService;

import java.util.ArrayList;
import java.util.List;

import static ru.runner.textConstants.ConstantsForMessages.GENERAL_INFO_ABOUT_BOT_MESSAGE;
import static ru.runner.textConstants.ConstantsForMessages.IDK_THE_COMMAND_MESSAGE;
import static ru.runner.textConstants.Stickers.ConstantsForStickers.WELCOME_CAT_STICKER;

@Component("telegramBotOriginal")
@Log4j
public class TelegramBotOriginal extends TelegramLongPollingBot {
    @Autowired
    private UserService userService;
    @Autowired
    private FoodCalculationService foodCalculationService;
    @Autowired
    private UserKeyChainGeneration userKeyChain;


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
            log.debug(messageText);

            switch (messageText) {
                case "/start":
                    welcomeMessageReply(command);
                    return;
                case "/help":
                    messageExecutor(GENERAL_INFO_ABOUT_BOT_MESSAGE, chatID);
                    break;
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
                    if (!userService.isUserHaveKeyChain(chatID)) {
                        userKeyChain.generateKeyChain(chatID);
                    }
                    log.info("user with chatID :" + chatID + " saved");
                    break;
                case "BOTINFO":
                    messageReplyWithoutKeyboard(chatID, GENERAL_INFO_ABOUT_BOT_MESSAGE);
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
                    } else {
                        userService.userIsNotExistedPleaseRegister(chatID);
                    }
                    break;
                case "GETKEY":
                    if (userService.isUserExists(chatID)) {
                        userKeyChain.generateKeyChain(chatID);
                    } else {
                        userService.userIsNotExistedPleaseRegister(chatID);
                    }
                    break;
                case "RETURNBACK":
                    if (userService.isUserExists(chatID)) {
                        userService.returnBackOriginalAccount(chatID);
                        if (!userService.isAnotherUserNeedToBeUsed(chatID)) {
                            messageReplyWithoutKeyboard(chatID, userService.showUser(chatID).getFirstName() + ", Вы на своём аккаунте");
                        }
                    } else {
                        userService.userIsNotExistedPleaseRegister(chatID);
                    }

                    break;
                default:
                    userService.userIsNotExistedPleaseRegister(chatID);
                    break;
            }
        }
    }


    private void amountEntry(String messageText, Long chatID) {
        long otherChatID;
        if (userService.isAnotherUserNeedToBeUsed(chatID)) {
            otherChatID = userService.getAnotherID(chatID);
        } else {
            otherChatID = chatID;
        }

        if (foodCalculationService.getAllowanceToAddAmount(otherChatID)) {
            if (messageText.matches("\\d+")) {
                if (Integer.parseInt(messageText) > 10 && Integer.parseInt(messageText) < 400) {
                    foodCalculationService.setFoodAmount(Integer.parseInt(messageText), otherChatID);
                    messageExecutor(foodCalculationService.eatTimeCommandReply(otherChatID), chatID);
                } else {
                    messageReplyWithoutKeyboard(chatID, "Неверное количество смеси!");
                }
            }

        }

    }

    private void firstLevelMenuCommands(String messageText, Long chatID) {
        if (messageText.startsWith("!-") && messageText.endsWith("KEY")) {
            if (userKeyChain.isKeyChainExists(messageText)) {
                if (messageText.equals(userService.showUser(chatID).getKeychain())) {
                    if (userService.isAnotherUserNeedToBeUsed(chatID)) {
                        messageExecutor("Это Ваш ключ", chatID);
                        userService.returnBackOriginalAccount(chatID);
                    } else {
                        messageExecutor("Вы все ещё на своём аккаунте", chatID);
                    }
                } else {
                    userKeyChain.changeUser(chatID, messageText);
                    messageExecutor("Вы зашли в аккаунт " + userService.showUser(userService.getAnotherID(chatID)).getFirstName(), chatID);
                }
            } else {
                messageExecutor("Неверный ключ", chatID);
            }
        }

        if (!messageText.matches("\\d+")) {
            long otherChatID;
            if (userService.isAnotherUserNeedToBeUsed(chatID)) {
                otherChatID = userService.getAnotherID(chatID);
            } else {
                otherChatID = chatID;
            }

            switch (messageText) {
                case "Удалить последнее кормление":
                    messageExecutor(foodCalculationService.deleteLastEntry(otherChatID), chatID);
                    break;
                case "Добавить кормление":
                    nutritionTypeKeyboardReply(chatID);
                    foodCalculationService.addNewNutrition(otherChatID);
                    break;
                case "Записи за сегодня":
                    messageExecutor(foodCalculationService.showAllEntriesForSomeDay(otherChatID, "today"), chatID);
                    break;
                case "Записи за вчера":
                    messageExecutor(foodCalculationService.showAllEntriesForSomeDay(otherChatID, "yesterday"), chatID);
                    break;
                case "Записи за неделю":
                    messageExecutor(foodCalculationService.showAllEntriesForCertainDays(otherChatID, 7), chatID);
                    break;
                case "Записи за месяц":
                    messageExecutor(foodCalculationService.showAllEntriesForCertainDays(otherChatID, 30), chatID);
                    break;
                case "сброс":
                case "Сброс":
                case "СБРОС":
                    userService.deleteAllData(chatID);
                    break;
                case "Nan Гипоаллергенный":
                    foodCalculationService.setFoodType("Nan Гипоаллергенный", otherChatID);
                    messageReplyWithoutKeyboard(chatID, "Введите количество смеси:");
                    break;
                case "Nan OptiPro":
                    foodCalculationService.setFoodType("Nan OptiPro", otherChatID);
                    messageReplyWithoutKeyboard(chatID, "Введите количество смеси:");
                    break;
                case "Nan Кисломолочный":
                    foodCalculationService.setFoodType("Nan Кисломолочный", otherChatID);
                    messageReplyWithoutKeyboard(chatID, "Введите количество смеси:");
                    break;
                case "Вода":
                    foodCalculationService.setFoodType("Вода", otherChatID);
                    messageReplyWithoutKeyboard(chatID, "Введите количество");
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
        String whoseAccount;
        if (userService.isUserExists(chatID)) {

            if (userService.isAnotherUserNeedToBeUsed(chatID)) {
                var anotherUser = userService.showUser(chatID).getAnotherUserID();
                whoseAccount = userService.showUser(anotherUser).getFirstName();
            } else {
                whoseAccount = userService.showUser(chatID).getFirstName();
            }
        } else {
            whoseAccount = command.getChat().getFirstName();
        }
        var messageTEXT = "Привет,\n" +
                "Вы используете аккаунт: " + whoseAccount + " " + EmojiParser.parseToUnicode(":arrow_left:");
        message.setChatId(String.valueOf(chatID));
        message.setText(messageTEXT);

        stickerExecutor(WELCOME_CAT_STICKER, chatID);

        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine0 = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine1 = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine2 = new ArrayList<>();


        var register = new InlineKeyboardButton();
        var deleteAllData = new InlineKeyboardButton();
        var botInfo = new InlineKeyboardButton();
        var functions = new InlineKeyboardButton();
        var getKey = new InlineKeyboardButton();





        register.setText("Регистрация");
        deleteAllData.setText("Сброс");
        botInfo.setText("Информация");
        functions.setText("Функции");
        getKey.setText("Получить ключ");



        register.setCallbackData("REGISTRATION");
        deleteAllData.setCallbackData("DELETEALLDATA");
        botInfo.setCallbackData("BOTINFO");
        functions.setCallbackData("FUNCTIONS");
        getKey.setCallbackData("GETKEY");



        rowInLine0.add(botInfo);
        rowInLine0.add(register);
        rowInLine0.add(deleteAllData);
        rowInLine1.add(functions);
        rowInLine2.add(getKey);


        rowsInLine.add(rowInLine0);
        rowsInLine.add(rowInLine1);
        rowsInLine.add(rowInLine2);

        if(userService.isAnotherUserNeedToBeUsed(chatID)){
            var returnBack = new InlineKeyboardButton();
            returnBack.setText("Вернуться на свой аккаунт");
            returnBack.setCallbackData("RETURNBACK");
            List<InlineKeyboardButton> rowInLine3 = new ArrayList<>();
            rowInLine3.add(returnBack);
            rowsInLine.add(rowInLine3);
        }
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
            row1.add("Записи за месяц");
            row1.add("Записи за сегодня");
            keyboardRows.add(row1);

            KeyboardRow row2 = new KeyboardRow();
            row2.add("Записи за неделю");
            row2.add("Записи за вчера");
            keyboardRows.add(row2);


            keyboardMarkup.setKeyboard(keyboardRows);
            message.setReplyMarkup(keyboardMarkup);
        }
    }

    private void messageReplyWithoutKeyboard(Long chatID, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatID);
        message.setText(text);
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
        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        KeyboardRow row3 = new KeyboardRow();
        row0.add("Nan Гипоаллергенный");
        row1.add("Nan OptiPro");
        row2.add("Nan Кисломолочный");
        row3.add("Вода");

        keyboardRows.add(row0);
        keyboardRows.add(row1);
        keyboardRows.add(row2);
        keyboardRows.add(row3);
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
