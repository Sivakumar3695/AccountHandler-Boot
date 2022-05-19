package com.application.acchandler.controllers.user;

import org.springframework.util.StringUtils;

import java.util.List;

public enum GenderEnum {
    MALE(1, "male", "Male"),
    FEMALE(2, "female", "Female"),
    NOT_MENTIONED(0, "not_mentioned", "Not Mentioned");

    private int genderType;
    private String genderCode;
    private String genderDisplayName;

    private GenderEnum(int genderType, String genderCode, String genderDisplayName){
        this.genderType = genderType;
        this.genderCode = genderCode;
        this.genderDisplayName = genderDisplayName;
    }

    private static GenderEnum findByGenderType(int genderType) throws Exception
    {
        for (GenderEnum gender : GenderEnum.values())
        {
            if (gender.genderType == genderType)
                return gender;
        }
        throw new Exception("Invalid Gender Type provided");
    }

    private static GenderEnum findByGenderCode(String genderCode) throws Exception
    {
        for (GenderEnum gender : GenderEnum.values())
        {
            if (StringUtils.pathEquals(gender.genderCode, genderCode))
                return gender;
        }
        throw new Exception("Invalid Gender Type provided");
    }

    public static String getGenderCode(int genderType) throws Exception
    {
        GenderEnum gender = findByGenderType(genderType);
        return gender.genderCode;
    }

    public static String getGenderDisplayName(int genderType) throws Exception
    {
        GenderEnum gender = findByGenderType(genderType);
        return gender.genderDisplayName;
    }

    public static int getGenderType(String genderCode) throws Exception
    {
        GenderEnum gender = findByGenderCode(genderCode);
        return gender.genderType;
    }
}
