package com.app.edithormobile.model;


import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

public class NoteModel implements Serializable, Comparable<NoteModel> {

    String notIcerigi, notBaslik, notOlusturmaTarihi, noteID, imageUri, date;
    boolean isSelected = false;
    boolean isPinned;
    int color;


    public NoteModel() {
    }

    //with images
    public NoteModel(String noteID, String notIcerigi, String notBaslik, String notOlusturmaTarihi, String imageUri, Boolean isSelected, int color, String date, Boolean isPinned) {
        this.notIcerigi = notIcerigi;
        this.isSelected = isSelected;
        this.noteID = noteID;
        this.imageUri = imageUri;
        this.notBaslik = notBaslik;
        this.notOlusturmaTarihi = notOlusturmaTarihi;
        this.color = color;
        this.date = date;
        this.isPinned = isPinned;
    }

    //without images
    public NoteModel(String noteID, String notIcerigi, String notBaslik, String notOlusturmaTarihi, Boolean isSelected, int color, String date, Boolean isPinned) {
        this.notIcerigi = notIcerigi;
        this.isSelected = isSelected;
        this.noteID = noteID;
        this.notBaslik = notBaslik;
        this.notOlusturmaTarihi = notOlusturmaTarihi;
        this.color = color;
        this.date = date;
        this.isPinned = isPinned;
    }

    public boolean isPinned() {
        return isPinned;
    }

    public void setPinned(boolean pinned) {
        isPinned = pinned;
    }

    public NoteModel(String noteID) {
        this.noteID = noteID;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NoteModel noteModel = (NoteModel) o;
        return isSelected == noteModel.isSelected && color == noteModel.color && Objects.equals(notIcerigi, noteModel.notIcerigi) && Objects.equals(notBaslik, noteModel.notBaslik) && Objects.equals(notOlusturmaTarihi, noteModel.notOlusturmaTarihi) && Objects.equals(noteID, noteModel.noteID) && Objects.equals(imageUri, noteModel.imageUri) && Objects.equals(date, noteModel.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(notIcerigi, notBaslik, notOlusturmaTarihi, noteID, imageUri, date, isSelected, color);
    }

    //En yeniden en eskiye sıralama
    @Override
    public int compareTo(NoteModel o) {
        return o.date.compareTo(this.date);
    }
}

