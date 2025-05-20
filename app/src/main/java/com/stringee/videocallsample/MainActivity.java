package com.stringee.videocallsample;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult;
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleObserver;

import com.stringee.StringeeClient;
import com.stringee.call.StringeeCall;
import com.stringee.call.StringeeCall2;
import com.stringee.exception.StringeeError;
import com.stringee.listener.StatusListener;
import com.stringee.listener.StringeeConnectionListener;

import org.json.JSONObject;
@SuppressLint("SetTextI18n")
public class MainActivity extends AppCompatActivity implements LifecycleObserver {
    private StringeeClient client;

    private String to;

    private TextView tvUserId;

    private ActivityResultLauncher<Intent> launcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvUserId = findViewById(R.id.tv_userid);
        EditText etTo = findViewById(R.id.et_to);

        Button btnMakeCall = findViewById(R.id.btn_make_call);
        btnMakeCall.setOnClickListener(view -> {
            // gọi từ kiosk001 → staff001
            Intent intent = new Intent(MainActivity.this, OutgoingCallActivity.class);
            String from = client != null ? client.getUserId() : "unknown";
            intent.putExtra("from", from);
            Log.d("DEBUG_CALL", "From: " + from);

            intent.putExtra("to", "staff001");
            launcher.launch(intent);
        });


        ProgressBar progressBar = findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);


        // register data call back
        launcher = registerForActivityResult(new StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == AppCompatActivity.RESULT_CANCELED)
                        if (result.getData() != null) {
                            if (result.getData().getAction() != null && result.getData().getAction().equals("open_app_setting")) {
                                Builder builder = new Builder(this);
                                builder.setTitle(R.string.app_name);
                                builder.setMessage("Permissions must be granted for the call");
                                builder.setPositiveButton("Ok", (dialogInterface, id) -> dialogInterface.cancel());
                                builder.setNegativeButton("Settings", (dialogInterface, id) -> {
                                    dialogInterface.cancel();
                                    // open app setting
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                                    intent.setData(uri);
                                    startActivity(intent);
                                });
                                builder.create().show();
                            }
                        }
                });

        initAndConnectStringee();
    }

    public void initAndConnectStringee() {
        if (client == null) {
            client = new StringeeClient(getApplicationContext());
            ((MyApp) getApplication()).setClient(client);  
            client.setConnectionListener(new StringeeConnectionListener() {
                @Override
                public void onConnectionConnected(final StringeeClient stringeeClient, boolean isReconnecting) {
                    runOnUiThread(() -> {
                        ProgressBar progressBar = findViewById(R.id.progress_bar);
                        progressBar.setVisibility(View.GONE);

                        tvUserId.setText("Connected as: " + stringeeClient.getUserId());
                        Log.d("DEBUG_CONNECT", "✅ Connected as: " + stringeeClient.getUserId());
                    });
                }

                @Override
                public void onConnectionDisconnected(StringeeClient stringeeClient, boolean isReconnecting) {
                    runOnUiThread(() -> {
                        ProgressBar progressBar = findViewById(R.id.progress_bar);
                        progressBar.setVisibility(View.GONE);

                        tvUserId.setText("Disconnected");
                    });
                }

                @Override
                public void onIncomingCall(final StringeeCall stringeeCall) {
                }

                @Override
                public void onIncomingCall2(StringeeCall2 stringeeCall2) {
                    Log.d("DEBUG_INCOMING", "📥 Incoming call from: " + stringeeCall2.getFrom() + " → to: " + stringeeCall2.getTo());
                    Log.d("DEBUG_INCOMING", "isInCall = " + Common.isInCall);

                    runOnUiThread(() -> {
                        if (Common.isInCall) {
                            stringeeCall2.reject(new StatusListener() {
                                @Override
                                public void onSuccess() {

                                }
                            });
                        } else {
                            Common.callMap.put(stringeeCall2.getCallId(), stringeeCall2);
                            Log.d("DEBUG_CALLMAP", "Put callId into map: " + stringeeCall2.getCallId());

                            Intent intent = new Intent(MainActivity.this, IncomingCallActivity.class);
                            intent.putExtra("call_id", stringeeCall2.getCallId());
                            startActivity(intent);
                        }
                    });
                }

                @Override
                public void onConnectionError(StringeeClient stringeeClient, final StringeeError stringeeError) {
                    runOnUiThread(() -> {
                        ProgressBar progressBar = findViewById(R.id.progress_bar);
                        progressBar.setVisibility(View.GONE);

                        tvUserId.setText("Connect error: " + stringeeError.getMessage());
                    });
                }

                @Override
                public void onRequestNewToken(StringeeClient stringeeClient) {
                    // Get new token here and connect to Stringee server
                    Log.d("Stringee", "Token expired. Request new token here");
                }

                @Override
                public void onCustomMessage(String from, JSONObject msg) {
                }

                @Override
                public void onTopicMessage(String from, JSONObject msg) {

                }
            });
        }
        //token
        String token = "eyJjdHkiOiJzdHJpbmdlZS1hcGk7dj0xIiwidHlwIjoiSldUIiwiYWxnIjoiSFMyNTYifQ.eyJqdGkiOiJTSy4wLlo0Q3dJZmJrdk1SMktnNERsb0ZpQnNsQ0ZpT09Sc0tjLTE3NDcxODYzNzMiLCJpc3MiOiJTSy4wLlo0Q3dJZmJrdk1SMktnNERsb0ZpQnNsQ0ZpT09Sc0tjIiwiZXhwIjoxNzQ5Nzc4MzczLCJ1c2VySWQiOiJraW9zazAwMSIsImljY19hcGkiOnRydWV9.NZEwnFJxLW5JNupg6kedUA-6UNsSf0lkUVAhtBms7Cs";
        client.connect(token);
    }
}
