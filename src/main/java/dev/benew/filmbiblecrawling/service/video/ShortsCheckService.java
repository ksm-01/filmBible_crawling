package dev.benew.filmbiblecrawling.service.video;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class ShortsCheckService {


    public Boolean shortsUrlCheck(String shortsUrl, String videoId) {

        try {
            URL url = new URL(shortsUrl);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setInstanceFollowRedirects(false);   // 리다이렉션 옵션
            connection.connect();

            int responseCode = connection.getResponseCode();

//            System.out.println("code:" + responseCode);
            connection.disconnect();

            // 첫 요청에 200이면
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println(shortsUrl);
                return true;
            } else {
                return false;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

    }


}
