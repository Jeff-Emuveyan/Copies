package Classes;

import com.orm.SugarRecord;

/**
 * Created by JEFF EMUVEYAN on 2/4/2018.
 */

public class User extends SugarRecord<User> {


    String userId;
    String userName,userPassword, userEmail, userGender, signUpPeriod, profilePictureLink, totalNumberOfCopies;
    Boolean isActivated, isLoggedIn;


    public User() {//Neccessary empty constructor so that Sugar database can work!
    }


    public User(String userId, String userName, String userPassword, String userEmail, String userGender, String signUpPeriod, String profilePictureLink, String totalNumberOfCopies, Boolean isActivated, Boolean isLoggedIn) {
        this.userId = userId;
        this.userName = userName;
        this.userPassword = userPassword;
        this.userEmail = userEmail;
        this.userGender = userGender;
        this.signUpPeriod = signUpPeriod;
        this.profilePictureLink = profilePictureLink;
        this.totalNumberOfCopies = totalNumberOfCopies;
        this.isActivated = isActivated;
        this.isLoggedIn = isLoggedIn;
    }

    public User(String userName, String userPassword, String userEmail, String userGender, String signUpPeriod, String profilePictureLink, String totalNumberOfCopies, Boolean isActivated, Boolean isLoggedIn) {
        this.userName = userName;
        this.userPassword = userPassword;
        this.userEmail = userEmail;
        this.userGender = userGender;
        this.signUpPeriod = signUpPeriod;
        this.profilePictureLink = profilePictureLink;
        this.totalNumberOfCopies = totalNumberOfCopies;
        this.isActivated = isActivated;
        this.isLoggedIn = isLoggedIn;
    }

    public User(String userName, String userGender, String totalNumberOfCopies, String profilePictureLink) {
        this.userName = userName;
        this.userGender = userGender;
        this.totalNumberOfCopies = totalNumberOfCopies;
        this.profilePictureLink = profilePictureLink;
    }

    public User(String user_id, String userName, String userGender, String totalNumberOfCopies, String profilePictureLink) {
        this.userId = user_id;
        this.userName = userName;
        this.userGender = userGender;
        this.totalNumberOfCopies = totalNumberOfCopies;
        this.profilePictureLink = profilePictureLink;

    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserGender() {
        return userGender;
    }

    public void setUserGender(String userGender) {
        this.userGender = userGender;
    }

    public String getSignUpPeriod() {
        return signUpPeriod;
    }

    public void setSignUpPeriod(String signUpPeriod) {
        this.signUpPeriod = signUpPeriod;
    }

    public String getProfilePictureLink() {
        return profilePictureLink;
    }

    public void setProfilePictureLink(String profilePictureLink) {
        this.profilePictureLink = profilePictureLink;
    }

    public String getTotalNumberOfCopies() {
        return totalNumberOfCopies;
    }

    public void setTotalNumberOfCopies(String totalNumberOfCopies) {
        this.totalNumberOfCopies = totalNumberOfCopies;
    }

    public Boolean getActivated() {
        return isActivated;
    }

    public void setActivated(Boolean activated) {
        isActivated = activated;
    }

    public Boolean getLoggedIn() {
        return isLoggedIn;
    }

    public void setLoggedIn(Boolean loggedIn) {
        isLoggedIn = loggedIn;
    }
}
