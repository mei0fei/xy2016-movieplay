package w.cn.movie;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.universalvideoview.MyMediaController;
import com.universalvideoview.MyVideoView;


public class MainActivity extends AppCompatActivity implements MyVideoView.VideoViewCallback {

    private static final String SEEK_POSITION_KEY = "SEEK_POSITION_KEY";

    MyMediaController mMediaController;
    View mBottomLayout;
    View mVideoLayout;
    TextView title;
    final MediaPlayer mMediaPlayer = new MediaPlayer();
    private int pos = 0;//播放进度
    private int cachedHeight;//缓存进度
    private boolean isFullscreen;//全屏/窗口播放切换标志
    final int REQUEST_CODE = 20;
    TextView history, local, luzhi, network_video;
    Uri uri;
    private MyVideoView mVideoView;
    ListView lv;
    static final String db_name = "db_movie";//数据库名称
    static final String tb_name = "tb_movie";//数据表名称
    String movie_name, movie_uri, thumb_path;
    SQLiteDatabase db;//数据库对象
    Cursor cur;//存放查询结果的Cursor对象
    final String Lag = MainActivity.class.getSimpleName();
    private String movie_uri2;
    private int if_local = 1;
    private FrameLayout placeholder;
    private TextView author;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);//设置屏幕不随手机旋转
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//设置屏幕不进入休眠
        initView();

        //打开或创建数据库
        db = openOrCreateDatabase(db_name, Context.MODE_PRIVATE, null);
        //创建数据表
        String createTable = "CREATE TABLE IF NOT EXISTS " + tb_name +//数据表名称
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +//索引字段
                "movie_name VARCHAR(20)," +         //视频名称
                "movie_uri text," +                  //视频地址
                "thumb_path text," +                  //缩略图地址
                "pos INTEGER)";                    //视频播放进度
        db.execSQL(createTable);//创建数据表
        cur = db.rawQuery("SELECT * FROM " + tb_name, null);//查询tb_name数据表中的所有数据

//打开播放历史
        history = (TextView) findViewById(R.id.history);
        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

//打开本地视频
        local = (TextView) findViewById(R.id.local);
        local.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, LocalVideoActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

//录制视频
        luzhi = (TextView) findViewById(R.id.luzhi);
        luzhi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 激活系统的照相机进行录像
                Intent i = new Intent();
                i.setAction("android.media.action.VIDEO_CAPTURE");
                i.addCategory("android.intent.category.DEFAULT");
                startActivityForResult(i, REQUEST_CODE);
            }
        });

        //网络视频
        network_video = (TextView) findViewById(R.id.network_video);
        network_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, NetworkVideo.class);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        //关于我们
        author = (TextView) findViewById(R.id.author);
        author.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this,AuthorActivity.class);
                startActivity(intent);
            }
        });

    }

    //处理其他页面传回数据
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 10 && requestCode == REQUEST_CODE) {//本地选取
            movie_name = data.getExtras().getString("movie_name");
            movie_uri = data.getExtras().getString("movie_uri");
            thumb_path = data.getExtras().getString("thumb_path");
            try {
                uri = Uri.parse(movie_uri);
                if_local = 1;
                startVv(uri, pos);
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "播放出错了！", Toast.LENGTH_SHORT).show();
            }
        }
        if (resultCode == 30 && requestCode == REQUEST_CODE) {//播放历史传回
            thumb_path = data.getExtras().getString("thumb_path");
            movie_uri = data.getExtras().getString("history_uri");
            String history_pos = data.getExtras().getString("history_pos");
            movie_name = data.getExtras().getString("movie_name");
            pos = Integer.parseInt(history_pos);
            uri = Uri.parse(movie_uri);
            startVv(uri, pos);
        }
        if (resultCode == 40 && requestCode == REQUEST_CODE) {//网络视频传回
            thumb_path = data.getExtras().getString("thumb_path");
            movie_uri = data.getExtras().getString("movie_uri");
            movie_name = data.getExtras().getString("movie_name");
            if_local = 0;//第一次传入区别本地视频
            uri = Uri.parse(movie_uri);
            startVv(uri, pos);
        }
    }

    /**
     * 初始化控件
     */
    private void initView() {
        mVideoLayout = findViewById(R.id.video_layout);
        mBottomLayout = findViewById(R.id.bottom_layout);
        mVideoView = (MyVideoView) findViewById(R.id.vv);
        mMediaController = (MyMediaController) findViewById(R.id.media_controller);

        //判断是否有读取权限
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)//判断是否有读取权限
                != PackageManager.PERMISSION_GRANTED) {
            //弹出权限授予对话框，提示用户授权
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
        }

        //把视频控制的按钮设置到播放器里
        mVideoView.setMediaController(mMediaController);
        //设置置视频区域大小
        setVideoAreaSize();
        //设置屏幕状态和播放状态的监听
        mVideoView.setVideoViewCallback(this);
        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
            }
        });
    }

    @Override
    protected void onPause() {
        onPause(mMediaPlayer);
        super.onPause();
    }

    /**
     * 设置置视频区域大小和播放地址
     */
    private void setVideoAreaSize() {
        mVideoLayout.post(new Runnable() {
            @Override
            public void run() {
                int width = mVideoLayout.getWidth();
                cachedHeight = (int) (width * 405f / 720f);
                ViewGroup.LayoutParams videoLayoutParams = mVideoLayout.getLayoutParams();
                videoLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                videoLayoutParams.height = cachedHeight;
                mVideoLayout.setLayoutParams(videoLayoutParams);
                mVideoView.setVideoURI(uri);
                mVideoView.requestFocus();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SEEK_POSITION_KEY, pos);
    }

    @Override
    protected void onRestoreInstanceState(Bundle outState) {
        super.onRestoreInstanceState(outState);
        pos = outState.getInt(SEEK_POSITION_KEY);
    }

    //半全屏切换
    @Override
    public void onScaleChange(boolean isFullscreen) {
        this.isFullscreen = isFullscreen;
        if (uri != null) {
            if (isFullscreen) {//切换成全屏
                ViewGroup.LayoutParams layoutParams = mVideoLayout.getLayoutParams();
                layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                mVideoLayout.setLayoutParams(layoutParams);
                mBottomLayout.setVisibility(View.GONE);
                title = (TextView) findViewById(R.id.title);
                title.setText(movie_name);
            } else {//切换成半屏
                ViewGroup.LayoutParams layoutParams = mVideoLayout.getLayoutParams();
                layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                layoutParams.height = this.cachedHeight;
                mVideoLayout.setLayoutParams(layoutParams);
                mBottomLayout.setVisibility(View.VISIBLE);
            }
            switchTitleBar(!isFullscreen);
        }
    }


    //调整状态栏是否隐藏
    private void switchTitleBar(boolean show) {
        android.support.v7.app.ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            if (show) {
                supportActionBar.show();
            } else {
                supportActionBar.hide();
            }
        }
    }

    /*
     * 暂停
     * */
    @Override
    public void onPause(MediaPlayer mediaPlayer) {
        if (if_local == 0) {
            pos = mVideoView.getCurrentPosition();//存储播放进度
            cur = db.rawQuery("SELECT * FROM tb_movie WHERE movie_name=?", new String[]{movie_name});
            if (cur.moveToFirst()) {
                int id = cur.getInt(cur.getColumnIndex("_id"));
                if (pos != 0)
                    update(movie_name, movie_uri, thumb_path, pos, id);
            } else {
                addData(movie_name, movie_uri, thumb_path, pos);
            }
        } else {
            if (uri != null) {
                pos = mVideoView.getCurrentPosition();//存储播放进度
                cur = db.rawQuery("SELECT * FROM tb_movie WHERE movie_name=?", new String[]{movie_name});
                if (cur.moveToFirst()) {
                    int id = cur.getInt(cur.getColumnIndex("_id"));
                    if (pos != 0)
                        update(movie_name, movie_uri, thumb_path, pos, id);
                } else {
                    addData(movie_name, movie_uri, thumb_path, pos);
                }
            }
        }
        mVideoView.pause();//暂停播放
    }

    @Override
    public void onStart(MediaPlayer mediaPlayer) {
    }

    @Override
    public void onBufferingStart(MediaPlayer mediaPlayer) {
    }

    @Override
    public void onBufferingEnd(MediaPlayer mediaPlayer) {
    }

    @Override
    public void onBackPressed() {
        if (this.isFullscreen) {
            mVideoView.setFullscreen(false);
        } else {
            super.onBackPressed();
        }
    }

    //记录添加
    private void addData(String movie_name, String movie_uri, String thumb_path, int pos) {
        ContentValues cv = new ContentValues(3);//创建包含3个数据项的对象
        cv.put("movie_name", movie_name);
        cv.put("movie_uri", movie_uri);
        cv.put("thumb_path", thumb_path);
        cv.put("pos", pos);
        db.insert(tb_name, null, cv);//将数据加到数据表
        cur = db.rawQuery("SELECT * FROM " + tb_name, null);//重新查询
        if (cur.getCount() != 0) {
            Log.d(Lag, "添加成功");
        } else
            Log.d(Lag, "添加失败");
    }

    //记录更新
    private void update(String movie_name, String movie_uri, String thumb_path, int pos, int id) {
        ContentValues cv = new ContentValues(3);//创建包含3个数据项的对象
        cv.put("movie_name", movie_name);
        cv.put("movie_uri", movie_uri);
        cv.put("thumb_path", thumb_path);
        cv.put("pos", pos);
        db.update(tb_name, cv, "_id=" + id, null);//更新_id所指的记录
    }

    //启动播放器
    private void startVv(Uri uri, int pos) {
        placeholder = (FrameLayout) findViewById(R.id.placeholder);//获取遮盖层
        placeholder.setVisibility(View.GONE);//隐藏
        mVideoView.setVideoURI(uri);
        mVideoView.seekTo(pos);
        mVideoView.start();
    }
}