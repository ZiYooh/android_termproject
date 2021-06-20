package com.example.sns_project;

import android.os.AsyncTask;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.BufferedReader;
import java.io.IOException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class GetWeather{
    public String fn_time() {
        LocalDateTime nowDateTime = LocalDateTime.now();
        LocalDateTime yesterday = nowDateTime.minusDays(1);

        String timedata = nowDateTime.format(
                DateTimeFormatter.ofPattern("yyyyMMdd HHmmss")
        );

        String hh = timedata.substring(9, 11);
        hh = hh + "00";
        if (hh.equals("0000") || hh.equals("0100")) {
            timedata = yesterday.format(DateTimeFormatter.ofPattern("yyyyMMdd HHmmss"));
        }

        return timedata;

    }

    public String fn_timeChange(String timedata) {
        String hh = timedata.substring(9, 11);
        String baseTime = "";
        hh = hh + "00";

        // 현재 시간에 따라 데이터 시간 설정(3시간 마다 업데이트)
        switch (hh) {

            case "0200":
            case "0300":
            case "0400":
                baseTime = "0200";
                break;
            case "0500":
            case "0600":
            case "0700":
                baseTime = "0500";
                break;
            case "0800":
            case "0900":
            case "1000":
                baseTime = "0800";
                break;
            case "1100":
            case "1200":
            case "1300":
                baseTime = "1100";
                break;
            case "1400":
            case "1500":
            case "1600":
                baseTime = "1400";
                break;
            case "1700":
            case "1800":
            case "1900":
                baseTime = "1700";
                break;
            case "2000":
            case "2100":
            case "2200":
                baseTime = "2000";
                break;
            default:
                baseTime = "2300";

        }
        return baseTime;
    }

    public String fn_urlbuild(String baseDate, String baseTime, String nx, String ny) throws IOException, ParseException{
        String apiUrl = "http://apis.data.go.kr/1360000/VilageFcstInfoService/getVilageFcst?serviceKey";

        // 홈페이지에서 받은 키
        String serviceKey = "TN9pbkfy22%2B1aXHrZYejoBUcVp7I0su2sHGdA8447ZmVGBv9ypGI0ThwXN%2BJ8zOzUrEkNZ3qPE" +
                "N29wsPWEvbyw%3D%3D";
        String pageNo = "1";
        String numOfRows = "10";

        //String baseDate = "20210516"; 조회하고싶은 날짜 String baseTime = "1100"; 조회하고싶은 시간
        String type = "json"; //타입 xml, json 등등 ..

        StringBuilder urlBuilder = new StringBuilder(apiUrl);
        urlBuilder.append("=" + serviceKey);
        urlBuilder.append(
                "&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode(pageNo, "UTF-8")
        ); //pageNo
        urlBuilder.append(
                "&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode(numOfRows, "UTF-8")
        ); //numOfRows
        urlBuilder.append(
                "&" + URLEncoder.encode("dataType", "UTF-8") + "=" + URLEncoder.encode(type, "UTF-8")
        );/* 타입 */
        urlBuilder.append(
                "&" + URLEncoder.encode("base_date", "UTF-8") + "=" + URLEncoder.encode(baseDate, "UTF-8")
        );/* 조회하고싶은 날짜*/
        urlBuilder.append(
                "&" + URLEncoder.encode("base_time", "UTF-8") + "=" + URLEncoder.encode(baseTime, "UTF-8")
        );/* 조회하고싶은 시간 AM 02시부터 3시간 단위 */
        urlBuilder.append(
                "&" + URLEncoder.encode("nx", "UTF-8") + "=" + URLEncoder.encode(nx, "UTF-8")
        ); //경도
        urlBuilder.append(
                "&" + URLEncoder.encode("ny", "UTF-8") + "=" + URLEncoder.encode(ny, "UTF-8")
        ); //위도

        return urlBuilder.toString();
    }

    public String fn_weather(URL url) throws IOException, ParseException {
        /*
         * GET방식으로 전송해서 파라미터 받아오기
         */

        //어떻게 넘어가는지 확인하고 싶으면 아래 출력분 주석 해제
        //System
        //        .out
        //        .println(url);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");
        System
                .out
                .println("Response code: " + conn.getResponseCode());
        BufferedReader rd;
        if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        conn.disconnect();
        String result = sb.toString();
        System
                .out
                .println(result);

        // Json parser를 만들어 만들어진 문자열 데이터를 객체화
        JSONParser parser = new JSONParser();
        JSONObject obj = (JSONObject)parser.parse(result);
        // response 키를 가지고 데이터를 파싱
        JSONObject parse_response = (JSONObject)obj.get("response");
        // response 로 부터 body 찾기
        JSONObject parse_body = (JSONObject)parse_response.get("body");
        // body 로 부터 items 찾기
        JSONObject parse_items = (JSONObject)parse_body.get("items");

        // items로 부터 itemlist 를 받기
        JSONArray parse_item = (JSONArray)parse_items.get("item");
        String category;
        JSONObject weather; // parse_item은 배열형태이기 때문에 하나씩 데이터를 하나씩 가져올때 사용
        // 카테고리와 값만 받아오기 String day=""; String time="";

        String VALUE = "";
        String date = "";
        String time = "";
        String DataValue = "";
        String info = "";

        for (int i = 0; i < parse_item.size(); i++) {
            weather = (JSONObject)parse_item.get(i);

            date = weather
                    .get("baseDate")
                    .toString();
            time = weather
                    .get("baseTime")
                    .toString();
            DataValue = weather
                    .get("fcstValue")
                    .toString();
            info = weather
                    .get("category")
                    .toString();

            if (info.equals("POP")) {

                info = "강수확률";
                DataValue = DataValue + " %";
            }
            if (info.equals("REH")) {

                info = "습도";
                DataValue = DataValue + " %";
            }
            if (info.equals("SKY")) {
                info = "하늘상태";
                if (DataValue.equals("1")) {
                    DataValue = "맑음";
                } else if (DataValue.equals("2")) {
                    DataValue = "비";
                } else if (DataValue.equals("3")) {
                    DataValue = "구름많음";
                } else if (DataValue.equals("4")) {
                    DataValue = "흐림";
                }
            }
            if (info.equals("UUU")) {
                info = "동서성분풍속";
                DataValue = DataValue + " m/s";
            }
            if (info.equals("VVV")) {
                info = "남북성분풍속";
                DataValue = DataValue + " m/s";
            }
            if (info.equals("T1H")) {
                info = "기온";
                DataValue = DataValue;
            }
            if (info.equals("R06")) {
                info = "6시간강수량";
                DataValue = DataValue + " mm";

            }
            if (info.equals("S06")) {
                info = "6시간적설량";
                DataValue = DataValue + " mm";
            }
            if (info.equals("PTY")) {
                info = "강수형태";
                if (DataValue.equals("0")) {
                    DataValue = "없음";
                } else if (DataValue.equals("1")) {
                    DataValue = "비";
                } else if (DataValue.equals("2")) {
                    DataValue = "눈/비";
                } else if (DataValue.equals("3")) {
                    DataValue = "눈";
                }
            }
            if (info.equals("T3H")) {
                info = "3시간기온";
                DataValue = DataValue;
            }
            if (info.equals("VEC")) {
                info = "풍향";
                DataValue = DataValue + " m/s";
            }

            VALUE += info + "," + DataValue + "," + date + "," + time + ",";

        }

        return VALUE;
    }

}