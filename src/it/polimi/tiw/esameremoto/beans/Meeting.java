package it.polimi.tiw.esameremoto.beans;

import java.util.Date;

public class Meeting {

    private int id;
    private String name;
    private Date date;
    private String description;

    public Meeting(){}

    public Meeting(int id, String name, Date date, String description){
        this.setId(id);
        this.setName(name);
        this.setDate(date);
        this.setDescription(description);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
