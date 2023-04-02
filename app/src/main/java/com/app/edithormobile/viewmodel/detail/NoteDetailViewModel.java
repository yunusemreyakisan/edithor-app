package com.app.edithormobile.viewmodel.detail;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.app.edithormobile.databinding.ActivityNoteDetailBinding;
import com.app.edithormobile.model.NoteModel;
import com.app.edithormobile.service.APIService;
import com.app.edithormobile.util.Util;
import com.app.edithormobile.view.home.NotePage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;

import org.json.JSONException;

import java.util.HashMap;


public class NoteDetailViewModel extends ViewModel {
    Util util = new Util();
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
        util.getCopiedObject(app, deger);
    }

    //assign data
    public void assignData(ActivityNoteDetailBinding binding, String baslik, String icerik, String olusturmaZamani, int notRengi, boolean pin) {
        binding.txtDetailTitle.setText(baslik);
        binding.txtDetailContent.setText(icerik);
        binding.tvDetailOlusturmaZamani.setText(olusturmaZamani);
        binding.tvSonDuzenlemeZamani.setText(olusturmaZamani); //Toolbar üzerinde son düzenleme tarihinin gosterilmesi
        binding.scrollView2.setBackgroundColor(notRengi);
        if(pin){
            binding.btnDetailPin.setBackgroundColor(Color.CYAN);
        }else{
            binding.btnDetailPin.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    //TODO: NOT DETAYDAN GERİYE GELİNCE KAYIT EDERKEN İMAGE SİLİNMESİ
    //Note Update
    public void updateNote(ActivityNoteDetailBinding binding, DatabaseReference mDatabaseReference, NoteModel position, String notID, int notRengi, String olusturma_zamani,Boolean pinned, String imageUri, Util util, Context context) {
        //Düzenleme tarihi eklenmesi
        binding.tvDetailOlusturmaZamani.setText(olusturma_zamani);
        String image = position.getImageUri();
        Log.e("İmage update note", image);

        HashMap<String, Object> map = new HashMap<>();
        map.put("notBaslik", binding.txtDetailTitle.getText().toString());
        map.put("notIcerigi", binding.txtDetailContent.getText().toString());
        map.put("notOlusturmaTarihi", binding.tvDetailOlusturmaZamani.getText().toString());
        map.put("color", notRengi);
        map.put("date", util.getDateAnotherPattern());
        map.put("pinned", pinned);
        map.put("imageUri", imageUri);
        mDatabaseReference.child(notID).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    position.setNotBaslik(binding.txtDetailTitle.getText().toString());
                    position.setNotIcerigi(binding.txtDetailContent.getText().toString());
                    position.setNotOlusturmaTarihi(olusturma_zamani);
                    position.setColor(notRengi);
                    position.setDate(util.getDateAnotherPattern());
                    position.setImageUri(imageUri);
                    binding.tvSonDuzenlemeZamani.setText(olusturma_zamani);
                    position.setPinned(pinned);
                    //TODO: Position nesnesi ile o pozisyona ait object alınıyor.
                    //adapter.notifyDataSetChanged(); //null dönüyor!!!

                    //util.toastMessage(context, "Notunuz güncellendi").show();
                    Intent intent = new Intent(context, NotePage.class);
                    intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            }
        }).addOnFailureListener(e -> util.toastMessage(context, "Hata oluştu").show());

    }

}
