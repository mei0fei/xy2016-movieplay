package w.cn.movie;

import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class NetworkVideo extends AppCompatActivity {

    private final int GET_DATA_SUCCESS = 101;//获取数据成功的标志
    private BufferedReader bufferedReader;
    private InputStream inputStream;
    private StringBuilder stringBuilder;
    private HttpURLConnection connection;
    private ArrayList<EntityVideo> entityVideos = new ArrayList<>();
    private List<EntityVideo> entityVideoList = new ArrayList<>();
    private String data = null;
    private ListView lv3;

    //接收子线程传回的数据
    Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            if (message.what == GET_DATA_SUCCESS) {
                data = message.getData().getString("data");
                addData(parseJson(data));
                EntityVideoAdapter adapter = new EntityVideoAdapter(NetworkVideo.this, R.layout.item4, entityVideoList, 4);
                lv3.setAdapter(adapter);//设置Adapter
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_video);
        initData();

        lv3 = (ListView) findViewById(R.id.lv3);
        lv3.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                EntityVideo entityVideo = entityVideoList.get(i);
                Intent intent = new Intent();
                intent.putExtra("thumb_path", entityVideo.getThumb_path());//缩略图地址
                intent.putExtra("movie_uri", entityVideo.getMovie_uri());
                intent.putExtra("movie_name", entityVideo.getMovie_name());
                setResult(40, intent);
                finish();
            }
        });

    }

    //初始化数据
    private void initData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String data = getDataFromServer();
                //创建信息对象
                Message message = Message.obtain();
                Bundle bundle = new Bundle();
                bundle.putString("data", data);
                message.setData(bundle);
                message.what = GET_DATA_SUCCESS;
                //向主线程发信息
                mHandler.sendMessage(message);
            }
        }).start();
    }

    //从服务器请求数据
    private String getDataFromServer() {
        try {
            URL url = new URL("http://api.m.mtime.cn/PageSubArea/TrailerList.api");
            //打开链接
            connection = (HttpURLConnection) url.openConnection();

            if (connection.getResponseCode() == 200) {
                inputStream = connection.getInputStream();
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                stringBuilder = new StringBuilder();
                for (String line = ""; (line = bufferedReader.readLine()) != null; ) {
                    stringBuilder.append(line);
                }
                return stringBuilder.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedReader != null) bufferedReader.close();
                if (inputStream != null) inputStream.close();
                if (connection != null) connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    //Json数据解析
    private ArrayList<EntityVideo> parseJson(String json) {
        ArrayList<EntityVideo> entityVideos = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.optJSONArray("trailers");
            if (jsonArray != null && jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObjectItem = (JSONObject) jsonArray.get(i);
                    if (jsonObjectItem != null) {
                        EntityVideo entityVideo = new EntityVideo();
                        String movie_name = jsonObjectItem.optString("movieName");
                        entityVideo.setMovie_name(movie_name);
                        String thumb_path = jsonObjectItem.optString("coverImg");
                        entityVideo.setThumb_path(thumb_path);
                        String movie_uri = jsonObjectItem.optString("hightUrl");
                        entityVideo.setMovie_uri(movie_uri);
                        int duration = jsonObjectItem.optInt("videoLength");
                        entityVideo.setDuration(duration * 1000);
                        String summary = jsonObjectItem.optString("summary");
                        entityVideo.setSummary(summary);
                        entityVideos.add(entityVideo);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return entityVideos;
    }

    //数据添加
    private void addData(List<EntityVideo> List) {
        for (int i = 0; i < List.size(); i++) {
            EntityVideo entityVideo1 = new EntityVideo(
                    List.get(i).thumb_path,
                    List.get(i).movie_name,
                    List.get(i).movie_uri,
                    List.get(i).summary,
                    List.get(i).duration);
            entityVideoList.add(entityVideo1);
        }
    }
}
