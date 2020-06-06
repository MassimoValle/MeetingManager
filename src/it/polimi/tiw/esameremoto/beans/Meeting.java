package it.polimi.tiw.esameremoto.beans;

import java.sql.Date;
import java.sql.Time;

public class Meeting {

    private int idMeeting;
    private Time hour;
    private Date date;
    private String title;
    private Time duration;
    private int maxParticipantsNumber;
    private String usernameCreator;
    
    public int getIdMeeting() {
        return idMeeting;
    }
    
    public void setIdMeeting(int idMeeting) {
        this.idMeeting = idMeeting;
    }
    
    public Time getHour() {
        return hour;
    }
    
    public void setHour(Time hour) {
        this.hour = hour;
    }
    
    public Date getDate() {
        return date;
    }
    
    public void setDate(Date date) {
        this.date = date;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public Time getDuration() {
        return duration;
    }
    
    public void setDuration(Time duration) {
        this.duration = duration;
    }
    
    public int getMaxParticipantsNumber() {
        return maxParticipantsNumber;
    }
    
    public void setMaxParticipantsNumber(int maxParticipantsNumber) {
        this.maxParticipantsNumber = maxParticipantsNumber;
    }
    
    public String getUsernameCreator() {
        return usernameCreator;
    }
    
    public void setUsernameCreator(String usernameCreator) {
        this.usernameCreator = usernameCreator;
    }
}
