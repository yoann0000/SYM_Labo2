package com.example.labo2.ui.main;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

public class PageViewModel extends ViewModel {

    private MutableLiveData<Integer> mIndex = new MutableLiveData<>();
    private LiveData<String> mText = Transformations.map(mIndex, new Function<Integer, String>() {
        @Override
        public String apply(Integer input) {
            switch(input) {
                case 1: return "Envoi / Réception";
                case 2: return "Envoi / Minimum logs";
                case 3: return "Envoi / Minimum logs";
                case 4: return "Envoi / Réception";
                case 5: return "Choix d'un auteur: / Postes de l'auteur";
                default: return "null";
            }
        }
    });

    public void setIndex(int index) {
        mIndex.setValue(index);
    }

    public LiveData<String> getText() {
        return mText;
    }
}