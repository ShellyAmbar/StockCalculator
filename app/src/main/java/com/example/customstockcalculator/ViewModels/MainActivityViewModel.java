package com.example.customstockcalculator.ViewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.customstockcalculator.Listeners.OnValueChangedListener;

public class MainActivityViewModel extends ViewModel  {

    private MutableLiveData<Integer> valueOfNumberPicker=null;
    private MutableLiveData<Integer> pricePerItem=null;
    private MutableLiveData<Integer> totalPrice=null;
    private MainActivityViewModel mainActivityViewModelInstance = null;

    public void initializeValues(int initialValueOfNumberPicker){

        if(mainActivityViewModelInstance==null){
            valueOfNumberPicker = new MutableLiveData<Integer>();
            pricePerItem = new MutableLiveData<Integer>();
            totalPrice = new MutableLiveData<Integer>();

            valueOfNumberPicker.setValue(initialValueOfNumberPicker);
            pricePerItem.setValue(0);
            totalPrice.setValue(0);

            mainActivityViewModelInstance = new MainActivityViewModel();
        }

    }

    public MutableLiveData<Integer> getValueOfNumberPicker() {
        return valueOfNumberPicker;
    }

    public MutableLiveData<Integer> getPricePerItem() {
        return pricePerItem;
    }

    public MutableLiveData<Integer> getTotalPrice() {
        return totalPrice;
    }


}
