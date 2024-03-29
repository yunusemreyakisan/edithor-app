package com.app.edithormobile.util;

import static android.content.Context.CLIPBOARD_SERVICE;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.view.ViewCompat;

import com.app.edithormobile.R;
import com.app.edithormobile.model.NoteModel;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;

public class Util {

    //Olusturma zamani al
    public String olusturmaZamaniGetir(Context context) {
        //Olusturma zamanini al.
        Calendar calendar = new GregorianCalendar();
        int month = calendar.get(Calendar.MONTH) + 1; //0 ile basladigi icin 1 eklendi.
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);
        String time = String.format("%02d:%02d", hours, minutes);
        String ay = null;
        int ayinGunu = calendar.get(Calendar.DAY_OF_MONTH);

        //Ay
        switch (month) {
            case 1:
                ay = context.getResources().getString(R.string.Ocak);
                break;
            case 2:
                ay = context.getResources().getString(R.string.Subat);
                break;
            case 3:
                ay = context.getResources().getString(R.string.Mart);
                break;

            case 4:
                ay = context.getResources().getString(R.string.Nisan);
                break;
            case 5:
                ay = context.getResources().getString(R.string.Mayis);
                break;
            case 6:
                ay = context.getResources().getString(R.string.Haziran);
                break;
            case 7:
                ay = context.getResources().getString(R.string.Temmuz);
                break;
            case 8:
                ay = context.getResources().getString(R.string.Agustos);
                break;
            case 9:
                ay = context.getResources().getString(R.string.Eylul);
                break;
            case 10:
                ay = context.getResources().getString(R.string.Ekim);
                break;
            case 11:
                ay = context.getResources().getString(R.string.Kasim);
                break;
            case 12:
                ay = context.getResources().getString(R.string.Aralik);
                break;
        }

        String notOlusturmaTarihi = ayinGunu + " " + ay + " " + time;

        return notOlusturmaTarihi;
    }

    //Zamanı farklı pattern ile alır.
    public String getDateAnotherPattern(){
        //Olusturma zamanini al.
        Calendar cal = Calendar.getInstance(); // Anlık tarihi alır
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1; // 0-11 arası bir değer döndürür, bu yüzden 1 ekliyoruz
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int hour = cal.get(Calendar.HOUR_OF_DAY); // 24 saatlik format için HOUR_OF_DAY kullanıyoruz
        int minute = cal.get(Calendar.MINUTE);
        int second = cal.get(Calendar.SECOND);
        String date = null;
        if(month < 9){
            date = day + " / " +"0"+ month + " / " + year + "   " + hour + ":" +  minute + ":" + second;
        }else{
            date = day + " / " + month + " / " + year + "   " + hour + ":" +  minute + ":" + second;
        }
        return date;
    }

    //Toast mesaji goster
    public Toast toastMessage(Context context, String message) {
        return Toast.makeText(context, message, Toast.LENGTH_SHORT);
    }

    //Set snacbar elevation
    public void configSnackbar(Context context, Snackbar snack) {
        ViewCompat.setElevation(snack.getView(), 0f);
    }

    //Set snacbar drawable
    public void setDrawableSnackbar(Snackbar snackbar, int drawable) {
        snackbar.getView().setBackgroundResource(drawable);
    }


    //Bos Kontrollu deger donduren method
    public String bosKontrolluDeger(String deger) {
        if (TextUtils.isEmpty(deger)) {
            deger = "İçerik yok";
        } else {
            return deger;
        }

        return deger;
    }

    //İçeriği kopyala
    public void getCopiedObject(Context context, String icerik) {
        // ClipboardManager nesnesini al
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
        // Metin değerini kopyala
        ClipData clip = ClipData.newPlainText("label", icerik);
        clipboard.setPrimaryClip(clip);
    }


    //Kullanıcı verilerini cek
    public void kullaniciyiGetir() {
        //TODO Kullanıcı verilerini bir diziye at, o diziyi her yerden cagir.
    }
}
