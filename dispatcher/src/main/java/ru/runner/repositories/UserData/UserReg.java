package ru.runner.repositories.UserData;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table
@Getter
@Setter
@ToString
@NoArgsConstructor
public class UserReg {

    @Id
    private Long chatId;

    private String firstName;

    private String lastName;

    private String userName;

    private Timestamp registeredAt;

    private String keychain;

    private Long anotherUserID;

    private boolean isAnotherUserNeedToBeUsed;


}
