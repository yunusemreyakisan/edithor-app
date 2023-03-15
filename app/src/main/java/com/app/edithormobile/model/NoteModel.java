package com.app.edithormobile.model;


import java.io.Serializable;

public class NoteModel implements Serializable {

    String notIcerigi, notBaslik, notOlusturmaTarihi, noteID, imageUri;
    boolean isSelected = false;
    int color;


    public NoteModel() {
    }

    //with images
    public NoteModel(String noteID, String notIcerigi, String notBaslik, String notOlusturmaTarihi, String imageUri, Boolean isSelected, int color) {
        this.notIcerigi = notIcerigi;
        this.isSelected = isSelected;
        this.noteID = noteID;
        this.imageUri = imageUri;
        this.notBaslik = notBaslik;
        this.notOlusturmaTarihi = notOlusturmaTarihi;
        this.color = color;
    }

    //without images
    public NoteModel(String noteID, String notIcerigi, String notBaslik, String notOlusturmaTarihi, Boolean isSelected, int color) {
        this.notIcerigi = notIcerigi;
        this.isSelected = isSelected;
        this.noteID = noteID;
        this.notBaslik = notBaslik;
        this.notOlusturmaTarihi = notOlusturmaTarihi;
        this.color = color;
    }

    public NoteModel(String noteID) {
        this.noteID = noteID;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getNoteID() {
        return noteID;
    }

    public void setNoteID(String noteID) {
        this.noteID = noteID;
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

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;

        NoteModel itemCompare = (NoteModel) obj;
        if (itemCompare.getNoteID().equals(this.getNoteID()))
            return true;

        return false;
    }

}

