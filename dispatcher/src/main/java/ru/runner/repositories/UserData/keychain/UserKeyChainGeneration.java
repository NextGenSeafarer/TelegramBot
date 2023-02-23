package ru.runner.repositories.UserData.keychain;


import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runner.controller.TelegramBotOriginal;
import ru.runner.repositories.UserData.UserReg;
import ru.runner.repositories.UserData.UserRepository;
import ru.runner.repositories.UserData.UserService;
import ru.runner.repositories.foodCalculation.FoodCalculationEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@NoArgsConstructor
@Component
public class UserKeyChainGeneration {
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    TelegramBotOriginal telegramBotOriginal;

    public void generateKeyChain(Long chatID) {
        var user = userRepository.findById(chatID).get();
        if (userService.isUserHaveKeyChain(chatID)) {
            telegramBotOriginal.defaultMessageReplyWithoutKeyboard(chatID, user.getKeychain());
        } else {
            user.setKeychain(generatingKey(user));
            userRepository.save(user);
            telegramBotOriginal.defaultMessageReplyWithoutKeyboard(chatID, "Вам присвоен ключ:\n" + user.getKeychain()
                    + "\nБолее подробная информация, зачем он нужен: /help");
        }
    }

    public boolean isKeyChainExists(String keychain) {
        return findUserByKeychainAndReturnChatID(keychain) != null;
    }

    private String generatingKey(UserReg user) {
        List<String> list = new ArrayList<>();

        String id = String.valueOf(user.getChatId());
        String randomWord = "ABCDEFGZXCVmnbvoiup";

        for (int i = 0; i < randomWord.length(); i++) {
            list.add(randomWord.substring(i, i + 1));
        }
        for (int i = 0; i < id.length(); i++) {
            list.add(id.substring(i, i + 1));
        }
        Collections.shuffle(list);
        list.add(0, "!-");
        list.add(list.size(), "KEY");
        StringBuilder sb = new StringBuilder();
        list.forEach(sb::append);
        System.out.println(sb);
        return sb.toString();

    }

    public void changeUser(Long chatID, String keychain) {

        UserReg user;
        if (userService.isUserExists(chatID)) {
            user = userRepository.findById(chatID).get();
            user.setAnotherUserNeedToBeUsed(true);
            user.setAnotherUserID(findUserByKeychainAndReturnChatID(keychain));
            userRepository.save(user);
        }
    }

    private Long findUserByKeychainAndReturnChatID(String keychain) {
        var list = userRepository.findAll();
        for (UserReg entity : list) {
            if (entity.getKeychain().equals(keychain)) {
                return entity.getChatId();
            }
        }
        return null;
    }
}
