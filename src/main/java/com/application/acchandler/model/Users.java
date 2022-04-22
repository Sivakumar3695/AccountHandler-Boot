package com.application.acchandler.model;

import javax.persistence.*;

@Entity
@Table(name="Users")
public class Users {

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


}
