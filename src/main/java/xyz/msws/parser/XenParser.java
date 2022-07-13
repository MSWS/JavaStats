package xyz.msws.parser;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.lang.Nullable;
import org.springframework.web.util.UriBuilder;
import xyz.msws.data.PostSnapshot;
import xyz.msws.formatter.ForumsFormat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class XenParser implements ForumParser {
    private final String baseURL;

    private URL threadURL;

    private final String token;

    public XenParser(String token, long threadId, String baseURL) {
        this.token = token;
        this.baseURL = baseURL;

        try {
            threadURL = new URL(baseURL + "/threads/" + threadId);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    @Nullable
    public PostSnapshot getData() {
        String mostRecent = getRaw(threadURL);
        if (mostRecent == null)
            return null;
        JsonObject threadRecent = JsonParser.parseString(mostRecent).getAsJsonObject().getAsJsonObject("thread");
        int lastPostID = threadRecent.get("last_post_id").getAsInt();
        String lastPost = getRaw(baseURL + "/posts/" + lastPostID);
        if (lastPost == null)
            return null;
        JsonObject post = JsonParser.parseString(lastPost).getAsJsonObject().getAsJsonObject("post");
        String date = post.get("message").getAsString();
        date = date.substring(0, date.indexOf("\n"));
        SimpleDateFormat format = ForumsFormat.sdf;
        long time;
        try {
            time = format.parse(date).getTime();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return new PostSnapshot(time, date, lastPostID);
    }

    private String getRaw(String url) {
        try {
            return getRaw(new URL(url));
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getRaw(URL url) {
        HttpURLConnection query = null;
        try {
            query = (HttpURLConnection) url.openConnection();
            query.setRequestMethod("GET");
            query.addRequestProperty("XF-Api-Key", token);
            query.addRequestProperty("with_last_post", "true");
            query.addRequestProperty("encoding", "application/x-www-form-urlencoded");
            if (query.getResponseCode() != HttpURLConnection.HTTP_OK) {
                System.out.println("Error: could not fetch latest xen post from " + url.toString());
                System.out.println(query.getResponseCode() + ": " + query.getResponseMessage());
                return null;
            }
            InputStream in = query.getInputStream();
            BufferedReader read = new BufferedReader(new InputStreamReader(in));
            String line;
            StringBuilder data = new StringBuilder();
            while ((line = read.readLine()) != null) data.append(line);
            return data.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
