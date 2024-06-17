package com.solbeg;

import com.solbeg.model.User;
import com.solbeg.model.UserBankAccount;
import com.solbeg.service.UserBankAccountProvider;
import com.solbeg.service.UserProvider;
import com.solbeg.service.UserService;
import com.solbeg.service.Users;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    private static User user = new User(6L, "Peter", "ap@gmail.com", new BigDecimal(35));
    private static User temp;
    private static UserBankAccount userBankAccount = new UserBankAccount(user, BigDecimal.valueOf(20));
    private static UserBankAccount tempBankAccount;
    private static final UserProvider userProvider = () -> Optional.of(user);
    private static final UserBankAccountProvider userBankAccountProvider = () -> Optional.of(userBankAccount);

    private static final List<UserBankAccount> bankAccounts = new ArrayList<>();

    private Map<String, Optional<User>> getOptionalUserMap() {
        Map<String, Optional<User>> userMap = new HashMap<>();
        userMap.put("user 1", Optional.of(
                new User(2L, "Jorge", "luc@gmail.com", new BigDecimal(20))));
        userMap.put("user 2", Optional.of(
                new User(3L, "Elisabeth", "el@mail.en", new BigDecimal(25))));
        userMap.put("user 3", Optional.of(
                new User(4L, "Jack", "ok@yahoo.com", new BigDecimal(60))));
        userMap.put("user 4", Optional.of(
                new User(5L, "Marry", "hem@email.com", new BigDecimal(40))));
        userMap.put("user with empty fields", (Optional.of(
                new User(7L, null, null, null))));
        userMap.put("null", Optional.empty());
        return userMap;
    }

    private Map<String, User> getUserMap() {
        Map<String, User> userMap = new HashMap<>();
        userMap.put("user 1",
                new User(2L, "Jorge", "luc@gmail.com", new BigDecimal(20)));
        userMap.put("user 2",
                new User(3L, "Elisabeth", "el@mail.en", new BigDecimal(25)));
        userMap.put("user with empty fields",
                new User(7L, null, null, null));
        userMap.put("null", null);
        return userMap;
    }

    private static List<User> getUserList() {
        List<User> userList = new ArrayList<>();
        userList.add(
                new User(2L, "Jorge", "luc@gmail.com", new BigDecimal(20)));
        userList.add(
                new User(3L, "Elisabeth", "el@mail.en", new BigDecimal(25)));
        userList.add(
                new User(4L, "Jack", "ok@yahoo.com", new BigDecimal(60)));
        userList.add(
                new User(5L, "Marry", "hem@email.com", new BigDecimal(40)));
        userList.add(
                new User(6L, "Peter", "ap@gmail.com", new BigDecimal(35)));
        return userList;
    }

    @BeforeAll
    static void initBankAccounts() {
        int i = 0;
        for (User user :
                getUserList()) {
            bankAccounts.add(new UserBankAccount(user, new BigDecimal(10 * i++)));
        }
    }

    @BeforeEach
    void saveDefaultValue() {
        temp = User.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .balance(user.getBalance())
                .build();
        tempBankAccount = new UserBankAccount(temp, userBankAccount.getCreditBalance().get());
    }

    @AfterEach
    void returnDefaultValue() {
        user = User.builder()
                .id(temp.getId())
                .email(temp.getEmail())
                .name(temp.getName())
                .balance(temp.getBalance())
                .build();
        userBankAccount = new UserBankAccount(user, tempBankAccount.getCreditBalance().get());
    }

    @Test
    void optionalOfNullString() {
        assertEquals(Main.optionalOfString(null), Optional.empty());
    }

    @Test
    void optionalOfString() {
        String normalString = "java";
        assertEquals(Main.optionalOfString(normalString), Optional.of(normalString));
    }

    @Test
    void deposit() {
        Main.deposit(userProvider, new BigDecimal(30));
        assertEquals(userProvider.getUser().get().getBalance(), BigDecimal.valueOf(65));
    }

    @Test
    void depositWithEmptyUser() {
        Optional<User> user = getOptionalUserMap().get("null");
        UserProvider userProvider = () -> user;
        assertThrows(RuntimeException.class, () -> Main.deposit(userProvider, BigDecimal.valueOf(30)));
    }

    @Test
    void depositWithNullBalance() {
        Optional<User> user = getOptionalUserMap().get("user with empty fields");
        UserProvider userProvider = () -> user;
        assertThrows(NullPointerException.class, () -> Main.deposit(userProvider, BigDecimal.valueOf(30)));
    }

    @Test
    void optionalOfUser() {
        User user = Users.generateUser();
        assertEquals(Optional.of(user), Main.optionalOfUser(user));
    }

    @Test
    void optionalOfNullUser() {
        assertEquals(Optional.empty(), Main.optionalOfUser(null));
    }

    @Test
    void getUser() {
        User defaultUser = Users.generateUser();
        assertEquals(Main.getUser(userProvider, defaultUser), user);
    }

    @Test
    void getDefaultUser() {
        UserProvider userProvider = Optional::empty;
        User defaultUser = Users.generateUser();
        assertEquals(Main.getUser(userProvider, defaultUser), defaultUser);
    }

    @Test
    void processUser() {
        UserService userService = (user) -> user.setBalance(user.getBalance().add(BigDecimal.TEN));
        Main.processUser(userProvider, userService);
        assertEquals(userProvider.getUser().get().getBalance(), BigDecimal.valueOf(45));
    }

    @Test
    void processWithNoUser() {
        UserService userService = (user) -> user.setBalance(user.getBalance().add(BigDecimal.TEN));
        Optional<User> optionalUser = getOptionalUserMap().get("null");
        UserProvider userProvider = () -> optionalUser;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(baos);
        System.setOut(printStream);

        Main.processUser(userProvider, userService);
        String expendedOut = baos.toString();
        assertEquals(expendedOut, "No user found\r\n");
    }

    @Test
    void getOrGenerateUserGET() {
        assertEquals(Main.getOrGenerateUser(userProvider), user);
    }

    @Test
    void getOrGenerateUserGENERATE() {
        UserProvider userProvider = () -> getOptionalUserMap().get("null");
        assertEquals(Main.getOrGenerateUser(userProvider), Users.generateUser());
    }

    @Test
    void retrieveBalance() {
        assertEquals(Main.retrieveBalance(userProvider), Optional.of(userProvider.getUser().get().getBalance()));
    }

    @Test
    void retrieveBalanceFromEmptyUser() {
        UserProvider userProvider = Optional::empty;
        assertEquals(Main.retrieveBalance(userProvider), Optional.empty());
    }

    @Test
    void retrieveEmptyBalance() {
        UserProvider userProvider = () -> getOptionalUserMap().get("user with empty fields");
        assertEquals(Main.retrieveBalance(userProvider), Optional.empty());
    }

    @Test
    void testGetUser() {
        assertEquals(Main.getUser(userProvider), user);
    }

    @Test
    void testGetUserException() {
        UserProvider userProvider = Optional::empty;
        assertThrows(RuntimeException.class, () -> Main.getUser(userProvider));
    }

    @Test
    void retrieveCreditBalance() {
        assertEquals(Main.retrieveCreditBalance(userBankAccountProvider), userBankAccount.getCreditBalance());
    }

    @Test
    void retrieveCreditBalanceFromEmptyAccount() {
        UserBankAccountProvider userBankAccountProvider = Optional::empty;
        assertEquals(Main.retrieveCreditBalance(userBankAccountProvider), Optional.empty());
    }

    @Test
    void retrieveEmptyCreditBalance() {
        UserBankAccountProvider userBankAccountProvider = () -> Optional.of(new UserBankAccount(user, null));
        assertEquals(Main.retrieveCreditBalance(userBankAccountProvider), Optional.empty());
    }

    @Test
    void retrieveUserGmail() {
        assertEquals(Main.retrieveUserGmail(userProvider), Optional.of(user));
    }

    @Test
    void failRetrieveUserGmail() {
        UserProvider userProvider = () -> Optional.of(getUserMap().get("user 2"));
        assertEquals(Main.retrieveUserGmail(userProvider), Optional.empty());
    }

    @Test
    void getUserWithFallbackMAIN() {
        UserProvider fallbackProvider = () -> Optional.of(getUserMap().get("user 1"));
        assertEquals(Main.getUserWithFallback(userProvider, fallbackProvider), user);
    }

    @Test
    void getUserWithFallbackFALLBACK() {
        UserProvider fallbackProvider = () -> Optional.of(getUserMap().get("user 1"));
        UserProvider userProvider = Optional::empty;
        assertEquals(Main.getUserWithFallback(userProvider, fallbackProvider), getUserMap().get("user 1"));
    }

    @Test
    void getUserWithFallbackEXCEPTION() {
        UserProvider fallbackProvider = Optional::empty;
        UserProvider userProvider = Optional::empty;
        assertThrows(NoSuchElementException.class,
                () -> Main.getUserWithFallback(userProvider, fallbackProvider));
    }

    @Test
    void getUserWithMaxBalance() {
        assertEquals(Main.getUserWithMaxBalance(getUserList()), getUserList().get(2));
    }

    @Test
    void getUserWithMaxBalanceEmptyList() {
        assertThrows(NoSuchElementException.class, () -> Main.getUserWithMaxBalance(null));
    }

    @Test
    void findMinBalanceValue() {
        assertEquals(Main.findMinBalanceValue(getUserList()), OptionalDouble.of(20.));
    }

    @Test
    void findMinBalanceValueEmptyList() {
        assertEquals(Main.findMinBalanceValue(null), OptionalDouble.empty());
    }

    @Test
    void calculateTotalCreditBalance() {
        assertEquals(100, Main.calculateTotalCreditBalance(bankAccounts));
    }
}
