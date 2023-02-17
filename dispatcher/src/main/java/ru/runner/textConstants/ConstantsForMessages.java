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
    public static final String IDK_THE_COMMAND_MESSAGE = "Не знаю такой команды, введите /start, чтобы вернуться к меню";

    public static final String YOU_ARE_REGISTERED_ALREADY_MESSAGE = ", Вы уже зарегистрированы! " +
            EmojiParser.parseToUnicode(":smiley_cat:");

    public static final String YOU_ARE_REGISTERED_MESSAGE = ", Вы успешно зарегистрированы! " +
            EmojiParser.parseToUnicode(":sparkles:");

    public static final String PLEASE_REGISTER_MESSAGE = "Чтобы использовать весь функционал - зарегистрируйтесь";
    public static final String NOTHING_TO_DELETE_MESSAGE = "Удалять нечего" + EmojiParser.parseToUnicode(":hear_no_evil:");
    public static final String GENERAL_INFO_ABOUT_BOT_MESSAGE = "Я - бот, приятно познакомиться, вот пара правил для ознакомления:\n" +
            "При нажатии кнопки \"регистрация\":\n" +
            "Я начинаю собирать следующую информацию:\n" +
            "Имя, фамилия, никнейм (если указано в профиле), дата первого использования\n" +
            "А также я сохраняю все сообщения, отправленные мне Вами\n" +
            "При нажатии кнопки \"сброс\" я удалю всю информацию о Вас, без возможности восстановления\n" +
            "Кнопка \"Записать кормление\" и дальнейшая информация будет сохранена в таблицу, её можно посмотреть\n" +
            "Все записи, где Вы не указали количество смеси или другую информацию по каким - либо причинам,\n" +
            "будут автоматически удалены\n" +
            "Каждый день в 00:00 происходит автоматический сброс количества кормлений\n" +
            "Для уточнения информации/сотрудничества/предложений пишите: pocha000@mail.ru (Илья)\n" +
            "Приятного использования!";
    ;


}
