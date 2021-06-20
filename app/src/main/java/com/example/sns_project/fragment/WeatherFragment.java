package com.example.sns_project.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.sns_project.BottomDTO;
import com.example.sns_project.EtcDTO;
import com.example.sns_project.GetWeather;
import com.example.sns_project.GpsTracker;
import com.example.sns_project.Out_erDTO;
import com.example.sns_project.R;
import com.example.sns_project.TopDTO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class WeatherFragment extends Fragment {
    private static final String TAG = "WeatherFragment";

    int nCurrentPermission = 0;
    int alarmHour = 0;
    int alarmMinute = 0;
    static final int PERMISSIONS_REQUEST = 0x0000001;

    // GPS 관련
    private GpsTracker gpsTracker;

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    //

    Button timeSet;
    TextView alarmTime;
    Button feedback;
    String[] feedbackMsg = new String[8];

    TextView time;
    int count=0;

    TextView location;
    TextView temp;
    TextView weather;
    ImageView weathericon;

    TextView top;
    TextView bottom;
    TextView out_er;
    TextView etc;

    private SimpleDateFormat format=new SimpleDateFormat("yyyy년 MM월 dd일 a hh:mm:ss");

    Calendar cal = Calendar.getInstance();
    int hour = cal.get(Calendar.HOUR_OF_DAY);

    String city = "부산";
    String tempDo = "";
    String weatherDo = "";

    String top_result = "";
    String bottom_result = "";
    String out_er_result = "";
    String etc_result = "";

    List<TopDTO> top_items =  new ArrayList<>();
    List<BottomDTO> bottom_items =  new ArrayList<>();
    List<Out_erDTO> out_er_items =  new ArrayList<>();
    List<EtcDTO> etc_items =  new ArrayList<>();

    public WeatherFragment(){
        // Empty Constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

            View view = inflater.inflate(R.layout.fragment_weather, container, false);

            alarmTime = (TextView) view.findViewById(R.id.alarmTime);

            timeSet = (Button) view.findViewById(R.id.timeSet);
            feedback = (Button) view.findViewById(R.id.feedback);

            location = (TextView) view.findViewById(R.id.location);
            temp = (TextView) view.findViewById(R.id.temp);
            weather = (TextView) view.findViewById(R.id.weather);
            weathericon = (ImageView) view.findViewById(R.id.weatherIcon);
            //location.append(city);

            time=(TextView) view.findViewById(R.id.time);

            top = (TextView) view.findViewById(R.id.top);
            bottom = (TextView) view.findViewById(R.id.bottom);
            out_er = (TextView) view.findViewById(R.id.out_er);
            etc = (TextView) view.findViewById(R.id.etc);

            ArrayList xyLocation = new ArrayList<>();

            OnCheckPermission();

            // 위치 확인
            gpsTracker = new GpsTracker(container.getContext());

            double latitude = gpsTracker.getLatitude();
            double longitude = gpsTracker.getLongitude();

            String address = getCurrentAddress(latitude, longitude);
            String resultAddress[] = address.split(" ");

            StringBuilder finalAddress = new StringBuilder();

            for(int i = 1; i < 4; i++){
                finalAddress.append(resultAddress[i]);
                if(i != 3){
                    finalAddress.append(" ");
                }
            }

            location.setText("현재위치: " + finalAddress.toString());

            AsyncGPS gpsTask = new AsyncGPS();
            
        try {
            xyLocation = gpsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, resultAddress).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 날씨 요청받은 JSON 값이 파싱돼서 들어갈 배열
            String[] ResponseData = new String[0];

            // 온도, 날씨, 날씨아이콘 처리하는 부분
            final AsyncWeather weatherTask = new AsyncWeather();

            try {
                // [AsyncTask클래스이름].executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR); 로 여러 Asynctask 병렬적 수행 가능
                ResponseData = weatherTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, xyLocation).get().split(",");
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            new AsyncCounterTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            if(ResponseData[8].equals("6시간강수량")){
                if(ResponseData[21].equals("구름많음")){
                    if((hour >= 6) && (hour <= 18)){
                        weathericon.setImageResource(R.drawable.cloudsun); // 날씨별 아이콘 변경
                    }
                    else{
                        weathericon.setImageResource(R.drawable.cloudmoon); // 날씨별 아이콘 변경
                    }
                }

                if(ResponseData[21].equals("맑음")){
                    if((hour >= 6) && (hour <= 18)){
                        weathericon.setImageResource(R.drawable.sun); // 날씨별 아이콘 변경
                    }
                    else{
                        weathericon.setImageResource(R.drawable.moon); // 날씨별 아이콘 변경
                    }
                }

                if(ResponseData[21].equals("흐림")){
                    weathericon.setImageResource(R.drawable.cloud); // 날씨별 아이콘 변경
                }


                if(ResponseData[5].equals("비")){ // ResponseData[5]의 값은 강수형태가 들어간다. 비나 눈이 오면 온도 위치가 달라짐
                    tempDo = ResponseData[25];
                    weatherDo = ResponseData[5];
                    weathericon.setImageResource(R.drawable.rain); // 날씨별 아이콘 변경
                } else if(ResponseData[5].equals("눈")) {
                    tempDo = ResponseData[25];
                    weatherDo = ResponseData[5];
                    weathericon.setImageResource(R.drawable.snow); // 날씨별 아이콘 변경
                } else {
                    tempDo = ResponseData[25];
                    weatherDo = ResponseData[21];
                }

            } else{
                if(ResponseData[13].equals("구름많음")){
                    if((hour >= 6) && (hour <= 18)){
                        weathericon.setImageResource(R.drawable.cloudsun); // 날씨별 아이콘 변경
                    }
                    else{
                        weathericon.setImageResource(R.drawable.cloudmoon); // 날씨별 아이콘 변경
                    }
                }

                if(ResponseData[13].equals("맑음")){
                    if((hour >= 6) && (hour <= 18)){
                        weathericon.setImageResource(R.drawable.sun); // 날씨별 아이콘 변경
                    }
                    else{
                        weathericon.setImageResource(R.drawable.moon); // 날씨별 아이콘 변경
                    }
                }

                if(ResponseData[13].equals("흐림")){
                    weathericon.setImageResource(R.drawable.cloud); // 날씨별 아이콘 변경
                }

                tempDo = ResponseData[17];
                weatherDo = ResponseData[13];

            }


            temp.append(tempDo);
            temp.append(" ℃");
            weather.append(weatherDo);
            // 온도, 날씨, 날씨아이콘 처리하는 부분 끝

            // 추천 의상 출력 처리하는 부분

            try {
                top_result = new DBTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, tempDo, "top").get();
                bottom_result = new DBTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, tempDo, "bottom").get();
                out_er_result = new DBTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, tempDo, "out_er").get();
                etc_result = new DBTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, tempDo, "etc").get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            // 상의 구하기
            try {
                // 받아온 source를 JSONObject로 변환한다.
                JSONObject jsonObj = new JSONObject(top_result);

                JSONArray jArray = (JSONArray) jsonObj.get("top");

                int length = jArray.length();
                Log.d("JSONArray : ", "길이: " + length);

                for(int i = 0; i < length; i++){
                    // 0번째 JSONObject를 받아옴
                    JSONObject row = jArray.getJSONObject(i);
                    TopDTO dto = new TopDTO();
                    dto.setName(row.getString("name"));
                    dto.setTemp_high(row.getInt("temp_high"));
                    dto.setTemp_low(row.getInt("temp_low"));
                    dto.setSpring(row.getInt("spring"));
                    dto.setSummer(row.getInt("summer"));
                    dto.setFall(row.getInt("fall"));
                    dto.setWinter(row.getInt("winter"));

                    top_items.add(dto);
                }

            } catch(JSONException e){
                e.printStackTrace();
            }
            Log.i("태그", "결과: " + top_result);
            for(int i = 0; i < top_items.size(); i++){
                Log.d("의상확인 ", "이름: " + top_items.get(i).getName());
                top.append(top_items.get(i).getName());
                if(i != top_items.size() - 1){
                    top.append(", ");
                }
            }

        // 하의 구하기
            try {
                // 받아온 source를 JSONObject로 변환한다.
                JSONObject jsonObj = new JSONObject(bottom_result);

                JSONArray jArray = (JSONArray) jsonObj.get("bottom");

                int length = jArray.length();
                Log.d("JSONArray : ", "길이: " + length);

                for(int i = 0; i < length; i++){
                    // 0번째 JSONObject를 받아옴
                    JSONObject row = jArray.getJSONObject(i);
                    BottomDTO dto = new BottomDTO();
                    dto.setName(row.getString("name"));
                    dto.setTemp_high(row.getInt("temp_high"));
                    dto.setTemp_low(row.getInt("temp_low"));
                    dto.setSpring(row.getInt("spring"));
                    dto.setSummer(row.getInt("summer"));
                    dto.setFall(row.getInt("fall"));
                    dto.setWinter(row.getInt("winter"));

                    bottom_items.add(dto);
                }

            } catch(JSONException e){
                e.printStackTrace();
            }
            Log.i("태그", "결과: " + bottom_result);
            for(int i = 0; i < bottom_items.size(); i++){
                Log.d("의상확인 ", "이름: " + bottom_items.get(i).getName());
                bottom.append(bottom_items.get(i).getName());
                if(i != bottom_items.size() - 1){
                    bottom.append(", ");
                }
            }

            // 아우터 구하기
            try {
                // 받아온 source를 JSONObject로 변환한다.
                JSONObject jsonObj = new JSONObject(out_er_result);

                JSONArray jArray = (JSONArray) jsonObj.get("out_er");

                int length = jArray.length();
                Log.d("JSONArray : ", "길이: " + length);

                for(int i = 0; i < length; i++){
                    // 0번째 JSONObject를 받아옴
                    JSONObject row = jArray.getJSONObject(i);
                    Out_erDTO dto = new Out_erDTO();
                    dto.setName(row.getString("name"));
                    dto.setTemp_high(row.getInt("temp_high"));
                    dto.setTemp_low(row.getInt("temp_low"));
                    dto.setSpring(row.getInt("spring"));
                    dto.setSummer(row.getInt("summer"));
                    dto.setFall(row.getInt("fall"));
                    dto.setWinter(row.getInt("winter"));

                    out_er_items.add(dto);
                }

            } catch(JSONException e){
                e.printStackTrace();
            }
            Log.i("태그", "결과: " + out_er_result);
            if(out_er_items.size() == 0) {
                out_er.append("없음");
            } else {
                for(int i = 0; i < out_er_items.size(); i++){
                    Log.d("의상확인 ", "이름: " + out_er_items.get(i).getName());
                    out_er.append(out_er_items.get(i).getName());
                    if(i != out_er_items.size() - 1) {
                        out_er.append(", ");
                    }
                }
            }


            // 기타 구하기
            try {
                // 받아온 source를 JSONObject로 변환한다.
                JSONObject jsonObj = new JSONObject(etc_result);

                JSONArray jArray = (JSONArray) jsonObj.get("etc");

                int length = jArray.length();
                Log.d("JSONArray : ", "길이: " + length);

                for(int i = 0; i < length; i++){
                    // 0번째 JSONObject를 받아옴
                    JSONObject row = jArray.getJSONObject(i);
                    EtcDTO dto = new EtcDTO();
                    dto.setName(row.getString("name"));
                    dto.setTemp_high(row.getInt("temp_high"));
                    dto.setTemp_low(row.getInt("temp_low"));
                    dto.setSpring(row.getInt("spring"));
                    dto.setSummer(row.getInt("summer"));
                    dto.setFall(row.getInt("fall"));
                    dto.setWinter(row.getInt("winter"));

                    etc_items.add(dto);
                }

            } catch(JSONException e){
                e.printStackTrace();
            }
            Log.i("태그", "결과: " + etc_result);
            if(etc_items.size() == 0){
                etc.append("없음");
            } else {
                for (int i = 0; i < etc_items.size(); i++) {
                    Log.d("의상확인 ", "이름: " + etc_items.get(i).getName());
                    etc.append(etc_items.get(i).getName());
                    if(i != etc_items.size() - 1){
                        etc.append(", ");
                    }
                }
            }


        // 알림 설정하는 부분 시작
        timeSet.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(
                        container.getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                        alarmTime.setText("알림 설정: " + hourOfDay + "시 " + minute + "분");
                    }
                }, alarmHour, alarmMinute, false);
                timePickerDialog.show();
            }
        });
        // 알림 설정하는 부분 끝

        // 피드백 보내는 부분 시작
        Date sendtime = new Date();
        String sendtime1 = format.format(sendtime);

        feedbackMsg[1] = top.getText().toString();
        feedbackMsg[2] = bottom.getText().toString();
        feedbackMsg[3] = out_er.getText().toString();
        feedbackMsg[4] = etc.getText().toString();
        feedbackMsg[5] = sendtime1;
        feedbackMsg[6] = tempDo;
        feedbackMsg[7] = weatherDo;

        final FeedbackTask feedbackTask = new FeedbackTask();

        feedback.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                feedbackMsg[0] =  "불만족";
                /* 커스텀 다이얼로그로 EditText 받아오는게 오류나서 일단 보류
                CustomDialog customDialog = new CustomDialog(container.getContext());
                // CustomDialog.java에서 callfunction 함수 매개변수 설정 시 괄호 안에 넘겨받을 매개변수 설정 가능
                customDialog.callFunction();
                */
                Toast.makeText(container.getContext(), "의견 감사합니다.", Toast.LENGTH_SHORT).show();
                feedbackTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, feedbackMsg);
            }
        });
        // 피드백 보내는 부분 끝

        return view;
    }
    
    // GPS 관련 시작


    public void OnCheckPermission() {

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED

                || ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {

                Toast.makeText(getActivity(), "앱 실행을 위해서는 권한을 설정해야 합니다", Toast.LENGTH_LONG).show();

                ActivityCompat.requestPermissions(getActivity(),

                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},

                        PERMISSIONS_REQUEST);

            } else {

                ActivityCompat.requestPermissions(getActivity(),

                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},

                        PERMISSIONS_REQUEST);

            }
        }
    }

    @Override

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {

            case PERMISSIONS_REQUEST :

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getActivity(), "앱 실행을 위한 권한이 설정 되었습니다", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity(), "앱 실행을 위한 권한이 취소 되었습니다", Toast.LENGTH_LONG).show();

                }
                break;

        }
    }


    /*
    //ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드입니다.

    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        super.onRequestPermissionsResult(permsRequestCode, permissions, grandResults);
        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면

            boolean check_result = true;


            // 모든 퍼미션을 허용했는지 체크합니다.

            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }


            if (check_result) {

                //위치 값을 가져올 수 있음
                ;
            } else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.

                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), REQUIRED_PERMISSIONS[1])) {

                    Toast.makeText(getActivity(), "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity(), "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ", Toast.LENGTH_LONG).show();
                }
            }

        }
    }
    */


    void checkRunTimePermission(){

        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION);


        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {

            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)


            // 3.  위치 값을 가져올 수 있음



        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(getActivity(), "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(getActivity(), REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);


            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(getActivity(), REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }

        }

    }

    public String getCurrentAddress( double latitude, double longitude) {

        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());

        List<Address> addresses;

        try {

            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    7);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(getActivity(), "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(getActivity(), "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";

        }

        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(getActivity(), "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";

        }

        Address address = addresses.get(0);
        return address.getAddressLine(0).toString()+"\n";
    }


    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    // GPS 관련 끝



    // 추천 의상 출력 처리하는 부분 끝

    // 시계 AsyncTask
    class AsyncCounterTask extends AsyncTask<Void, Void, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Log.e("__LOG__","onPreExecute");
        }
        @Override
        protected Integer doInBackground(Void... integers) {
            while(count<100){
                try {
                    Thread.sleep(1000);
                }catch(InterruptedException e){}
                //publishProgress 호출 후 바로 onProgressUpdate가 호출됨
                publishProgress();
            }
            //Log.e("__LOG__","doInBackground End");
            return count;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            //Log.e("__LOG__","onProgressUpdate");
            long now = System.currentTimeMillis();
            time.setText(format.format(new Date(now)));
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            //("__LOG__","onPostExecute");
        }
    }

    // 기상청 X Y 값 구하기 위한 AsyncTask
    class AsyncGPS extends AsyncTask<String, Void, ArrayList> {
        @Override
        protected ArrayList doInBackground(String ... strings) {
            String result;
            String areaTop = strings[1];	//지역
            String areaMdl = strings[2];

            String code="";	//지역 코드

            ArrayList loca = new ArrayList<>();

            String x;
            String y;

            URL url = null;
            BufferedReader br = null;
            URLConnection conn = null;

            JSONArray jArr;

            try{
                // 시 검색
                url = new URL("http://www.kma.go.kr/DFSROOT/POINT/DATA/top.json.txt");
                conn = url.openConnection();
                br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                result = br.readLine().toString();
                br.close();
                jArr = new JSONArray(result);

                for(int i = 0 ; i < jArr.length(); i++) {
                    JSONObject temp = jArr.getJSONObject(i);
                    String tempValue = temp.getString("value");
                    if(tempValue.equals(areaTop)){
                        code = temp.getString("code");
                        break;
                    }
                }

                //구 검색
                url = new URL("http://www.kma.go.kr/DFSROOT/POINT/DATA/mdl."+code+".json.txt");
                conn = url.openConnection();
                br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                result = br.readLine().toString();
                br.close();
                jArr = new JSONArray(result);

                for(int i = 0 ; i < jArr.length(); i++) {
                    JSONObject temp = jArr.getJSONObject(i);
                    String tempValue = temp.getString("value");
                    if(tempValue.equals(areaMdl)){
                        code = temp.getString("code");
                        break;
                    }
                }

                //동 검색
                url = new URL("http://www.kma.go.kr/DFSROOT/POINT/DATA/leaf."+code+".json.txt");
                conn = url.openConnection();
                br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                result = br.readLine().toString();
                br.close();
                jArr = new JSONArray(result);

                JSONObject temp = jArr.getJSONObject(0);

                loca.add(temp.getString("x"));
                loca.add(temp.getString("y"));

            } catch(MalformedURLException e){
                e.printStackTrace();
            } catch(IOException e){
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return loca;
        }
    }


    class AsyncWeather extends AsyncTask<ArrayList, Void, String> {

        @Override
        protected String doInBackground(ArrayList ... params) {

            //날씨 정보 받아오기 위한 GetWeather 클래스 생성
            GetWeather weatherData = new GetWeather();

            //fn_time 함수를 사용하여 현재시간 받아오기
            String timedata = weatherData.fn_time();

            //현재시간을 활용하여 기상청 시간데이터 형식에 맞게 변환
            String baseDate = timedata.substring(0, 8);
            String baseTime = weatherData.fn_timeChange(timedata);

            ArrayList<String> temp = new ArrayList<>();
            temp = params[0];
            String nx = temp.get(0);
            String ny = temp.get(1);

            String geturl = "";
            String returnData = "";

            {
                try {
                    geturl = weatherData.fn_urlbuild(baseDate, baseTime, nx, ny);
                    Log.v("태그", "geturl 합니다");
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }



            try {
                Log.v("result", "결과넣기 전 입니다" + returnData);
                returnData = weatherData.fn_weather(new URL(geturl));
                Log.v("result", "결과 입니다" + returnData);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            return returnData;
        }
    }

    // DB에서 추천의상 가져오는 AsyncTask
    class DBTask extends AsyncTask<String, Void, String> {
        String sendMsg, receiveMsg;
        //JSONObject receiveMsg = new JSONObject();
        @Override
        protected String doInBackground(String... strings) {
            try {
                String str;
                StringBuffer sb = new StringBuffer();
                URL url = new URL("http://180.69.110.110:8080/android_project_team16/data.jsp");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");

                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                sendMsg = "temp="+strings[0]+"&part="+strings[1];
                osw.write(sendMsg);
                osw.flush();

                if(conn.getResponseCode() == conn.HTTP_OK) {
                    InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "UTF-8");
                    BufferedReader reader = new BufferedReader(tmp);

                    while (true) {
                        String line = reader.readLine();
                        if (line == null)
                            break;
                        sb.append(line);
                    }
                    //Log.d("myLog", sb.toString());

                    receiveMsg = sb.toString();

                } else {
                    Log.i("통신 결과", conn.getResponseCode()+"에러");
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return receiveMsg;
        }
    }

    class FeedbackTask extends AsyncTask<String, Void, Void> {
        String sendMsg, receiveMsg;
        @Override
        protected Void doInBackground(String... strings) {
            try {
                String str;
                URL url = new URL("http://180.69.110.110:8080/android_project_team16/feedback.jsp");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                sendMsg = "text="+strings[0]+"&top="+strings[1]+"&bottom="+strings[2]+"&out_er="+strings[3]+"&etc="+strings[4]+"&time="+strings[5]+"&temp="+strings[6]+"&weather="+strings[7];
                osw.write(sendMsg);
                osw.flush();
                if(conn.getResponseCode() == conn.HTTP_OK) {
                    InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "UTF-8");
                    BufferedReader reader = new BufferedReader(tmp);
                    StringBuffer buffer = new StringBuffer();
                    while ((str = reader.readLine()) != null) {
                        buffer.append(str);
                    }
                    receiveMsg = buffer.toString();

                } else {
                    Log.i("통신 결과", conn.getResponseCode()+"에러");
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}

