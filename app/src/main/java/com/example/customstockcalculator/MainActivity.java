package com.example.customstockcalculator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.customstockcalculator.Listeners.OnValueChangedListener;
import com.example.customstockcalculator.ViewModels.MainActivityViewModel;

public class MainActivity extends AppCompatActivity implements OnValueChangedListener, TextWatcher {

    private CustomNumberPicker customNumberPicker;
    private EditText price_edit_text;
    private TextView total_text_view;
    private Button btn_calculate;
    private int pricePerItem = 0;
    private int totalPrice = 0;
    private int valueOfNumberPicker = 0;
    private MainActivityViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        price_edit_text = findViewById(R.id.price_edit_text);
        total_text_view = findViewById(R.id.total_text_view);
        customNumberPicker = findViewById(R.id.custom_number_picker);
        //changing 'customNumberPicker' attributes manually
        try {
            customNumberPicker.setOnValueChangedListener(this);
            customNumberPicker.setValueOfPicker(10);
            customNumberPicker.setMinValueOfPicker(1);
            customNumberPicker.setMaxValueOfPicker(200);
            customNumberPicker.setDefaultLongPressIntervalValue(100);
            customNumberPicker.setLongPressPeriodOfSubtractFromIntervalValue(600);
            customNumberPicker.setLongPressSubtractFromIntervalValue(10);
        }catch (Exception e) {
            e.printStackTrace();
            Log.d("initial_values_error",e.getMessage()+"");
        }

        price_edit_text.addTextChangedListener(this);
        viewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);
        viewModel.initializeValues(customNumberPicker.getValueOfPicker());

        Log.d("ValueOfNumberPicker",viewModel.getValueOfNumberPicker().getValue().toString());
        viewModel.getTotalPrice().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {

                Log.d("viewModelTotalPrice", integer+"");
               total_text_view.setText(integer +"");
            }
        });
    }

    @Override
    public void onValueChanged(int newValue) {
        int currentPricePerItem = this.viewModel.getPricePerItem().getValue();
        Log.d("modelViewPriceItem",currentPricePerItem+"");
        int totalValue = newValue * currentPricePerItem;
        this.viewModel.getValueOfNumberPicker().setValue(newValue);
        this.viewModel.getTotalPrice().setValue(totalValue);
        Log.d("valueNumberPickerChange",this.viewModel.getValueOfNumberPicker().getValue().toString()+"");
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        int integerValueOfPricePerItem = 0;
        Log.d("priceChanged",s.toString());
        if(!s.toString().equals("")){
            integerValueOfPricePerItem = Integer.parseInt(s.toString());
        }

        this.viewModel.getPricePerItem().setValue(integerValueOfPricePerItem);
        int currentValueOfNumberPicker =this.viewModel.getValueOfNumberPicker().getValue();
        int totalValue = integerValueOfPricePerItem * currentValueOfNumberPicker;
        this.viewModel.getTotalPrice().setValue(totalValue);
    }

    @Override
    public void afterTextChanged(Editable s) {



    }
}
