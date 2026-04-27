package com.huellas.model;

import java.time.LocalDateTime;

public sealed abstract class User permits Client, Veterinarian {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String password;
    private boolean active;
    private Role role;
    private LocalDateTime createdAt;

    protected User() {
        this.createdAt = LocalDateTime.now();
    }

    //se crean protected puesto que es una clase abstracta y solo podran acceder a sus constructores las clases hijas. visibilidad para la clase que hereda utilizando el constructor padre 
    protected User(Long id, String name, String email, String phone, String address, String password, boolean active, Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.password = password;
        this.active = active;
        this.role = role;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
