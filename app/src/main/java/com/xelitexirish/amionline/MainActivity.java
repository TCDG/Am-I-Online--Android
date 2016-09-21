package com.xelitexirish.amionline;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;

import static com.xelitexirish.amionline.R.string.title_connected;
import static com.xelitexirish.amionline.R.string.title_disconnected;

public class MainActivity extends AppCompatActivity {

    private FirebaseAnalytics mFirebaseAnalytics;

    private ImageView mImageViewStatus;
    private TextView mTextViewStatus;
    private Switch mSwitchAutoUpdater;
    private ImageView mImageViewRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.mImageViewStatus = (ImageView) findViewById(R.id.imageViewStatus);
        this.mTextViewStatus = (TextView) findViewById(R.id.textViewStatus);
        this.mSwitchAutoUpdater = (Switch) findViewById(R.id.switchAutoUpdater);
        this.mImageViewRefresh = (ImageView) findViewById(R.id.imageViewRefresh);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        mSwitchAutoUpdater.setChecked(ServiceCheckConnection.isNotificationRunning());
        mSwitchAutoUpdater.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if(checked){
                    // Stopped
                    Toast.makeText(MainActivity.this, R.string.action_starting, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, ServiceCheckConnection.class);
                    intent.putExtra(ServiceCheckConnection.TAG_PREVIOUS_STATE, hasSimpleNetworkConnection(MainActivity.this));
                    startService(intent);
                }else {
                    // Running
                    Toast.makeText(MainActivity.this, R.string.action_stopping, Toast.LENGTH_SHORT).show();
                    stopService(new Intent(MainActivity.this, ServiceCheckConnection.class));
                }
            }
        });
        mSwitchAutoUpdater.setVisibility(View.GONE);
        
        mImageViewRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkInternetConnection(MainActivity.this);
            }
        });
        
        checkInternetConnection(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkInternetConnection(this);
    }

    private void checkInternetConnection(Context context){
        Toast.makeText(context, R.string.action_refreshing, Toast.LENGTH_SHORT).show();
        if (hasSimpleNetworkConnection(context)){
            showInternet();
        }else {
            showNoInternet();
        }
    }

    public static boolean hasSimpleNetworkConnection(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void showNoInternet(){
        mImageViewStatus.setImageDrawable(getDrawable(R.drawable.ic_disconnected));
        mTextViewStatus.setText(getString(title_disconnected));
    }

    public void showInternet(){
        mImageViewStatus.setImageDrawable(getDrawable(R.drawable.ic_connected));
        mTextViewStatus.setText(getString(title_connected));
    }
}
