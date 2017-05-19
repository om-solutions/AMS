package com.attendance.om.myapplication;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Dashboard2 extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    private Button b_get;
    private Button b_get2;
    private TrackGPS gps;
    double longitude;
    double latitude;
    private TextView textView;
    private String uName;
    private TextView textView1;
    private TextView textView22;
    private View mProgressView;
    private View mDashFormView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle bundle = getIntent().getExtras();
         uName= bundle.getString("uName");

        b_get = (Button) findViewById(R.id.btnCheckIn);
        b_get2 = (Button) findViewById(R.id.btnCheckOut);
        textView =(TextView) findViewById(R.id.textView);
        textView1 =(TextView) findViewById(R.id.textView1);
        textView22 =(TextView) findViewById(R.id.textView22);
        textView1.setText(uName);

        gps = new TrackGPS(Dashboard2.this);
        if(gps.canGetLocation()){
            longitude = gps.getLongitude();
            latitude = gps .getLatitude();
            textView.setText("Longitude:"+Double.toString(longitude)+"\nLatitude:"+Double.toString(latitude));
            Toast.makeText(getApplicationContext(),"Longitude:"+Double.toString(longitude)+"\nLatitude:"+Double.toString(latitude),Toast.LENGTH_SHORT).show();
        }
        else
        {   gps.showSettingsAlert();
        }


            b_get.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    showProgress(true);
                    UserAttTask userAttTask = null;
                    userAttTask = new UserAttTask(uName,"Check-In" ,Double.toString(longitude),Double.toString(latitude));
                if(Build.VERSION.SDK_INT >= 11)
                    userAttTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                else
                    userAttTask.execute();



            }
        });


        b_get2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    showProgress(true);
                    UserAttTask userAttTask = null;
                    userAttTask = new UserAttTask(uName,"Check-Out" ,Double.toString(longitude),Double.toString(latitude));
                if(Build.VERSION.SDK_INT >= 11)
                    userAttTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                else
                    userAttTask.execute();

//                    userAttTask.execute((Void) null);


            }
        });

        mDashFormView = findViewById(R.id.fullscreen_content);
        mProgressView = findViewById(R.id.dash_progress);
    }


    public class UserAttTask extends AsyncTask<String, Void, String> {

        private final String mEmail;
        private final String aType;
        private final String aLatitude;
        private final String aLongitude;


        UserAttTask(String email, String aType, String aLocation,String aLocation2) {
            mEmail = email;
            this.aType = aType;
            this.aLatitude = aLocation;
            this.aLongitude = aLocation2;
        }

        @Override
        protected String doInBackground(String... params) {
            // TODO: attempt authentication against a network service.
            Log.i("In ", "doInBackground");
            String text = "";
            try {

                Log.i("In ", "doInBackground11111");
                HttpClient httpClient = new DefaultHttpClient();
                HttpContext localContext = new BasicHttpContext();
                HttpGet httpGet = new HttpGet("http://localhost:8080/attendanceNew/webapi/user/attendance?username="+mEmail+"&aType="+aType+"&aLocation="+aLatitude+""+aLongitude);

                try {
                    HttpResponse response = httpClient.execute(httpGet, localContext);

                    HttpEntity entity = response.getEntity();


                    text = getASCIIContentFromEntity(entity);
                    Log.i("In ", "doInBackground11111 : "+text );

                    //Toast.makeText(FullscreenActivity.this, ""+text.toString(),Toast.LENGTH_LONG).show();
                    if (text.equalsIgnoreCase("true"))

                        return  text;

                } catch (Exception e) {
                    return "Exception ";
                }

                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return "Exception";
            }


            // TODO: register the new account here.
            return text;
        }


        protected String getASCIIContentFromEntity(HttpEntity entity) throws IllegalStateException, IOException {
            InputStream in = entity.getContent();


            StringBuffer out = new StringBuffer();
            int n = 1;
            while (n>0) {
                byte[] b = new byte[4096];
                n = in.read(b);

                if (n>0) out.append(new String(b, 0, n));
            }


            return out.toString();
        }

        protected void onPostExecute(final Boolean success) {
            showProgress(false);
            Toast.makeText(Dashboard2.this,"Done Sucessfully !!!", Toast.LENGTH_LONG);
        }



        @Override
        protected void onCancelled() {

            showProgress(false);
        }


    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mDashFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mDashFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mDashFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mDashFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), Dashboard2.ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
           // emails.add(cursor.getString(Dashboard.ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.

    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }


}
