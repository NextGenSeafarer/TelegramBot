package ru.runner.repositories.foodCalculation;


import com.vdurmont.emoji.EmojiParser;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.runner.controller.TelegramBotOriginal;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static ru.runner.textConstants.ConstantsForMessages.NOTHING_TO_DELETE_MESSAGE;


@Service
@Setter
@Getter
public class FoodCalculationService {
    @Autowired
    private FoodCalculationRepository repository;
    @Autowired
    private TelegramBotOriginal telegramBotOriginal;
    private FoodCalculationEntity entity;

    private List<FoodCalculationEntity> findAllEntriesByChatID(Long chatID) {

        List<FoodCalculationEntity> resList = new ArrayList<>();
        var list = repository.findAll();
        for (FoodCalculationEntity entity : list) {
            if (entity.getChatID() == chatID) {
                resList.add(entity);
            }
        }
        return resList;
    }
    public FoodCalculationEntity findLastEntryByChatID(Long chatID) {
        LinkedList<FoodCalculationEntity> resList = new LinkedList<>();
        var list = repository.findAll();
        for (FoodCalculationEntity entity : list) {
            if (entity.getChatID() == chatID) {
                resList.add(entity);
            }
        }
        return resList.getLast();
    }
    private boolean isEntityExists(Long chatID) {
        return findAllEntriesByChatID(chatID).size() > 0;
    }
    private String parseLocalDateTime(LocalDateTime localDateTime) {
        int day = localDateTime.toLocalDate().getDayOfMonth();
        String month = localDateTime.toLocalDate().getMonth().toString().substring(0, 3);
        int hours = localDateTime.getHour();
        int minuteInt = localDateTime.getMinute();
        String minute = minuteInt <= 9 ? "0" + minuteInt : String.valueOf(minuteInt);
        return day + " " + month + ", " + hours + ":" + minute;
    }
    private void checkForEmptyFoodAmountEntries(Long chatID) {
        var list = findAllEntriesByChatID(chatID);
        list.stream().filter(x -> x.getFoodAmount() == 0).forEach(x -> repository.deleteById(x.getId()));
    }
    public void eatTimeCommandReply(Long chatID) {
        if (isEntityExists(chatID)) {
            String messageTEXT;
            var lastEntry = findLastEntryByChatID(chatID);
            messageTEXT = "Последняя запись: " +
                    parseLocalDateTime(lastEntry.getFeedTime()) + ", " +
                    lastEntry.getNutritionType() + ", " +
                    lastEntry.getFoodAmount() + " мл";
            telegramBotOriginal.messageExecutor(messageTEXT, chatID);
            checkForEmptyFoodAmountEntries(chatID);
        }
    }
    public void addNewNutrition(Long chatID) {
        entity = new FoodCalculationEntity();
        entity.setChatID(chatID);
        entity.setFeedTime(LocalDateTime.now());
        entity.setAllowedEntryFoodAmount(false);
        repository.save(entity);
    }
    public void setFoodType(String type, Long chatID) {
        if (isEntityExists(chatID)) {
            var lastEntry = findLastEntryByChatID(chatID);
            lastEntry.setNutritionType(type);
            lastEntry.setAllowedEntryFoodAmount(true);
            repository.save(lastEntry);

        }
    }
    public void setFoodAmount(int amount, Long chatID) {
        if (isEntityExists(chatID)) {
            var lastEntry = findLastEntryByChatID(chatID);
            lastEntry.setAllowedEntryFoodAmount(false);
            lastEntry.setFoodAmount(amount);
            repository.save(lastEntry);
        }
    }
    public void deleteLastEntry(Long chatID) {
        if (isEntityExists(chatID)) {
            telegramBotOriginal.messageExecutor("Я удалил:\n" +
                    parseLocalDateTime(findLastEntryByChatID(chatID).getFeedTime()) +
                    EmojiParser.parseToUnicode(" :o:"), chatID);
            repository.deleteById(findLastEntryByChatID(chatID).getId());
        } else {
            telegramBotOriginal.messageExecutor(NOTHING_TO_DELETE_MESSAGE, chatID);
        }
    }
    public String showAllEntriesForToday(Long chatID) {
        LocalDateTime startDate = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT);
        var dates = findAllEntriesByChatID(chatID);
        if (dates.isEmpty()) {
            return "У Вас нет записей за сегодня" + EmojiParser.parseToUnicode(":crying_cat_face:");
        } else {
            StringBuilder sb = new StringBuilder();
            AtomicInteger totalAmount = new AtomicInteger();
            AtomicInteger totalTimes = new AtomicInteger();
            dates.stream()
                    .filter(x -> x.getFeedTime().isAfter(startDate))
                    .forEach(x -> {
                        totalAmount.addAndGet(x.getFoodAmount());
                        totalTimes.incrementAndGet();
                        sb
                                .append(parseLocalDateTime(x.getFeedTime())).append(", ").append(x.getFoodAmount()).append(" мл, ")
                                .append(x.getNutritionType())
                                .append("\n");
                    });
            sb.append("В общем за сегодня: ").append(totalAmount).append(" мл, ").append(totalTimes).append(" раз").append(EmojiParser.parseToUnicode(" :white_check_mark:"));
            return sb.toString().strip();
        }
    }
    public String showAllEntriesForYesterday(Long chatID) {
        LocalDateTime startDate = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.MIDNIGHT);
        LocalDateTime finishDate = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT);
        var dates = findAllEntriesByChatID(chatID);
        if (dates.isEmpty()) {
            return "У Вас нет записей за вчера" + EmojiParser.parseToUnicode(":crying_cat_face:");
        } else {
            StringBuilder sb = new StringBuilder();
            AtomicInteger totalAmount = new AtomicInteger();
            AtomicInteger totalTimes = new AtomicInteger();
            dates.stream()
                    .filter(x -> x.getFeedTime().isAfter(startDate) && x.getFeedTime().isBefore(finishDate))
                    .forEach(x -> {
                        totalAmount.addAndGet(x.getFoodAmount());
                        totalTimes.incrementAndGet();
                        sb
                                .append(parseLocalDateTime(x.getFeedTime())).append(", ").append(x.getFoodAmount()).append(" мл, ")
                                .append(x.getNutritionType())
                                .append("\n");
                    });
            if (totalTimes.get() == 0 || totalAmount.get() == 0) {
                return "У Вас нет записей за вчера" + EmojiParser.parseToUnicode(":crying_cat_face:");
            }
            sb.append("В общем за вчера: ").append(totalAmount).append(" мл, ").append(totalTimes).append(" раз").append(EmojiParser.parseToUnicode(" :white_check_mark:"));
            return sb.toString().strip();
        }
    }
    public void eraseAllDataForTheUserByChatID(Long chatID) {
        var getIDsToDelete = findAllEntriesByChatID(chatID);
        for (FoodCalculationEntity entity : getIDsToDelete) {
            repository.deleteById(entity.getId());
        }
    }
    public boolean getAllowanceToAddAmount(Long chatID) {
        if (isEntityExists(chatID)) {
            var lastEntry = findLastEntryByChatID(chatID);
            return lastEntry.isAllowedEntryFoodAmount();
        } else return false;
    }
}
