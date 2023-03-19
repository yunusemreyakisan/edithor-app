package com.app.edithormobile.viewmodel.gpt;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.app.edithormobile.model.GPTModel;

public class GPTViewModel extends ViewModel {
    MutableLiveData<GPTModel> mesajlar = new MutableLiveData<>();

    //TODO: LiveData ile verileri çek.

    //Mesajları getir
    MutableLiveData<GPTModel> getMesajlar() {
        if (mesajlar == null) {
            mesajlar = new MutableLiveData<>();
        }
        return mesajlar;
    }
}
