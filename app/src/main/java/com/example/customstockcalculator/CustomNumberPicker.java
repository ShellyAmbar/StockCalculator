package com.example.customstockcalculator;

import android.content.Context;

import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;

import androidx.annotation.RequiresApi;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.customstockcalculator.Listeners.OnValueChangedListener;

import java.util.Timer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CustomNumberPicker extends LinearLayout implements  View.OnTouchListener , GestureDetector.OnGestureListener {
    private Button btn_plus,btn_minus;

    private int minValueOfPicker = 0;
    private int maxValueOfPicker = 0;
    private int valueOfPicker = 0;
    private TextView numberPickerText;
    private  long defaultLongPressIntervalValue=0;
    private long longPressSubtractFromIntervalValue=0;
    private long longPressPeriodOfSubtractFromIntervalValue=0;
    private  long currentLongPressIntervalValue =0;
    private OnValueChangedListener onValueChangedListener = null;
    private Handler handler = null;
    private GestureDetector gestureDetector;
    private boolean stopTimer = true;
    private static final String TAG_ACTION_EVENT = "motion_event";
    private static int currentButtonIdPressed =0;
    private ScheduledExecutorService scheduleTaskExecutor;
    private Object lockThreadLongPressTaskOperation;
    private boolean startSubtraction = false;

//constructors of custom view
    public CustomNumberPicker(Context context) {
        super(context);
        init(context, null);

    }

    public CustomNumberPicker(Context context,  AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CustomNumberPicker(Context context,  AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CustomNumberPicker(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }
//set listener
    public void setOnValueChangedListener(OnValueChangedListener onValueChangedListener) throws Exception {
        if(onValueChangedListener == null){
            throw new Exception("Listener must not be a null value");
        }else{
            Log.d("picker_set_listener","new listener had been added.");
            this.onValueChangedListener = onValueChangedListener;
        }
    }
//getters and setters of all attrs values
    public int getMinValueOfPicker() {
        return minValueOfPicker;
    }

    public void setMinValueOfPicker(int minValueOfPicker) throws Exception {
        if(minValueOfPicker > valueOfPicker || minValueOfPicker > maxValueOfPicker){
            throw new Exception("Minimum value must be less then the Maximum value & initial value oof picker.");
        }else{
            Log.d("picker_set_min_value ",minValueOfPicker+"");
            this.minValueOfPicker = minValueOfPicker;
        }
    }

    public int getMaxValueOfPicker() {
        return maxValueOfPicker;
    }

    public void setMaxValueOfPicker(int maxValueOfPicker) throws Exception {
        if(maxValueOfPicker < minValueOfPicker || maxValueOfPicker < valueOfPicker){
            throw new Exception("Max value must be higher then the Minimum value & initial value oof picker.");
        }else{
            Log.d("picker_set_max_value ",maxValueOfPicker+"");
            this.maxValueOfPicker = maxValueOfPicker;
        }
    }

    public int getValueOfPicker() {
        return valueOfPicker;
    }

    public void setValueOfPicker(int valueOfPicker) throws Exception {
        if(valueOfPicker < minValueOfPicker || valueOfPicker > maxValueOfPicker){
            throw new Exception("The initial value must be between: "+minValueOfPicker+"and: "+maxValueOfPicker+" .");
        }else{
            this.valueOfPicker = valueOfPicker;
            numberPickerText.setText(valueOfPicker+"");
            Log.d("picker_set_value ",valueOfPicker+"");
        }

    }

    public long getDefaultLongPressIntervalValue() {
        return defaultLongPressIntervalValue;
    }

    public void setDefaultLongPressIntervalValue(long defaultLongPressIntervalValue) throws Exception {
        if(defaultLongPressIntervalValue < 0 || defaultLongPressIntervalValue < longPressSubtractFromIntervalValue){
            throw new Exception("Initial value of interval must be higher or equal to '0' & subtracted value.");
        }else {
            Log.d("picker_set_interval ",defaultLongPressIntervalValue+"");
            this.defaultLongPressIntervalValue = defaultLongPressIntervalValue;
        }
    }

    public long getLongPressSubtractFromIntervalValue() {
        return longPressSubtractFromIntervalValue;
    }

    public void setLongPressSubtractFromIntervalValue(long longPressSubtractFromIntervalValue) throws Exception {
        if(longPressSubtractFromIntervalValue > defaultLongPressIntervalValue || longPressSubtractFromIntervalValue < 0){
            throw new Exception("Subtract value must higher or equal to '0' & interval value.");
        }else{
            Log.d("picker_set_subtract ",longPressSubtractFromIntervalValue+"");
            this.longPressSubtractFromIntervalValue = longPressSubtractFromIntervalValue;
        }
    }

    public long getLongPressPeriodOfSubtractFromIntervalValue() {
        return longPressPeriodOfSubtractFromIntervalValue;
    }

    public void setLongPressPeriodOfSubtractFromIntervalValue(long longPressPeriodOfSubtractFromIntervalValue) throws Exception {
        if(longPressPeriodOfSubtractFromIntervalValue < 0){
            throw new Exception("The long press period must be higher or equal to '0'.");
        }else{
            Log.d("picker_set_period",longPressPeriodOfSubtractFromIntervalValue+"");
            this.longPressPeriodOfSubtractFromIntervalValue = longPressPeriodOfSubtractFromIntervalValue;
        }
    }

    //init
    private void init(Context context, AttributeSet attrs) {
        View.inflate(context, R.layout.custom_number_picker, this);
        setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
        setSaveEnabled(true);
        handler = new Handler();
        btn_minus = findViewById(R.id.btn_minus);
        btn_plus = findViewById(R.id.btn_plus);
        numberPickerText = findViewById(R.id.number_picker_text);
        gestureDetector = new GestureDetector(context,this);
        lockThreadLongPressTaskOperation = new Object();
        btn_minus.setOnTouchListener(this);
        btn_plus.setOnTouchListener(this);


        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CustomNumberPicker);

            try {
                valueOfPicker = a.getInteger(R.styleable.CustomNumberPicker_numberPicker_value, 0);
                maxValueOfPicker = a.getInteger(R.styleable.CustomNumberPicker_numberPicker_maxValue, 0);
                minValueOfPicker = a.getInteger(R.styleable.CustomNumberPicker_numberPicker_minValue, 0);
                defaultLongPressIntervalValue =  a.getInteger(R.styleable.CustomNumberPicker_numberPicker_default_longPress_interval, 0);
                longPressSubtractFromIntervalValue =  a.getInteger(R.styleable.CustomNumberPicker_numberPicker_longPress_subtract_from_interval, 0);
                longPressPeriodOfSubtractFromIntervalValue =  a.getInteger(R.styleable.CustomNumberPicker_numberPicker_longPress_period_of_subtract_from_interval, 0);
                numberPickerText.setText(valueOfPicker+"");


            } catch (Exception e) {
                Log.e("error_attr", e.getMessage());

            } finally {
                a.recycle();
            }
        }
    }

    public void onClick() {
        int oldValue =0;
        int newValue = 0;

        switch (currentButtonIdPressed){

            case R.id.btn_minus:
              oldValue = Integer.parseInt(numberPickerText.getText().toString());
              newValue = oldValue -1;
                break;

            case R.id.btn_plus:
                oldValue = Integer.parseInt(numberPickerText.getText().toString());
                newValue = oldValue +1;
                break;
        }

        validateAndSetNewValueForNumberPicker(newValue);
    }

    public void  onLongClick() {
        int operation = 0;
        startSubtraction = false;
        currentLongPressIntervalValue = defaultLongPressIntervalValue;
        Log.d("get_initial_interval", getDefaultLongPressIntervalValue()+"");
        Log.d("get_period_subtraction", getLongPressPeriodOfSubtractFromIntervalValue()+"");
        Log.d("get_subtract_value", getLongPressSubtractFromIntervalValue()+"");
        Log.d("get_initial_value", getValueOfPicker()+"");
        Log.d("get_max_value", getMaxValueOfPicker()+"");
        Log.d("get_min_value", getMinValueOfPicker()+"");

        stopTimer = false;
        if (currentButtonIdPressed == R.id.btn_plus) {
            operation = 1;
        } else if (currentButtonIdPressed == R.id.btn_minus) {
            operation = -1;
        }

        final int finalOperation = operation;
        scheduleTaskExecutor= Executors.newScheduledThreadPool(1);
        Log.d("executor","new_thread");
        scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {

            int oldValue = 0;
            int newValue = 0;

                @Override
                public void run() {
                    if (stopTimer) {
                        Log.d("executor","shut_down");
                        scheduleTaskExecutor.shutdown();
                        return;
                    }
                    synchronized (lockThreadLongPressTaskOperation) {
                        oldValue = Integer.parseInt(numberPickerText.getText().toString());
                        newValue = oldValue + finalOperation;
                        Log.d("new_value ", newValue + "");
                        Log.d("operation:", finalOperation+"" );
                        Log.d("current_interval", currentLongPressIntervalValue + "");
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                validateAndSetNewValueForNumberPicker(newValue);
                            }
                        });

                        // start in delay --> subtract X time from the current interval
                        if(startSubtraction){
                            // subtract from the interval
                            if((currentLongPressIntervalValue - longPressSubtractFromIntervalValue)>= 0){
                                currentLongPressIntervalValue-=longPressSubtractFromIntervalValue;
                                Log.d("interval_decreased_to",currentLongPressIntervalValue+"");
                            }
                        }

                    }
                }
            },0, currentLongPressIntervalValue, TimeUnit.MILLISECONDS);

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startSubtraction = true;
                }
            },longPressPeriodOfSubtractFromIntervalValue);
    }

    private void validateAndSetNewValueForNumberPicker(int newValue) {
        if(newValue<= maxValueOfPicker && newValue>= minValueOfPicker ){

            numberPickerText.setText(newValue+"");
            this.valueOfPicker = newValue;
            btn_minus.setEnabled(true);
            btn_plus.setEnabled(true);

            if(onValueChangedListener!=null){
                onValueChangedListener.onValueChanged(newValue);
            }


        }else if(newValue > maxValueOfPicker){
            btn_plus.setEnabled(false);
            this.valueOfPicker = maxValueOfPicker;
            stopTimer=true;
            Toast.makeText(getContext(), getContext().getResources().getString(R.string.max_value_message) + maxValueOfPicker, Toast.LENGTH_SHORT).show();
        }else if(newValue < minValueOfPicker){
            btn_minus.setEnabled(false);
            this.valueOfPicker =  minValueOfPicker;
            stopTimer=true;
            Toast.makeText(getContext(), getContext().getResources().getString(R.string.min_value_message)+ minValueOfPicker, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
         if(event.getAction() == MotionEvent.ACTION_UP){
             Log.d(TAG_ACTION_EVENT,"up");
             stopTimer=true;
         }

         if(v.getId()==R.id.btn_minus){
            currentButtonIdPressed = R.id.btn_minus;
            gestureDetector.onTouchEvent(event);
            return true;
         }
         else if( v.getId()==R.id.btn_plus){
            currentButtonIdPressed = R.id.btn_plus;
            gestureDetector.onTouchEvent(event);
            return true;
         }
         else{
            return false;
         }
    }

    @Override
    public boolean onDown(MotionEvent e) {
        Log.d(TAG_ACTION_EVENT,"down");
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

        Log.d(TAG_ACTION_EVENT,"show press");
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        Log.d(TAG_ACTION_EVENT,"single press");
        onClick();

        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        Log.d("motion_event","scroll");
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        Log.d("motion_event","long press");
        onLongClick();
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        Log.d("motion_event","fling");
        return false;
    }



    private static class SavedState extends BaseSavedState {

        int currentValue;
        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);

            currentValue = in.readInt();
        }
        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(currentValue);
        }

        public static final Parcelable.Creator<SavedState> CREATOR
                = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    @Override
    public Parcelable onSaveInstanceState() {
        // Obtain any state that our super class wants to save.
        Parcelable superState = super.onSaveInstanceState();

        // Wrap our super class's state with our own.
        SavedState myState = new SavedState(superState);

        myState.currentValue = this.valueOfPicker;

        // Return our state along with our super class's state.
        return myState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        // Cast the incoming Parcelable to our custom SavedState. We produced
        // this Parcelable before, so we know what type it is.
        SavedState savedState = (SavedState) state;

        // Let our super class process state before we do because we should
        // depend on our super class, we shouldn't imply that our super class
        // might need to depend on us.
        super.onRestoreInstanceState(savedState.getSuperState());

        // Grab our properties out of our SavedState.

        this.valueOfPicker = savedState.currentValue;

        numberPickerText.setText(valueOfPicker+"");

    }


}
