package com.app.edithormobile.util;

import static android.content.Context.CLIPBOARD_SERVICE;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.core.view.ViewCompat;

import com.google.android.material.snackbar.Snackbar;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class Util {
    //Olusturma zamani al
    public String olusturmaZamaniGetir() {
        //Olusturma zamanini al.
        Calendar calendar = new GregorianCalendar();
        int month = calendar.get(Calendar.MONTH) + 1; //0 ile basladigi icin 1 eklendi.
        int hours = calendar.get(Calendar.HOUR);
        int minutes = calendar.get(Calendar.MINUTE);
        String time = String.format("%02d:%02d", hours, minutes);
        String ay = null;
        int ayinGunu = calendar.get(Calendar.DAY_OF_MONTH);

        /*
        if (calendar.get(Calendar.DAY_OF_MONTH) < 9) {
            ayinGunu = "0" + calendar.get(Calendar.DAY_OF_MONTH);
        } else {
            ayinGunu = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        }

         */

        //Ay
        switch (month) {
            case 1:
                ay = "Ocak";
                break;
            case 2:
                ay = "Şubat";
                break;
            case 3:
                ay = "Mart";
                break;

            case 4:
                ay = "Nisan";
                break;
            case 5:
                ay = "Mayıs";
                break;
            case 6:
                ay = "Haziran";
                break;
            case 7:
                ay = "Temmuz";
                break;
            case 8:
                ay = "Ağustos";
                break;
            case 9:
                ay = "Eylül";
                break;
            case 10:
                ay = "Ekim";
                break;
            case 11:
                ay = "Kasım";
                break;
            case 12:
                ay = "Aralık";
                break;
        }

        String notOlusturmaTarihi = ayinGunu + " " + ay + " " + time;

        return notOlusturmaTarihi;
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
