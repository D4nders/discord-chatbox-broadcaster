package com.discordchatboxbroadcaster.notifier;

import com.discordchatboxbroadcaster.DiscordChatboxBroadcasterConfig;
import com.discordchatboxbroadcaster.render.ChatSegment;
import com.discordchatboxbroadcaster.render.ChatboxImageGenerator;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.DrawManager;
import net.runelite.client.util.ImageUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
public class DiscordWebhookNotifier implements Notifier {

    private final DiscordChatboxBroadcasterConfig pluginConfiguration;
    private final OkHttpClient networkClient;
    private final ScheduledExecutorService executorService;
    private final Gson gson;
    private final DrawManager drawManager;
    private final ChatboxImageGenerator imageGenerator;
    private final BlockingQueue<PendingNotification> notificationQueue;

    private ScheduledFuture<?> scheduledWorkerTask;

    public DiscordWebhookNotifier(
            DiscordChatboxBroadcasterConfig pluginConfiguration,
            OkHttpClient networkClient,
            ScheduledExecutorService executorService,
            Gson gson,
            DrawManager drawManager) {
        this.pluginConfiguration = pluginConfiguration;
        this.networkClient = networkClient;
        this.executorService = executorService;
        this.gson = gson;
        this.drawManager = drawManager;
        this.imageGenerator = new ChatboxImageGenerator(pluginConfiguration);
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
            scheduledWorkerTask = null;
        }
        notificationQueue.clear();
    }

    @Override
    public void dispatchNotification(List<ChatSegment> notificationSegments) {
        if (pluginConfiguration.attachScreenshot() && drawManager != null) {
            drawManager.requestNextFrameListener(image -> {
                BufferedImage screenshot = image != null ? ImageUtil.bufferedImageFromImage(image) : null;
                notificationQueue.offer(new PendingNotification(notificationSegments, screenshot));
            });
        } else {
            notificationQueue.offer(new PendingNotification(notificationSegments, null));
        }
    }

    private void processNotificationQueue() {
        try {
            PendingNotification pendingNotification = notificationQueue.poll();
            if (pendingNotification != null) {
                transmitPayload(pendingNotification);
            }
        } catch (Exception e) {
            log.debug("Error processing notification queue", e);
        }
    }

    private void transmitPayload(PendingNotification pendingNotification) {
        String targetWebhookUrl = pluginConfiguration.webhookUrl();
        if (targetWebhookUrl == null || targetWebhookUrl.trim().isEmpty()) {
            return;
        }

        byte[] generatedImageData = imageGenerator.generateChatboxImage(pendingNotification.getSegments());
        if (generatedImageData == null || generatedImageData.length == 0) {
            return;
        }

        MultipartBody.Builder requestBodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);

        String username = pluginConfiguration.webhookUsername();
        String avatarUrl = pluginConfiguration.webhookAvatarUrl();

        Map<String, String> payloadMap = new LinkedHashMap<>();
        if (username != null) {
            String trimmedUsername = username.trim();
            if (!trimmedUsername.isEmpty()) {
                if (trimmedUsername.length() > 80) {
                    trimmedUsername = trimmedUsername.substring(0, 80);
                }
                payloadMap.put("username", trimmedUsername);
            }
        }

        if (avatarUrl != null) {
            String trimmedAvatarUrl = avatarUrl.trim();
            if (!trimmedAvatarUrl.isEmpty()) {
                payloadMap.put("avatar_url", trimmedAvatarUrl);
            }
        }

        if (!payloadMap.isEmpty() && gson != null) {
            String payloadJson = gson.toJson(payloadMap);
            requestBodyBuilder.addFormDataPart("payload_json", payloadJson);
        }

        requestBodyBuilder.addFormDataPart(
                "files[0]",
                "notification.png",
                RequestBody.create(MediaType.parse("image/png"), generatedImageData)
        );

        if (pendingNotification.getScreenshot() != null) {
            byte[] screenshotData = convertImageToByteArray(pendingNotification.getScreenshot());
            if (screenshotData != null && screenshotData.length > 0) {
                requestBodyBuilder.addFormDataPart(
                        "files[1]",
                        "screenshot.png",
                        RequestBody.create(MediaType.parse("image/png"), screenshotData)
                );
            }
        }

        Request networkRequest = new Request.Builder()
                .url(targetWebhookUrl)
                .post(requestBodyBuilder.build())
                .build();

        networkClient.newCall(networkRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call failedCall, IOException exception) {
                log.debug("Error sending Discord webhook notification", exception);
            }

            @Override
            public void onResponse(Call successfulCall, Response networkResponse) {
                try {
                    if (!networkResponse.isSuccessful()) {
                        log.debug("Discord webhook returned non-2xx response: code={}, message={}",
                                networkResponse.code(), networkResponse.message());
                    }
                } finally {
                    networkResponse.close();
                }
            }
        });
    }

    private byte[] convertImageToByteArray(BufferedImage image) {
        if (image == null) {
            return null;
        }
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            log.debug("Failed to encode screenshot to PNG", e);
            return null;
        }
    }
}