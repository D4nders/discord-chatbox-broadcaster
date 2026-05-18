package com.discordchatboxbroadcaster.notifier;

import com.discordchatboxbroadcaster.DiscordChatboxBroadcasterConfig;
import com.discordchatboxbroadcaster.render.ChatSegment;
import com.discordchatboxbroadcaster.render.ChatboxImageGenerator;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class DiscordWebhookNotifier implements Notifier {

    private final DiscordChatboxBroadcasterConfig pluginConfiguration;
    private final OkHttpClient networkClient;
    private final ScheduledExecutorService executorService;
    private final ChatboxImageGenerator imageGenerator;
    private final BlockingQueue<List<ChatSegment>> notificationQueue;

    private ScheduledFuture<?> scheduledWorkerTask;

    public DiscordWebhookNotifier(DiscordChatboxBroadcasterConfig pluginConfiguration, OkHttpClient networkClient, ScheduledExecutorService executorService) {
        this.pluginConfiguration = pluginConfiguration;
        this.networkClient = networkClient;
        this.executorService = executorService;
        this.imageGenerator = new ChatboxImageGenerator();
        this.notificationQueue = new LinkedBlockingQueue<>();
    }

    @Override
    public void initialize() {
        scheduledWorkerTask = executorService.scheduleAtFixedRate(this::processNotificationQueue, 0, 600, TimeUnit.MILLISECONDS);
    }

    @Override
    public void terminate() {
        if (scheduledWorkerTask != null) {
            scheduledWorkerTask.cancel(false);
        }
    }

    @Override
    public void dispatchNotification(List<ChatSegment> notificationSegments) {
        notificationQueue.offer(notificationSegments);
    }

    private void processNotificationQueue() {
        List<ChatSegment> targetSegments = notificationQueue.poll();
        if (targetSegments != null) {
            transmitPayload(targetSegments);
        }
    }

    private void transmitPayload(List<ChatSegment> targetSegments) {
        String targetWebhookUrl = pluginConfiguration.webhookUrl();

        if (targetWebhookUrl == null || targetWebhookUrl.isEmpty()) {
            return;
        }

        byte[] generatedImageData = imageGenerator.generateChatboxImage(targetSegments);

        if (generatedImageData == null || generatedImageData.length == 0) {
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