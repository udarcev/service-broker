package com.demo.obs.crossbrowser.model;

import com.demo.obs.crossbrowser.service.ServiceBinding;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.util.Objects;

@Entity
public class GGRServiceBinding extends ServiceBinding {

    @Column(name = "USER_NAME", nullable = false)
    private String userName;
    @Column(name = "USER_PASSWORD", nullable = false)
    private String userPassword;

    public GGRServiceBinding(String serviceInstanceId, String bindingId, String applicationId, String userName, String password) {
        super(serviceInstanceId, bindingId, applicationId);
        this.userName = userName;
        this.userPassword = password;
    }

    public GGRServiceBinding() {

    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return userPassword;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        GGRServiceBinding that = (GGRServiceBinding) obj;
        return userName.equals(that.userName) &&
                userPassword.equals(that.userPassword);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), userName, userPassword);
    }

    @Override
    public String toString() {
        return "PostgreServiceBinding{" +
                "userName='" + userName + '\'' +
                '}';
    }
}

