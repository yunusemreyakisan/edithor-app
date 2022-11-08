package com.app.edithormobile.models;


public class NoteModel {

    String notIcerigi, notBaslik, notOlusturmaTarihi;


    public NoteModel() {
    }

    public NoteModel( String notIcerigi, String notBaslik, String notOlusturmaTarihi) {
        this.notIcerigi = notIcerigi;
        this.notBaslik = notBaslik;
        this.notOlusturmaTarihi = notOlusturmaTarihi;
    }

    public String getNotOlusturmaTarihi() {
        return notOlusturmaTarihi;
    }

    public void setNotOlusturmaTarihi(String notOlusturmaTarihi) {
        this.notOlusturmaTarihi = notOlusturmaTarihi;
    }

    public String getNotBaslik() {
        return notBaslik;
    }

    public void setNotBaslik(String notBaslik) {
        this.notBaslik = notBaslik;
    }

    public String getNotIcerigi() {
        return notIcerigi;
    }

    public void setNotIcerigi(String notIcerigi) {
        this.notIcerigi = notIcerigi;
    }
}
