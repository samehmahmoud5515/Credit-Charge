package io.jrogers.android.wampexample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends ActionBarActivity {

EditText phoneEditText;
Button login_button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        phoneEditText =(EditText) findViewById(R.id.editText_phoneNumber);
        login_button =(Button) findViewById(R.id.button_Login);

        startService(new Intent(MainActivity.this, USSDService.class));

       /* if (phoneEditText.getText().toString().equals(""))
            phoneEditText.setError("Must enter Phone Numeber");
        else {
            String phone = phoneEditText.getText().toString();


            // phoneEditText.findViewById('');


       /* Intent intent = new Intent(this,USSD.class);
        intent.putExtra("phone","");
        startService(intent);*/

        }





    }









