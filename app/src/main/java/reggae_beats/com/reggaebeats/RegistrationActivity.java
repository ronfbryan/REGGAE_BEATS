package reggae_beats.com.reggaebeats;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegistrationActivity extends AppCompatActivity {

    EditText NewUserName, NewUserPassword, NewUserEmail, NewUserPhone, Answer1_EditText1, Answer2_EditText2;
    Button RegisterButton;
    RadioGroup NotificationGroup;

    String clicked = "";
    private Pattern pattern;
    private Matcher matcher;
    private int selectedItem_spinner1;
    private int selectedItem_spinner2;
    private String NewUserPasswordValue;
    ;
    SharedPreferences SavedSharedPreferences;
    SharedPreferences.Editor editor;
    String QuestionArray[];
    private Spinner Spinner1, Spinner2;
    private String NewUserNameValue;

    private String currentDateAndTime;
    private String CurrentToken = null;
    private String urlRegistration;
    private String Answer1;
    private String Answer2;

    private String query;
    Handler handler;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_registration);
        progressBar = (ProgressBar) findViewById(R.id.progresBarId);

        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);


        }
        handler = new Handler(Looper.getMainLooper());


        //Initialise the UI Components


        NewUserName = (EditText) findViewById(R.id.NewUserNameId);
        NewUserPassword = (EditText) findViewById(R.id.NewUserPasswordId);

        RegisterButton = (Button) findViewById(R.id.RegisterButtonId);


        RegisterButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {


                NewUserNameValue = NewUserName.getText().toString();


                //Ensure that password has numbers ,lower case ,upper case and  atleast one symbol
                NewUserPasswordValue = NewUserPassword.getText().toString();

                if (!(NewUserNameValue).isEmpty() && !(NewUserPasswordValue).isEmpty()) {


                    String password_pattern = "(^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).{6,}$)";
                    pattern = Pattern.compile(password_pattern);

                    matcher = pattern.matcher(NewUserPasswordValue);
                    if (matcher.matches()) {

                        RegistrationNetworkCall RegNetworkCall = new RegistrationNetworkCall();
                        RegNetworkCall.execute(NewUserPasswordValue);


                    } else {


                        Toast.makeText(getApplicationContext(), R.string.PASSWORD_VALIDATION_INSTRUCTION,
                                Toast.LENGTH_LONG).show();


                    }

                } else {

                    Toast.makeText(getApplicationContext(), R.string.RELEVANT_FIELDS,
                            Toast.LENGTH_LONG).show();
                }


            }


        });
    }


    private class RegistrationNetworkCall extends AsyncTask<String, Void, String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //Show progressBar
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);


            }

            // get values entered by user

            NewUserNameValue = NewUserName.getText().toString();


            NewUserPasswordValue = NewUserPassword.getText().toString();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Calendar calender = Calendar.getInstance();
            currentDateAndTime = sdf.format(calender.getTime());
            Date date = new Date();

            calender.setTime(date);
            calender.add(Calendar.HOUR, 24 * 30);

            Toast.makeText(getBaseContext(), "Time is " + currentDateAndTime, Toast.LENGTH_LONG).show();


        }

        @Override
        protected String doInBackground(String... params) {


            urlRegistration = "http://jpsbillfinder.com/jam_player/registration.php";
            Uri.Builder builder = new Uri.Builder().appendQueryParameter("NewUserName", NewUserNameValue)
                    .appendQueryParameter("NewUserPassword", NewUserPasswordValue);


            query = builder.build().getEncodedQuery();

            String response = null;
            try {
                //Check for internet connectivity
                if (new Configuration(RegistrationActivity.this).IsNetworkAvailable()) {
                    response = new NetWorkConnection().ServerConnectionResults(query, urlRegistration);
                }
            } catch (MalformedURLException e) {

                e.printStackTrace();

            }
            return response;
        }

        protected void onPostExecute(String response) {
            String success;
            String message;

            String registrationCode;
            // Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();

            try {
                JSONObject jsonObject = new JSONObject(response);
                success = jsonObject.getString("SUCCESS");
                message = jsonObject.getString("MESSAGE");
                registrationCode = jsonObject.getString("REGISTRATION_CODE");
                if ((success).equals("SUCCESS")) {

                    // Communicate to user on succesful login
                    Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();
                    Log.d("REGGAE_BEATS", response);
                    Intent intent = new
                            Intent(RegistrationActivity.this, MainActivity.class);
                    //Make progressbar invisible before starting new activity
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);


                    }
                    new SharedPreferenceConfig(RegistrationActivity.this).addToSharedPreferences("USERNAME", NewUserNameValue);
                    new SharedPreferenceConfig(RegistrationActivity.this).addToSharedPreferences("REGISTRATION_CODE", registrationCode);
                    startActivity(intent);

                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.SUCCESSFUL_LOGIN), Toast.LENGTH_LONG).show();


                } else {

                    Toast.makeText(getApplicationContext(), "You were not registered.This may have been due to a network error .Please wait for a moment then try again ", Toast.LENGTH_LONG).show();


                }


            } catch (JSONException e) {
                e.printStackTrace();
            }


            super.onPostExecute(response);
        }

    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        new Configuration(this).IsNetworkAvailable();

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }


}



