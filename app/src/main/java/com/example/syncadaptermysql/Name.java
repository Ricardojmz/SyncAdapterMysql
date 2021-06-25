package com.example.syncadaptermysql;

public class Name {
    //Variables que mandamos a llamar
    private String name;
    private int status;
    private String phone;

    public Name(String name, String phone, int status) {
        this.name = name;
        this.status = status;
        this.phone = phone;
    }
    //Creación de getters
    public String getName() {
        return name;
    }

    public int getStatus() {
        return status;
    }

    public String getPhone(){
        return phone;
    }
}

