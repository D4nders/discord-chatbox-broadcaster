package com.discordchatboxbroadcaster.notifier;

import com.discordchatboxbroadcaster.DiscordChatboxBroadcasterConfig;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;

public class DiscordWebhookNotifier implements Notifier {

    private final DiscordChatboxBroadcasterConfig pluginConfiguration;
    private final OkHttpClient networkClient;

    public DiscordWebhookNotifier(DiscordChatboxBroadcasterConfig pluginConfiguration, OkHttpClient networkClient) {
        this.pluginConfiguration = pluginConfiguration;
        this.networkClient = networkClient;
    }

    @Override
    public void dispatchNotification(byte[] generatedImageData) {
        String targetWebhookUrl = pluginConfiguration.webhookUrl();

        if (targetWebhookUrl == null || targetWebhookUrl.isEmpty() || generatedImageData == null) {
            return;
        }

        MultipartBody.Builder requestBodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        requestBodyBuilder.addFormDataPart("file", "notification.png", RequestBody.create(MediaType.parse("image/png"), generatedImageData));

        Request networkRequest = new Request.Builder()
                .url(targetWebhookUrl)
                .post(requestBodyBuilder.build())
                .build();

        networkClient.newCall(networkRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call failedCall, IOException exception) {
            }

            @Override
            public void onResponse(Call successfulCall, Response networkResponse) {
                networkResponse.close();
            }
        });
    }
}