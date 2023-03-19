package com.app.edithormobile.viewmodel;

import static android.content.Context.CLIPBOARD_SERVICE;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.app.Application;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.widget.TextView;

import androidx.lifecycle.ViewModel;

import com.app.edithormobile.databinding.ActivityNoteDetailBinding;
import com.app.edithormobile.service.APIService;

import org.json.JSONException;


public class NoteDetailViewModel extends ViewModel {
    String url = "https://api.openai.com/v1/chat/completions";


    //Get GPT API Url
    public String getAPIUrl() {
        return url;
    }

    //Custom dialog send message button listener
    //TODO: Chat sisteminde response Türkçe gelirken custom dialog üzerinde ingilizce geliyor. Çözmeliyiz.
    public void sendMessageGPT(String ifade, TextView text, Context context) {
        APIService service = new APIService();
        try {
            service.getResponse(ifade, text, context);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    //İçeriği kopyala
    public void getCopiedObject(Context context, String icerik) {
        // ClipboardManager nesnesini al
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
        // Metin değerini kopyala
        ClipData clip = ClipData.newPlainText("label", icerik);
        clipboard.setPrimaryClip(clip);
    }

    //Notu paylaş
    public void shareNote(Application app, ActivityNoteDetailBinding binding) {
        String baslik = binding.txtDetailTitle.getText().toString().trim();
        String icerik = binding.txtDetailContent.getText().toString().trim();
        //Set copied text
        String deger = "Not başlığı: " + baslik + "\nNot içeriği: " + icerik + "\nEdithor uygulamasından gönderildi";
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, deger);
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        shareIntent.setFlags(FLAG_ACTIVITY_NEW_TASK);
        app.startActivity(shareIntent);
    }

    //Notu kopyala
    public void copyNote(Application app, ActivityNoteDetailBinding binding) {
        String baslik = binding.txtDetailTitle.getText().toString().trim();
        String icerik = binding.txtDetailContent.getText().toString().trim();
        //Set copied text
        String deger = "Not başlığı: " + baslik + "\nNot içeriği: " + icerik;
        getCopiedObject(app, deger);
    }

    //assign data
    public void assignData(ActivityNoteDetailBinding binding, String baslik, String icerik, String olusturmaZamani, int notRengi) {
        binding.txtDetailTitle.setText(baslik);
        binding.txtDetailContent.setText(icerik);
        binding.tvDetailOlusturmaZamani.setText(olusturmaZamani);
        binding.tvSonDuzenlemeZamani.setText(olusturmaZamani); //Toolbar üzerinde son düzenleme tarihinin gosterilmesi
        binding.scrollView2.setBackgroundColor(notRengi);
    }


}
