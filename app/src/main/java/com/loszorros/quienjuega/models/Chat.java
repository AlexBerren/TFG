package com.loszorros.quienjuega.models;

import java.util.ArrayList;
import java.util.List;

public class Chat {
    private String id = "";
    private String name = "";

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Chat() {
    }

    public Chat(String id, String name, List<String> users) {
        this.id = id;
        this.name = name;
        this.users = users;
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }

    private List<String> users = new ArrayList<>();

}
