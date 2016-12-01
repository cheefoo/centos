package com.tayo.centos;

/**
 * Created by temitayo on 11/3/16.
 */
public class UserDAO
{
    private String Id;
    private String userId;
    private String fullName;
    private String gender;
    private String relationshipStatus;
    private String activityTimestamp;
    private String activityType;
    private String activityMetadata;


    public UserDAO(String id, String userId, String fullName, String gender, String relationshipStatus, String activityTimestamp, String activityType)
    {
        Id = id;
        this.userId = userId;
        this.fullName = fullName;
        this.gender = gender;
        this.relationshipStatus = relationshipStatus;
        this.activityTimestamp = activityTimestamp;
        this.activityType = activityType;
    }

    public UserDAO(String id, String userId, String fullName, String gender, String relationshipStatus, String activityTimestamp, String activityType, String activityMetadata)
    {
        Id = id;
        this.userId = userId;
        this.fullName = fullName;
        this.gender = gender;
        this.relationshipStatus = relationshipStatus;
        this.activityTimestamp = activityTimestamp;
        this.activityType = activityType;
        this.activityMetadata = activityMetadata;
    }

    public String getId()
    {
        return Id;
    }

    public void setId(String id)
    {
        Id = id;
    }

    public String getUserId()
    {
        return userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    public String getFullName()
    {
        return fullName;
    }

    public void setFullName(String fullName)
    {
        this.fullName = fullName;
    }

    public String getGender()
    {
        return gender;
    }

    public void setGender(String gender)
    {
        this.gender = gender;
    }

    public String getRelationshipStatus()
    {
        return relationshipStatus;
    }

    public void setRelationshipStatus(String relationshipStatus)
    {
        this.relationshipStatus = relationshipStatus;
    }

    public String getActivityTimestamp()
    {
        return activityTimestamp;
    }

    public void setActivityTimestamp(String activityTimestamp)
    {
        this.activityTimestamp = activityTimestamp;
    }

    public String getActivityType()
    {
        return activityType;
    }

    public void setActivityType(String activityType)
    {
        this.activityType = activityType;
    }

    public String getActivityMetadata()
    {
        return activityMetadata;
    }

    public void setActivityMetadata(String activityMetadata)
    {
        this.activityMetadata = activityMetadata;
    }

    @Override
    public String toString()
    {
        return  Id + ","+  userId + "," + fullName + "," + gender + "," 
    + relationshipStatus + "," + activityTimestamp + "," + activityType + "," + activityMetadata;
    }
}
