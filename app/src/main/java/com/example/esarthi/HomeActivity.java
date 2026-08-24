package com.example.esarthi;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Dns;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "GROQ_API";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");

    private RecyclerView rvChat;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList;
    private EditText etMessage;
    private ImageButton btnSend;

    private OkHttpClient httpClient;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide Status Bar (Modern & Safe way)
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        controller.hide(WindowInsetsCompat.Type.statusBars());
        controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);

        setContentView(R.layout.activity_home);

        initHttpClient();

        rvChat = findViewById(R.id.rvChat);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);

        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvChat.setLayoutManager(layoutManager);
        rvChat.setAdapter(chatAdapter);

        // Scroll to bottom when keyboard opens
        rvChat.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (bottom < oldBottom) {
                rvChat.postDelayed(() -> {
                    if (messageList.size() > 0) {
                        rvChat.smoothScrollToPosition(messageList.size() - 1);
                    }
                }, 100);
            }
        });

        btnSend.setOnClickListener(v -> {
            String query = etMessage.getText().toString().trim();
            if (!query.isEmpty()) {
                sendMessage(query);
            }
        });
    }

    private void initHttpClient() {
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .dns(new Dns() {
                    @Override
                    public List<InetAddress> lookup(String hostname) throws UnknownHostException {
                        try {
                            return Dns.SYSTEM.lookup(hostname);
                        } catch (UnknownHostException e) {
                            if ("api.groq.com".equalsIgnoreCase(hostname)) {
                                Log.w(TAG, "System DNS lookup failed for " + hostname + ". Using fallback IPs.");
                                return Arrays.asList(
                                        InetAddress.getByAddress(hostname, new byte[]{(byte) 104, (byte) 18, (byte) 38, (byte) 236}),
                                        InetAddress.getByAddress(hostname, new byte[]{(byte) 172, (byte) 64, (byte) 149, (byte) 20})
                                );
                            }
                            throw e;
                        }
                    }
                })
                .build();
    }

    private void sendMessage(String userMessage) {
        messageList.add(new ChatMessage(userMessage, "", ChatMessage.TYPE_USER));
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        rvChat.scrollToPosition(messageList.size() - 1);

        etMessage.setText("");

        // Add "Thinking..." placeholder
        messageList.add(new ChatMessage("Thinking...", "", ChatMessage.TYPE_AI));
        int loadingIndex = messageList.size() - 1;
        chatAdapter.notifyItemInserted(loadingIndex);
        rvChat.scrollToPosition(loadingIndex);

        try {
            JSONObject jsonParams = new JSONObject();
            jsonParams.put("model", AiConfig.MODEL_NAME);
            jsonParams.put("temperature", 0.7);
            jsonParams.put("max_tokens", 2048);
            jsonParams.put("top_p", 1);
            jsonParams.put("stream", false);

            JSONArray messagesArr = new JSONArray();

            // Add system instructions
            if (AiConfig.SYSTEM_PROMPT != null && !AiConfig.SYSTEM_PROMPT.isEmpty()) {
                JSONObject systemObj = new JSONObject();
                systemObj.put("role", "system");
                systemObj.put("content", AiConfig.SYSTEM_PROMPT);
                messagesArr.put(systemObj);
            }

            // Add user message
            JSONObject userObj = new JSONObject();
            userObj.put("role", "user");
            userObj.put("content", userMessage);
            messagesArr.put(userObj);

            jsonParams.put("messages", messagesArr);

            Log.d(TAG, "Sending request to " + AiConfig.GROQ_URL + " with model: " + AiConfig.MODEL_NAME);

            RequestBody requestBody = RequestBody.create(jsonParams.toString(), JSON_MEDIA_TYPE);
            Request request = new Request.Builder()
                    .url(AiConfig.GROQ_URL)
                    .addHeader("Authorization", "Bearer " + AiConfig.API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .post(requestBody)
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Request onFailure: " + e.getMessage(), e);
                    mainHandler.post(() -> updateChat(loadingIndex, AiConfig.ERROR_SERVICE_UNAVAILABLE));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseString = response.body() != null ? response.body().string() : "";
                    Log.d(TAG, "Response Code: " + response.code() + ", Body: " + responseString);

                    if (response.isSuccessful() && !responseString.isEmpty()) {
                        try {
                            JSONObject responseJson = new JSONObject(responseString);
                            String aiResponse = responseJson.getJSONArray("choices")
                                    .getJSONObject(0)
                                    .getJSONObject("message")
                                    .getString("content");

                            // Strip internal reasoning/think tags if present
                            if (aiResponse != null && aiResponse.contains("</think>")) {
                                aiResponse = aiResponse.substring(aiResponse.lastIndexOf("</think>") + 8).trim();
                            }

                            if (aiResponse == null || aiResponse.trim().isEmpty()) {
                                aiResponse = AiConfig.ERROR_SERVICE_UNAVAILABLE;
                            }

                            final String finalAiResponse = aiResponse;
                            mainHandler.post(() -> updateChat(loadingIndex, finalAiResponse));
                        } catch (Exception e) {
                            Log.e(TAG, "JSON Parse Error: " + e.getMessage(), e);
                            mainHandler.post(() -> updateChat(loadingIndex, AiConfig.ERROR_SERVICE_UNAVAILABLE));
                        }
                    } else {
                        Log.e(TAG, "API Error (Status " + response.code() + "): " + responseString);
                        mainHandler.post(() -> updateChat(loadingIndex, AiConfig.ERROR_SERVICE_UNAVAILABLE));
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Exception preparing request: " + e.getMessage(), e);
            updateChat(loadingIndex, AiConfig.ERROR_SERVICE_UNAVAILABLE);
        }
    }

    private void updateChat(int loadingIndex, String aiResponse) {
        // Remove "Thinking..." placeholder
        if (loadingIndex < messageList.size()) {
            messageList.remove(loadingIndex);
            chatAdapter.notifyItemRemoved(loadingIndex);
        }

        if (aiResponse != null && !aiResponse.isEmpty()) {
            messageList.add(new ChatMessage(aiResponse, "", ChatMessage.TYPE_AI));
            chatAdapter.notifyItemInserted(messageList.size() - 1);
            rvChat.scrollToPosition(messageList.size() - 1);
        } else {
            Toast.makeText(HomeActivity.this, "AI response empty.", Toast.LENGTH_SHORT).show();
        }
    }
}
