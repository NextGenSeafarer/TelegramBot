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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static ru.runner.textConstants.ConstantsForMessages.NOTHING_TO_DELETE_MESSAGE;
import static ru.runner.textConstants.ConstantsForMessages.YOU_DO_NOT_HAVE_ENTRIES_MESSAGE;


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
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMMM, HH:mm");
        return localDateTime.format(dtf);
    }
    private void checkForEmptyFoodAmountEntries(Long chatID) {
        var list = findAllEntriesByChatID(chatID);
        list.stream().filter(x -> x.getFoodAmount() == 0).forEach(x -> repository.deleteById(x.getId()));
    }
    public String eatTimeCommandReply(Long chatID) {

        String messageTEXT;
        var lastEntry = findLastEntryByChatID(chatID);
        messageTEXT = "Последняя запись: " +
                parseLocalDateTime(lastEntry.getFeedTime()) + ", " +
                lastEntry.getNutritionType() + ", " +
                lastEntry.getFoodAmount() + " мл";
        checkForEmptyFoodAmountEntries(chatID);
        return messageTEXT;

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
    public String deleteLastEntry(Long chatID) {
        if (isEntityExists(chatID)) {
            String res = "Я удалил:\n" + parseLocalDateTime(findLastEntryByChatID(chatID).getFeedTime()) + EmojiParser.parseToUnicode(" :o:");
            repository.deleteById(findLastEntryByChatID(chatID).getId());
            return res;
        } else {
            return NOTHING_TO_DELETE_MESSAGE;
        }
    }
    public String showAllEntriesForSomeDay(Long chatID, String yesterdayORToday) {
        LocalDateTime startDate;
        LocalDateTime finishDate;

        if (yesterdayORToday.equals("today")) {
            startDate = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT);
            finishDate = LocalDateTime.now().plusDays(1);
        } else {
            startDate = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.MIDNIGHT);
            finishDate = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT);
        }
        var dates = findAllEntriesByChatID(chatID);
        if (dates.isEmpty()) {
            return YOU_DO_NOT_HAVE_ENTRIES_MESSAGE;
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
            String times = totalTimes.toString().matches("\\d*[234]") ? "раза" : "раз";
            String YOT = yesterdayORToday.equals("today") ? " сегодня " : " вчера ";

            if (totalTimes.get() == 0 || totalAmount.get() == 0) {
                return "У Вас нет записей за" + YOT + EmojiParser.parseToUnicode(":crying_cat_face:");
            }
            sb.append("В общем за").append(YOT).append(totalAmount).append(" мл, ").append(totalTimes).append(" ").append(times).append(EmojiParser.parseToUnicode(" :white_check_mark:"));
            return sb.toString().strip();
        }
    }
    public String showAllEntriesForCertainDays(Long chatID, int daysToShow) {

        LocalDateTime startDate = LocalDateTime.of(LocalDate.now().minusDays(daysToShow), LocalTime.MIDNIGHT);
        LocalDateTime finishDate = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT);
        var dates = findAllEntriesByChatID(chatID);
        if (dates.isEmpty()) {
            return YOU_DO_NOT_HAVE_ENTRIES_MESSAGE;
        } else {
            StringBuilder sb = new StringBuilder();

            List<FoodCalculationEntity> weekList =
                    dates.stream()
                            .filter(x -> x.getFeedTime().isAfter(startDate) && x.getFeedTime().isBefore(finishDate))
                            .collect(Collectors.toList());
            if (weekList.isEmpty()) {
                return YOU_DO_NOT_HAVE_ENTRIES_MESSAGE;
            }

            List<LocalDate> lastWeekDates = weekList.stream().map(x -> x.getFeedTime().toLocalDate()).distinct().collect(Collectors.toList());

            String weekOrMonth = daysToShow == 7 ? "Записи за последнюю неделю:\n" : "Записи за последний месяц:\n";
            sb.append(weekOrMonth);

            //TODO: реализовать поиск по введенному количеству дней

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMMM yyyy");

            for (LocalDate lwd : lastWeekDates) {
                int amount = 0;
                int water = 0;
                int timesInt = 0;

                for (FoodCalculationEntity foodCalculationEntity : weekList) {
                    if (foodCalculationEntity.getFeedTime().toLocalDate().equals(lwd)) {
                        if (foodCalculationEntity.getNutritionType().equals("Вода")) {
                            water += foodCalculationEntity.getFoodAmount();
                            timesInt++;
                            continue;
                        }
                        amount += foodCalculationEntity.getFoodAmount();
                        timesInt++;
                    }
                }
                String times = String.valueOf(timesInt).matches("\\d*[234]") ? "раза" : "раз";


                sb.append(lwd.format(dtf)).append(" : ").append(amount).append(" мл смеси, ").append(water).append(" воды, ").append(timesInt).append(" ").append(times).append("\n");
            }

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
