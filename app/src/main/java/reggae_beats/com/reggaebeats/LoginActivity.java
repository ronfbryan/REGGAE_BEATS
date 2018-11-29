package reggae_beats.com.reggaebeats;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.net.MalformedURLException;
import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity {
    private ArrayList<String> ListPackagedResults = null;
    private EditText LoginAccountNumber, LoginPassword;
    private String LoginAccountValue;
    private String LoginPasswordValue;
    private Spinner login_spinner;
    private String spinnerValue;
    private String selectedItem;
    String[] spinnerData = new String[]{"Doctor", "Receptionist", "Patient"};
    private ProgressBar progressBar;
    private String retrievedTokenSharedPrefs;
    private String newToken;
    private String activeUser;
    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //Check if token has changed
        checkToken();
        //Initialise progess bar
        progressBar = (ProgressBar) findViewById(R.id.progresBarId);
        // Initialize and add items to spinner


        Button LoginButton = (Button) findViewById(R.id.Login);
        Button RegisterButton = (Button) findViewById(R.id.RegisterId);
        LoginAccountNumber = (EditText) findViewById(R.id.editTextAccountLoginId);
        LoginPassword = (EditText) findViewById(R.id.editTextLoginPasswordId);


        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Toast.makeText(LoginActivity.this, "Got a notification1", Toast.LENGTH_LONG).show();
                String title_in_reciever = intent.getExtras().getString("TITLE");

                String body_in_receiver = intent.getExtras().getString("BODY");
                Toast.makeText(LoginActivity.this, "TITLE" + "" + title_in_reciever + " " + "BODY" + body_in_receiver, Toast.LENGTH_LONG).show();

                intent.setClassName("reggae_beats.com.reggaebeats", "reggae_beats.com.reggaebeats.FriendRequestActivity");
                startActivity(intent);

            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("reggae_beats.com.reggaebeats");

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);
//Get message from intenet object passed over with main launcher
        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                if (key.equals("TITLE")) {
                    String title = getIntent().getExtras().getString(key);

                }
                if (key.equals("BODY")) {
                    String Body = getIntent().getExtras().getString(key);

                }
            }
        }

        /*--------------------------------------------LISTENERS -------------------------------------------------------------------*/
        LoginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                //Check for network connectivity before making a net work call
                checkNetwork();
                checkToken();

            }


        });


        RegisterButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (new Configuration(LoginActivity.this).IsNetworkAvailable()) {

                    Intent intent2 = new Intent(LoginActivity.this,
                            RegistrationActivity.class);
                    startActivity(intent2);

                }
            }


        });
    }

    private boolean checkToken() {

        //temporarily remove shared preference value
        //new SharedPreferenceConfig(this).deleteValue("REFRESHED_TOKEN");

        Thread myThread = new Thread(new Runnable() {

            @Override
            public void run() {

                Looper.prepare();
                Handler handler = new Handler(Looper.getMainLooper());
                //check
                FirebaseInstanceId.getInstance().getInstanceId()
                        .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                            @Override
                            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                if (!task.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(), "Token request failed", Toast.LENGTH_LONG).show();

                                } else {

                                    newToken = task.getResult().getToken();
                                }
                                //.makeText(getApplicationContext(), "Token succesfully retrieved" + newToken, Toast.LENGTH_LONG).show();
                                checkForEqualTokenCondition(retrievedTokenSharedPrefs, newToken);

                            }
                        });


            }


        });
        myThread.start();

        return true;
    }

    private boolean checkForEqualTokenCondition(String retrievedTokenSharedPrefs, String newToken) {
        //Toast.makeText(getApplicationContext(), "check for equal token called" + newToken, Toast.LENGTH_LONG).show();
        if ((retrievedTokenSharedPrefs) == (newToken)) {

            return true;
        } else {

            // Toast.makeText(getApplicationContext(), "not equals called" , Toast.LENGTH_LONG).show();
            TokenAsyncTask tokenAsyncTask = new TokenAsyncTask();
            tokenAsyncTask.execute();

        }


        return false;
    }


    private class TokenAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            retrievedTokenSharedPrefs = new SharedPreferenceConfig(getApplicationContext()).findValue("REFRESHED_TOKEN");
            // Toast.makeText(getApplicationContext(), "RETRIEVED TOKEN  before" + retrievedTokenSharedPrefs, Toast.LENGTH_SHORT).show();
            activeUser = new SharedPreferenceConfig(getApplicationContext()).findValue("USERNAME");
        }

        @Override
        protected String doInBackground(String... strings) {
            String uploadTokenUrl = "http://jpsbillfinder.com/jam_player/upload_jam_player_token.php";

            String response = null;
            Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("TOKEN", newToken)
                    .appendQueryParameter("ACTIVE_USER", activeUser);
            String query = builder.build().getEncodedQuery();
            try

            {
                response = new NetWorkConnection().ServerConnectionResults(query, uploadTokenUrl);

            } catch (
                    MalformedURLException e)

            {
                e.printStackTrace();
            }


            return response;


        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //Toast.makeText(getApplicationContext(), "straight response " + s, Toast.LENGTH_SHORT).show();

            if (s == "SUCCESS")

            {
                new SharedPreferenceConfig(getApplicationContext()).addToSharedPreferences("REFRESHED_TOKEN", newToken);


                // Toast.makeText(getApplicationContext(), "RETRIEVED TOKEN after " + retrievedTokenSharedPrefs, Toast.LENGTH_SHORT).show();
                Toast.makeText(getApplicationContext(), "New token sent to server in main activity", Toast.LENGTH_SHORT).show();
            }


        }
    }


    private void checkNetwork() {
        if (new Configuration(this).IsNetworkAvailable()) {
            LoginAccountValue = LoginAccountNumber.getText().toString();
            LoginPasswordValue = LoginPassword.getText().toString();
            NetworkCall NwCall = new NetworkCall();
            NwCall.execute(LoginAccountValue, LoginPasswordValue);

        }
    }



    /*-------------------------------ASYNCTASK TASK FOR LOGIN ACTIVITY----------------------------------------------------*/

    /**
     * Get and send login values to server
     **/
    private class NetworkCall extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if (new Configuration(LoginActivity.this).IsNetworkAvailable()) {
                progressBar.setVisibility(View.VISIBLE);
            }


        }

        @Override
        protected String doInBackground(String... params) {
            //Using Uri to package the password and username values to be sent to server for aut

            String urlLoginConnect = "http://jpsbillfinder.com/jam_player/jam_player_login.php";
            Uri.Builder builder = new Uri.Builder().appendQueryParameter(
                    "LoginAccount", LoginAccountValue).appendQueryParameter(
                    "LoginPassword", LoginPasswordValue);


            String query = builder.build().getEncodedQuery();
            String response = null;
            try {
                if (new Configuration(LoginActivity.this).IsNetworkAvailable()) {

                    response = new NetWorkConnection().ServerConnectionResults(
                            query, urlLoginConnect);

                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return response;

        }

        @Override
        protected void onPostExecute(String response) {

            if (response == null) {
                new Configuration(LoginActivity.this).IsNetworkAvailable();

            } else {
                //Parse the json recieved from server
                parseResponse(response);
                progressBar.setVisibility(View.GONE);


            }
        }

    }

    /*-----------------------------------HELPER TO PARSE JSON VALUES RETURNED BY SERVER ---------------------------------------------------------------------------------------*/

    /*Json results are parsed after successful login
     *
     */

    /****Values sent to be be displayed in Items AccountActivity****/


    private void parseResponse(String response) {

        String TAG = "success";


        if (response.equals(TAG)) {


            Intent intent = new Intent(LoginActivity.this, MainActivity.class);

            progressBar.setVisibility(View.GONE);
            //Launch different fragments depending on who has logged in


            new SharedPreferenceConfig(getApplicationContext()).addToSharedPreferences("USERNAME", LoginAccountValue);
            new SharedPreferenceConfig(getApplicationContext()).addToSharedPreferences("PASSWORD", LoginPasswordValue);
            startActivity(intent);

        } else {

            Toast.makeText(getApplicationContext(), "Ensure you have entered the correct username or password or please register", Toast.LENGTH_LONG).show();

        }
    }




    /*------------------------ONSAVED INSTANCESTATE BUNDLE-------------------------------------------------------------------------*/
    /*Bundle which stores values values in event of shocks like configuration changes*/
    /*Bundle restored on onRestore method calls***/

    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);

        outState.putString("LoginAccountValue", LoginAccountValue);
        outState.putString("LoginPasswordValue", LoginPasswordValue);
        outState.putStringArrayList("ARRAY_LIST_PACKAGE", ListPackagedResults);


    }
    /*------------------------------------ONRESTOREINSTANCESTATE--------------------------------------------------------------------*/

    /*Bundle data which was previously saved is restored
     *  on this method call
     * @param savedInstanceState
     * @param persistentState
     */
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onRestoreInstanceState(savedInstanceState, persistentState);

        savedInstanceState.get("LoginAccountValue");
        savedInstanceState.get("LoginPasswordValue");

        //set username and password edit text with the values recovered from the instance state bundles
        String recoveredUserLogin = (String) savedInstanceState.get("LoginAccountValue");
        LoginAccountNumber.setText(recoveredUserLogin);

        String recoveredUserPassword = (String) savedInstanceState.get("LoginPasswordValue");
        LoginPassword.setText(recoveredUserPassword);


    }


    @Override
    protected void onResume() {
        super.onResume();
        new Configuration(this).IsNetworkAvailable();


    }


    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        finish();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

        }
        return super.onOptionsItemSelected(item);
    }
}
