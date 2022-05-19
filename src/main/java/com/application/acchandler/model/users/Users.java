package com.application.acchandler.model.users;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="Users")
public class Users {

    public Users()
    {

    }

    public Users(Users user)
    {
        UserId = user.getUserID();
        DisplayName = user.getDisplayName();
        NickName = user.getNickName();
        PhoneNumber = user.getPhoneNumber();
        EmailID = user.getEmailID();
        Gender = user.getGender();
        GoogleId = user.getGoogleId();
        IsEmailVerified = user.getIsEmailVerified();
        IsPhoneNumberVerified = user.getIsPhoneNumberVerified();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserId", columnDefinition = "INTEGER")
    private Integer UserId;

    @Column(name="DisplayName", columnDefinition = "VARCHAR(255)")
    private String DisplayName;

    @Column(name="NickName", columnDefinition = "VARCHAR(255)")
    private String NickName;

    @Column(name="PhoneNumber", columnDefinition = "BIGINT")
    private Long PhoneNumber;

    @Column(name="EmailId", columnDefinition = "VARCHAR(255)")
    private String EmailID;

    @Column(name="Gender", columnDefinition = "TINYINT(4) default 0")
    private int Gender;

    @Column(name="LatestOtp", columnDefinition = "VARCHAR(100)")
    private String LatestOtp;

    @Column(name="OTPExpirationTime", columnDefinition = "BIGINT")
    private Long OtpExpirationTime;

    @Column(name="ProfilePicture", columnDefinition = "VARCHAR(100)")
    private String ProfilePicture;

    @Column(name="GoogleId", columnDefinition = "VARCHAR(255)")
    private String GoogleId;

    @Column(name="IsPhoneNumberVerified", columnDefinition = "TINYINT(1) default false")
    private boolean IsPhoneNumberVerified;

    @Column(name="IsEmailVerified", columnDefinition = "TINYINT(1) default false")
    private boolean IsEmailVerified;

    public void setDisplayName(String displayName){
        this.DisplayName = displayName;
    }

    public Integer getUserID(){
        return this.UserId;
    }

    public void setUserId(Integer userId){
        this.UserId = userId;
    }

    public String getDisplayName(){
        return this.DisplayName;
    }

    public void setNickName(String nickName){
        this.NickName = nickName;
    }

    public String getNickName(){
        return this.NickName;
    }

    public void setPhoneNumber(Long phoneNumber){
        this.PhoneNumber = phoneNumber;
    }

    public Long getPhoneNumber(){
        return this.PhoneNumber;
    }

    public void setEmailID(String emailID){
        this.EmailID = emailID;
    }

    public String getEmailID(){
        return this.EmailID;
    }

    public void setGender(int gender){
        this.Gender = gender;
    }

    public int getGender(){
        return this.Gender;
    }

    public void setLatestOtp(String otp){
        this.LatestOtp = otp;
    }

    public String getLatestOtp(){
        return this.LatestOtp;
    }

    public void setOtpExpirationTime(Long expirationTime){
        this.OtpExpirationTime = expirationTime;
    }

    public Long getOtpExpirationTime(){
        return this.OtpExpirationTime;
    }

    public void setProfilePicture(String fileName){ this.ProfilePicture = fileName; }

    public String getProfilePicture() {
        return this.ProfilePicture;
    }

    public void setGoogleId(String googleId) { this.GoogleId = googleId; }

    public String getGoogleId() { return this.GoogleId; }

    public void setIsPhoneNumberVerified(boolean isPhoneNumberVerified) { this.IsPhoneNumberVerified = isPhoneNumberVerified; }

    public boolean getIsPhoneNumberVerified() { return this.IsPhoneNumberVerified; }

    public void setIsEmailVerified(boolean isEmailVerified) { this.IsEmailVerified = isEmailVerified; }

    public boolean getIsEmailVerified() { return this.IsEmailVerified; }

    public List<String> getPossibleSessionPrincipalNames(){
        List<String> possibleValues = new ArrayList<>();

        if (PhoneNumber != null)
            possibleValues.add(String.valueOf(PhoneNumber));
        if (GoogleId != null)
            possibleValues.add(String.valueOf(GoogleId));

        return possibleValues;

    }
}
