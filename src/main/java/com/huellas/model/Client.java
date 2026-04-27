package com.huellas.model;

public final class Client extends User {

    public Client() {}

    public Client(Long id, String name, String email, String phone, String address, String password, boolean active, Role role) {
        super(id, name, email, phone, address, password,active, role);
    }

    
}
