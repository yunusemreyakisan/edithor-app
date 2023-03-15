package com.app.edithormobile.model;

public class UserModel {

    String id, email, sifre, olusturulma_tarihi;

    public UserModel() {
    }

    public UserModel(String id, String email, String sifre, String olusturulma_tarihi) {
        this.id = id;
        this.email = email;
        this.sifre = sifre;
        this.olusturulma_tarihi = olusturulma_tarihi;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSifre() {
        return sifre;
    }

    public void setSifre(String sifre) {
        this.sifre = sifre;
    }

    public String getOlusturulma_tarihi() {
        return olusturulma_tarihi;
    }

    public void setOlusturulma_tarihi(String olusturulma_tarihi) {
        this.olusturulma_tarihi = olusturulma_tarihi;
    }
}
