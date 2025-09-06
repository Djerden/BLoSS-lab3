package com.djeno.lab1.persistence.wrappers;

import com.djeno.lab1.persistence.models.User;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.List;

@XmlRootElement(name = "users")
public class UsersWrapper {
    private List<User> users;

    public UsersWrapper() {}

    public UsersWrapper(List<User> users) {
        this.users = users;
    }

    @XmlElement(name = "user")
    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
}
