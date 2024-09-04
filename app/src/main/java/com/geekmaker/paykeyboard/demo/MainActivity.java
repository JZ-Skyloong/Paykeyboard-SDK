package com.geekmaker.paykeyboard.demo;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.geekmaker.paykeyboard.DefaultKeyboardListener;
import com.geekmaker.paykeyboard.ICheckListener;
import com.geekmaker.paykeyboard.IPayRequest;
import com.geekmaker.paykeyboard.PayKeyboard;
import com.geekmaker.paykeyboard.USBDetector;

import java.math.BigDecimal;
import java.util.Arrays;
public class MainActivity extends AppCompatActivity implements ICheckListener {
    private Handler handler = new Handler();
    private EditText eventLog;
    private PayKeyboard keyboard;
    private EditText  wifi,baudrate;
    private EditText gprs;
    private USBDetector detector;
    private Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //textField = findViewById()
        setContentView(R.layout.activity_main);
        eventLog = findViewById(R.id.eventLog);

        wifi = findViewById(R.id.wifi);
        gprs = findViewById(R.id.gprs);



        baudrate = findViewById(R.id.baudrate);

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //Toast.makeText(ShowAddressActivity.this,"beforeTextChanged ",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //Toast.makeText(ShowAddressActivity.this,"onTextChanged ",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void afterTextChanged(Editable s) {
                //Toast.makeText(ShowAddressActivity.this,"afterTextChanged ",Toast.LENGTH_SHORT).show();
                updateSignal();
            }
        };
        wifi.addTextChangedListener(textWatcher);
        gprs.addTextChangedListener(textWatcher);
        spinner = findViewById(R.id.layoutList);
        spinner.setAdapter(new ArrayAdapter<String>(this,
                R.layout.support_simple_spinner_dropdown_item,
                Arrays.asList("默认布局","布局1","布局2")));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.i("KeyBoardAPP","select layout"+position);
                if(keyboard!=null){
                    Log.i("KeyBoardAPP","set layout"+position);
                    keyboard.setLayout(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        double result = 0.0D;
        double num1  = Double.parseDouble( "2176.39");
        double num2 =  Double.parseDouble("46.46");

        result = num1 + num2;
        String ret = (BigDecimal.valueOf(result)).toPlainString();
        Log.i("Calc",ret);
    }

    @Override
    protected void onStart() {
        super.onStart();
        detector =  PayKeyboard.getDetector(this);
        detector.setListener(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Log.i("KeyboardUI","activity start!!!!!!");
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                openKeyboard();
            }
        },1000);

    }

    private void openKeyboard(){
        if(keyboard==null||keyboard.isReleased()){

            keyboard = PayKeyboard.get(getApplicationContext());
            if(keyboard!=null) {
                if(spinner.getSelectedItemPosition()>=0) keyboard.setLayout(spinner.getSelectedItemPosition());
                if(baudrate.getText().length()>0){
                    keyboard.setBaudRate(Integer.parseInt(baudrate.getText().toString()));
                }
                keyboard.setListener(new DefaultKeyboardListener() {
                    @Override
                    public void onRelease() {
                        super.onRelease();
                        keyboard = null;
                        Log.i("KeyboardUI", "Keyboard release!!!!!!");
                        keyboard = null;
                        if(detector!=null) detector.resume();
                    }

                    @Override
                    public void onDisplayUpdate(final String text) {
                        super.onDisplayUpdate(text);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                eventLog.setMovementMethod(ScrollingMovementMethod.getInstance());
                                eventLog.setSelection(eventLog.getText().length(), eventLog.getText().length());
                                eventLog.getText().append(String.format("lastupdate  : %s \n ",text));
                            }
                        });
                        Log.i("KeyboardUI",String.format("display update %s",text));
                    }

                    @Override
                    public void onAvailable() {
                        super.onAvailable();
                        logMessage("keyboard version:"+keyboard.getVerionInfo());
                        if(keyboard==null) return;
                        if(detector!=null) detector.pause();
                        // keyboard.updateSign(PayKeyboard.SIGN_TYPE_W,4);
                        updateSignal();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(keyboard!=null) keyboard.showTip("    0129");
                                keyboard.playVoice(PayKeyboard.VOICE_PAYING);
                            }
                        },1000);

                    }

                    @Override
                    public void onException(Exception e) {
                        Log.i("KeyboardUI", "usb exception!!!!"+e.getMessage());
                        super.onException(e);
                        if(keyboard!=null) keyboard.release();
                        keyboard = null;

                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                openKeyboard();
                            }
                        }, 1000);

                    }



                    @Override
                    public void onPay(final IPayRequest request) {
                        super.onPay(request);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                final AlertDialog.Builder normalDialog =
                                        new AlertDialog.Builder(MainActivity.this);
                                normalDialog.setTitle("支付提示");
                                normalDialog.setMessage(String.format("请支付 %.2f 元", request.getMoney()));

                                normalDialog.setPositiveButton("支付成功",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                request.setResult(true);
                                            }
                                        });
                                normalDialog.setNegativeButton("支付失败",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                request.setResult(false);
                                            }
                                        });
                                normalDialog.show();

                            }

                        });
                    }

                    @Override
                    public void onKeyDown(final int keyCode, final String keyName) {
                        super.onKeyDown(keyCode, keyName);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                eventLog.setMovementMethod(ScrollingMovementMethod.getInstance());
                                eventLog.setSelection(eventLog.getText().length(), eventLog.getText().length());
                                eventLog.getText().append(String.format("key down event code : %s, name: %s \n ", keyCode, keyName));
                            }

                        });


                    }


                    @Override
                    public void onKeyUp(final int keyCode, final String keyName) {
                        super.onKeyUp(keyCode, keyName);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                eventLog.setMovementMethod(ScrollingMovementMethod.getInstance());
                                eventLog.setSelection(eventLog.getText().length(), eventLog.getText().length());
                                eventLog.getText().append(String.format("key Up event code : %s, name: %s \n ", keyCode, keyName));
                            }

                        });
                    }
                });
                keyboard.open();

            }
        }else{
            Log.i("KeyboardUI","keyboard exists!!!");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }



    @Override
    protected void onStop() {
        Log.i("KeyboardUI","activity destroy!!!!!!");
        super.onStop();
        if(keyboard!=null){
            // keyboard.release();
            keyboard.release();
            keyboard=null;

        }
        if(detector!=null){
            detector.release();
            detector = null;
        }
    }

    public void updateSignal(){
        int w = 0;
        int g = 0;
        if(wifi.getText().length()>0) w =  Integer.parseInt(wifi.getText().toString());
        if(gprs.getText().length()>0) g = Integer.parseInt(gprs.getText().toString());

        if(keyboard!=null && !keyboard.isReleased()) keyboard.updateSign(w,g);
    }

    private void logMessage(final String message){
        handler.post(new Runnable() {
            @Override
            public void run() {
                eventLog.setMovementMethod(ScrollingMovementMethod.getInstance());
                eventLog.setSelection(eventLog.getText().length(), eventLog.getText().length());
                eventLog.getText().append(message);
            }
        });
    }

    @Override
    public void onAttach() {
        openKeyboard();
    }
}
