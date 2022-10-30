package service;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import util.CommonUtil;

import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
public class TwitchService {
    public static final Set<String> TWITCH_CHANNEL_SET = new HashSet<>();
    public static final Map<String, Set<String>> TWITCH_NOTIFICATION_MAP = new HashMap<>();

    protected void execute() {
        if (TWITCH_NOTIFICATION_MAP.isEmpty()) {
            return;
        }

        final String responseString = callStreamApi();
        if (StringUtils.isBlank(responseString)) {
            return;
        }

        try {
            final JSONArray dataJsonArray = new JSONObject(responseString).getJSONArray("data");
            if (dataJsonArray.isEmpty()) {
                return;
            }

            for (int i = 0; i < dataJsonArray.length(); i++) {
                final JSONObject jsonObject = dataJsonArray.getJSONObject(i);
                final String type = jsonObject.getString("type");
                final String userLogin = jsonObject.getString("user_login");
                if (StringUtils.equals(type, "live") && !TWITCH_CHANNEL_SET.contains(userLogin)) {
                    TWITCH_CHANNEL_SET.add(userLogin);
                    notification(jsonObject);
                }
            }
        } catch (final JSONException exception) {
            exception.printStackTrace();
        }
    }

    private static String callStreamApi() {
        final HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
        try {
            final URIBuilder uriBuilder = new URIBuilder(CommonUtil.TWITCH_API_BASE_URI + "/streams");
            TWITCH_NOTIFICATION_MAP.keySet().forEach(key -> uriBuilder.addParameter("user_login", key));
            final HttpRequest httpRequest = HttpRequest.newBuilder()
                    .GET()
                    .uri(uriBuilder.build())
                    .header("Client-Id", CommonUtil.TWITCH_API_CLIENT_ID)
                    .header("Authorization", CommonUtil.TWITCH_API_TOKEN_TYPE + StringUtils.SPACE + CommonUtil.TWITCH_API_ACCESS_TOKEN)
                    .build();
            final HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            log.info(httpResponse.statusCode() + StringUtils.SPACE + httpResponse.body());
            return httpResponse.body();
        } catch (final URISyntaxException | IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return StringUtils.EMPTY;
    }

    private void notification(final JSONObject dataJsonObject) {
        final String userLogin = dataJsonObject.getString("user_login");
        final String title = "開台通知";
        final String desc = dataJsonObject.getString("user_name") + " - " + dataJsonObject.getString("title");
        final String thumb = dataJsonObject.getString("thumbnail_url").replace("-{width}x{height}", StringUtils.EMPTY);
        final Color color = new Color(144, 0, 255);

        for (final String messageChannelId : TWITCH_NOTIFICATION_MAP.get(userLogin)) {
            final MessageChannel messageChannel = CommonUtil.JDA.getChannelById(MessageChannel.class, messageChannelId);
            if (messageChannel == null) {
                continue;
            }

            final MessageEmbed messageEmbed = new EmbedBuilder().setTitle(title).setDescription(desc).setThumbnail(thumb)
                    .setColor(color).setAuthor("Twitch", null, CommonUtil.TWITCH_LOGO_URI).build();
            messageChannel.sendMessage("https://www.twitch.tv/" + userLogin).addEmbeds(messageEmbed).queue();
        }
    }

    public static void addDataToTwitchChannelSet() {
        final String responseString = TwitchService.callStreamApi();
        if (StringUtils.isBlank(responseString)) {
            return;
        }

        try {
            final JSONArray dataJsonArray = new JSONObject(responseString).getJSONArray("data");
            if (dataJsonArray.isEmpty()) {
                return;
            }

            for (int i = 0; i < dataJsonArray.length(); i++) {
                final String type = dataJsonArray.getJSONObject(i).getString("type");
                final String startedAt = dataJsonArray.getJSONObject(i).getString("started_at");
                if (StringUtils.equals(type, "live") && CommonUtil.checkStartTime(startedAt)) {
                    final String userLogin = dataJsonArray.getJSONObject(i).getString("user_login");
                    TWITCH_CHANNEL_SET.add(userLogin);
                }
            }
        } catch (final JSONException exception) {
            exception.printStackTrace();
        }
    }
}