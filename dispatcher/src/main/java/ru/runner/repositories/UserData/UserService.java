package ru.runner.repositories.UserData;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.runner.controller.TelegramBotOriginal;
import ru.runner.repositories.foodCalculation.FoodCalculationService;

import java.sql.Timestamp;
import java.util.Optional;

import static ru.runner.textConstants.ConstantsForMessages.*;
import static ru.runner.textConstants.Stickers.ConstantsForStickers.SAD_CAT_STICKER;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TelegramBotOriginal telegramBotOriginal;

    @Autowired
    private FoodCalculationService foodService;


    public void saveUser(UserReg user) {
        userRepository.save(user);
    }

    public UserReg showUser(Long chatID) {
        Optional<UserReg> user = userRepository.findById(chatID);
        return user.orElse(null);
    }

    public boolean isUserExists(Long chatID) {
        return userRepository.existsById(chatID);
    }

    public void deleteUser(Long chatID) {
        if (isUserExists(chatID)) {
            userRepository.deleteById(chatID);
        }
    }

    public void userIsNotExistedPleaseRegister(Long chatID) {
        telegramBotOriginal.messageExecutor(PLEASE_REGISTER_MESSAGE, chatID);
    }

    public void deleteMessageReplySuccess(Long chatID) {
        telegramBotOriginal.defaultMessageReplyWithoutKeyboard(chatID, SUCCESS_DELETE_MESSAGE);
        telegramBotOriginal.stickerExecutor(SAD_CAT_STICKER, chatID);
    }

    public void registryUser(Message message) {
        //user registration
        if (!isUserExists(message.getChatId())) {
            var chatID = message.getChatId();
            var chat = message.getChat();
            UserReg user = new UserReg();
            user.setChatId(chatID);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUserName(chat.getUserName());
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));
            saveUser(user);
            regCommandReplyFirstReg(message);
        } else {
            regCommandReplyAlreadyExists(message);
        }
    }

    private void regCommandReplyFirstReg(Message command) {
        telegramBotOriginal.messageExecutor(command.getChat().getFirstName() + YOU_ARE_REGISTERED_MESSAGE, command.getChatId());
    }

    private void regCommandReplyAlreadyExists(Message command) {
        telegramBotOriginal.messageExecutor(command.getChat().getFirstName() + YOU_ARE_REGISTERED_ALREADY_MESSAGE, command.getChatId());
    }

    public void deleteAllData(Long chatID) {
        deleteUser(chatID); // deleting the USER info
        foodService.eraseAllDataForTheUserByChatID(chatID);// deleting the foodCalc table
        deleteMessageReplySuccess(chatID);
    }

    public void askUserOneMoreTomeAboutDelete(Long chatID) {

        if (isUserExists(chatID)) {
            telegramBotOriginal.messageExecutor(ASK_DELETE_MESSAGE, chatID);

        } else {
            userIsNotExistedPleaseRegister(chatID);
        }


    }


}
