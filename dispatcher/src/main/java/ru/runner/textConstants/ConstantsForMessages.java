package ru.runner.textConstants;

import com.vdurmont.emoji.EmojiParser;

public class ConstantsForMessages {


    public static final String WELCOME_MESSAGE = " , меня зовут HomeHelperBot" +
            EmojiParser.parseToUnicode(":tada:");

    public static final String SUCCESS_DELETE_MESSAGE = "Я удалил всю информацию о Вас, возвращайтесь скорее! " +
            EmojiParser.parseToUnicode(":pray:");

    public static final String ASK_DELETE_MESSAGE = "Введите: \"сброс\", чтобы выполнить команду, в таком случае " +
            "ВСЯ ВАША ИНФОРМАЦИЯ будет стёрта без возможности восстановления! " +
            EmojiParser.parseToUnicode(":no_entry:");
    public static final String IDK_THE_COMMAND_MESSAGE = "/start, чтобы вернуться к меню";

    public static final String YOU_ARE_REGISTERED_ALREADY_MESSAGE = ", Вы уже зарегистрированы! " +
            EmojiParser.parseToUnicode(":smiley_cat:");

    public static final String YOU_ARE_REGISTERED_MESSAGE = ", Вы успешно зарегистрированы! " +
            EmojiParser.parseToUnicode(":sparkles:");

    public static final String PLEASE_REGISTER_MESSAGE = "Чтобы использовать весь функционал - зарегистрируйтесь";
    public static final String NOTHING_TO_DELETE_MESSAGE = "Удалять нечего" + EmojiParser.parseToUnicode(":hear_no_evil:");
    public static final String GENERAL_INFO_ABOUT_BOT_MESSAGE =
            "- При нажатии кнопки \"регистрация\" я начинаю собирать следующую информацию:\n" +
                    "Имя, фамилия, никнейм (если указано в профиле), дата первого использования\n" +
                    "А также я сохраняю все сообщения, отправленные мне Вами\n\n" +
                    "- При вводе команды \"сброс\" я удалю всю информацию о Вас, без возможности восстановления\n" +
                    "- Кнопка \"Записать кормление\" и дальнейшая информация будет сохранена в таблицу\n" +
                    "- Кормления считаются с 00:00 до 00:00 следующего дня\n\n" +
                    "- Если Вы хотите использовать аккаунт вдвоем с разных аккаунтов telegram, тогда:\n" +
                    "-> Нажмите получить ключ\n" +
                    "-> Отправьте этот ключ тому, с кем собираетесь использовать бота\n" +
                    "-> Человек, который получит этот ключ, должен отправить его боту со своего аккаунта\n" +
                    "-> Готово!\n\n" +
                    "- Чтобы вернуться на свой аккаунт, нажмите: /start -> Вернуться на свой аккаунт";


    public static final String YOU_DO_NOT_HAVE_ENTRIES_MESSAGE = "У Вас ещё нет записей " + EmojiParser.parseToUnicode(":crying_cat_face:");


}
