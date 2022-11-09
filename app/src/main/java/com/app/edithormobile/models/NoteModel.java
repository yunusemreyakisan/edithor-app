package com.app.edithormobile.models;


public class NoteModel {

    String notIcerigi, notBaslik, notOlusturmaTarihi, noteID;
    boolean isSelected = false;


    public NoteModel() {
    }

    public NoteModel( String noteID, String notIcerigi, String notBaslik, String notOlusturmaTarihi, boolean isSelected) {
        this.notIcerigi = notIcerigi;
        this.isSelected = isSelected;
        this.noteID = noteID;
        this.notBaslik = notBaslik;
        this.notOlusturmaTarihi = notOlusturmaTarihi;
    }

    public String getNoteID() {
        return noteID;
    }

    public void setNoteID(String noteID) {
        this.noteID = noteID;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
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
