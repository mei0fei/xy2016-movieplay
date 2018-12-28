package w.cn.movie;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class HistoryActivity extends AppCompatActivity {
    ListView lv;
    final String Lag = HistoryActivity.class.getSimpleName();
    static final String db_name = "db_movie";//数据库名称
    static final String tb_name = "tb_movie";//数据表名称
    SQLiteDatabase db;//数据库对象
    Cursor cur;//存放查询结果的Cursor对象
    private List<EntityVideo> entityVideoList = new ArrayList<>();
    List<EntityVideo> sysVideoList;
    private Button clear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        //打开数据库
        db = openOrCreateDatabase(db_name, Context.MODE_PRIVATE, null);
        lv = (ListView) findViewById(R.id.lv);
        sysVideoList = getList(this);
        addData(sysVideoList);//添加数据
        EntityVideoAdapter adapter = new EntityVideoAdapter(HistoryActivity.this, R.layout.item1, entityVideoList, 1);
        lv = (ListView) findViewById(R.id.lv);
        lv.setAdapter(adapter);//设置Adapter
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                cur = db.rawQuery("SELECT * FROM " + tb_name, null);//重新查询
                cur.moveToPosition(i);
                Intent data = new Intent();
                data.putExtra("history_uri", cur.getString(cur.getColumnIndex("movie_uri")));
                data.putExtra("history_pos", cur.getString(cur.getColumnIndex("pos")));
                data.putExtra("movie_name", cur.getString(cur.getColumnIndex("movie_name")));
                Log.d(Lag, " 视频进度：" + cur.getString(cur.getColumnIndex("pos")));
                setResult(30, data);
                cur.close();
                finish();
            }
        });

        clear = findViewById(R.id.clear);//清除播放历史
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int d = db.delete(tb_name, null, null);
                setContentView(R.layout.activity_history);
                Toast.makeText(HistoryActivity.this, "已清除" + d + "条记录", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //读取数据库数据存入集合
    public List<EntityVideo> getList(Context context) {
        List<EntityVideo> sysVideoList = new ArrayList<>();
        cur = db.rawQuery("SELECT * FROM " + tb_name, null);//重新查询
        if (cur == null) {
            return sysVideoList;
        }
        if (cur.moveToFirst()) {
            do {
                EntityVideo info = new EntityVideo();
                info.setThumb_path(cur.getString(cur.getColumnIndex("thumb_path")));//缩略图地址
                info.setMovie_name(cur.getString(cur.getColumnIndex("movie_name")));//视频名称
                info.setMovie_uri(cur.getString(cur.getColumnIndex("movie_uri")));//视频地址
                info.setDuration(cur.getInt(cur.getColumnIndex("pos")));//视频时长
                sysVideoList.add(info);
            } while (cur.moveToNext());
        }
        cur.close();
        return sysVideoList;
    }

    //添加数据
    private void addData(List<EntityVideo> List) {
        for (int i = 0; i < List.size(); i++) {
            EntityVideo entityVideo = new EntityVideo(List.get(i).thumb_path, List.get(i).movie_name, List.get(i).movie_uri, List.get(i).duration);
            entityVideoList.add(entityVideo);
        }
    }
}
