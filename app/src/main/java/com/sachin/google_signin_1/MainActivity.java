package com.sachin.google_signin_1;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.support.v4.app.FragmentActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;

public class MainActivity extends FragmentActivity implements ConnectionCallbacks,
        OnConnectionFailedListener, View.OnClickListener{

    //Fb login variable
/*
    LoginButton loginButton;
    TextView textView;
    CallbackManager callbackManager;
*/

    //Google Plus variable
    private  static final int SIGNED_IN=0;
    private  static final int STATE_SIGNING_IN=1;
    private  static final int STATE_IN_PROGRESS=2;
    private  static final int RC_SIGN_IN =0;

    private GoogleApiClient mGoogleApiClient;
    private int mSignInProgress;
    private PendingIntent mSignInIntent;

    private SignInButton mSignInButton;
    private Button mSignOutButton;
    private Button mRevokeButton;
    private TextView mStatus;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //FaceBook SDK initialize
       // FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        // Get references to all the UI Views
        mSignInButton = (SignInButton) findViewById(R.id.sign_in_button);
        mSignOutButton = (Button) findViewById(R.id.sign_out_button);
        mRevokeButton = (Button) findViewById(R.id.revoke_access_button);
        mStatus = (TextView) findViewById(R.id.statuslabel);

        // Add Click Listeners for the Buttons
        mSignInButton.setOnClickListener(this);
        mSignOutButton.setOnClickListener(this);
        mRevokeButton.setOnClickListener(this);

        // Build a GoogleApiClient
        mGoogleApiClient = buildGoogleApiClient();

        /*loginButton = (LoginButton) findViewById(R.id.fb_login_bt);
        textView = (TextView) findViewById(R.id.textView);
        callbackManager = CallbackManager.Factory.create();
*/
       /* loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                textView.setText("Login Success \n" +
                loginResult.getAccessToken().getUserId()+
                "\n" + loginResult.getAccessToken().getToken());

            }

            @Override
            public void onCancel() {
                textView.setText("Login Cancelled");

            }

            @Override
            public void onError(FacebookException error) {

            }
        });*/
    }

    private GoogleApiClient buildGoogleApiClient() {
        return new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API, Plus.PlusOptions.builder().build())
                .addScope(new Scope("email"))
                .build();
    }

    @Override
    protected void onStart(){
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop(){
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    //Add the onConnectionSuspended() method:
    @Override
    public void onConnectionSuspended(int cause){
        mGoogleApiClient.connect();
    }

    // Handle Connection to Google Play services.
    @Override
    public void onConnected(Bundle connecctionHint){
        mSignInButton.setEnabled(false);
        mSignOutButton.setEnabled(true);
        mRevokeButton.setEnabled(true);

        // Indicate that the sign in process is complete.
        mSignInProgress = SIGNED_IN;

        try {
            String emailAddress = Plus.AccountApi.getAccountName(mGoogleApiClient);
            mStatus.setText(String.format("Singned In to My App as %s", emailAddress));
        }
        catch (Exception ex){
            String exception = ex.getLocalizedMessage();
            String exceptionString = ex.toString();
        }
    }

    // Handle Connection Failures
    @Override
    public void onConnectionFailed(ConnectionResult result){
        if(mSignInProgress != STATE_IN_PROGRESS){
            mSignInIntent = result.getResolution();
            if(mSignInProgress == STATE_SIGNING_IN){
                resolvesSignInError();
            }
        }
        onSignedOut();
    }

    //Implement onSignedOut()

     private void onSignedOut() {
        // Update the UI to reflect that the user is signed out
        mSignInButton.setEnabled(true);
        mSignOutButton.setEnabled(false);
        mRevokeButton.setEnabled(false);

        mStatus.setText("Signed Out");
    }

    private void resolvesSignInError() {
        if(mSignInIntent != null){
            try {
                mSignInProgress = STATE_IN_PROGRESS;
                startIntentSenderForResult(mSignInIntent.getIntentSender(),
                        RC_SIGN_IN, null,0,0,0);
            } catch (IntentSender.SendIntentException e) {
                mSignInProgress = STATE_SIGNING_IN;
                mGoogleApiClient.connect();
            }
        }else {
            // you have a play service error --inform the user
        }
    }

    //The user’s response with the intents can be handled in onActivityResult()
    @Override
    protected  void onActivityResult(int requestCode, int resultCode, Intent data){
        switch (requestCode){
            case RC_SIGN_IN:
                if(resultCode == RESULT_OK){
                    mSignInProgress = STATE_SIGNING_IN;
                }else {
                    mSignInProgress = SIGNED_IN;
                }
                if (!mGoogleApiClient.isConnecting()){
                    mGoogleApiClient.connect();
                }
                break;
        }
    }

    //Implement the onClick handlers

    @Override
    public void onClick(View v){
        if(!mGoogleApiClient.isConnecting()){
            switch (v.getId()){
                case R.id.sign_in_button:
                    mStatus.setText("Siging In");
                    resolvesSignInError();
                    break;
                case R.id.sign_out_button:
                    Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
                    Plus.AccountApi.revokeAccessAndDisconnect(mGoogleApiClient);
                    mGoogleApiClient = buildGoogleApiClient();
                    mGoogleApiClient.connect();
                    break;
            }
        }
    }

    
   /* protected void onActvityResult(int requestCode, int resultCode, Intent data){
        callbackManager.onActivityResult(requestCode,resultCode,data);
    }*/
}
