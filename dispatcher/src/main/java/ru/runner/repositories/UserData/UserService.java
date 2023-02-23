package ru.runner.repositories.UserData;

import lombok.Getter;
import lombok.Setter;
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
@Getter
@Setter
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

    public boolean isUserHaveKeyChain(Long chatID) {
        return userRepository.findById(chatID).get().getKeychain() != null;

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
        telegramBotOriginal.defaultMessageReplyWithoutKeyboard(command.getChatId(), command.getChat().getFirstName() + YOU_ARE_REGISTERED_ALREADY_MESSAGE);
    }

    public void deleteAllData(Long chatID) {
        deleteUser(chatID); // deleting the USER info
        foodService.eraseAllDataForTheUserByChatID(chatID);// deleting the foodCalc table
        deleteMessageReplySuccess(chatID);
    }

    public void askUserOneMoreTomeAboutDelete(Long chatID) {

        if (isUserExists(chatID)) {
            telegramBotOriginal.defaultMessageReplyWithoutKeyboard(chatID, ASK_DELETE_MESSAGE);
        } else {
            userIsNotExistedPleaseRegister(chatID);
        }
    }

    public boolean isAnotherUserNeedToBeUsed(Long chatID) {
        if (isUserExists(chatID)) {
           return userRepository.findById(chatID).get().isAnotherUserNeedToBeUsed();
        }
        return false;
    }

    public Long getAnotherID(Long chatID) {
        return userRepository.findById(chatID).get().getAnotherUserID();
    }

    public void returnBackOriginalAccount(Long chatID) {
        var user = userRepository.findById(chatID).get();
        user.setAnotherUserNeedToBeUsed(false);
        user.setAnotherUserID(null);
        userRepository.save(user);
    }


}
