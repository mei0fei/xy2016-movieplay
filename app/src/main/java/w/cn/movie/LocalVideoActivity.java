package w.cn.movie;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TooManyListenersException;

public class LocalVideoActivity extends AppCompatActivity {

    final String Lag = LocalVideoActivity.class.getSimpleName();
    List<EntityVideo> sysVideoList;
    Cursor cursor;//存放查询结果的Cursor对象
    Uri uri;
    boolean isVideo = false;        //记录是否为视频文件
    private List<EntityVideo> entityVideoList = new ArrayList<>();
    Button folder, search;
    EditText edt1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_video);

        sysVideoList = getList(this);
        addData(sysVideoList);//添加数据
        EntityVideoAdapter adapter = new EntityVideoAdapter(LocalVideoActivity.this, R.layout.item2, entityVideoList, 2);
        ListView lv2 = (ListView) findViewById(R.id.lv2);
        lv2.setAdapter(adapter);
        lv2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {//点击列表中视频传回播放
                EntityVideo entityVideo = entityVideoList.get(i);
                Intent intent = new Intent();
                intent.putExtra("movie_name", entityVideo.getMovie_name());
                intent.putExtra("movie_uri", entityVideo.getMovie_uri());
                intent.putExtra("thumb_path", entityVideo.getThumb_path());
                setResult(10, intent);
                finish();
            }
        });

        //文件搜索
        search = (Button) findViewById(R.id.search);
        edt1 = (EditText) findViewById(R.id.edt1);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text1 = edt1.getText().toString();
                if (text1.length() != 0) {
                    sysVideoList = searchKeyWord(LocalVideoActivity.this, text1);
                    addData2(sysVideoList);
                    EntityVideoAdapter adapter = new EntityVideoAdapter(LocalVideoActivity.this, R.layout.item3, entityVideoList, 3);
                    ListView lv2 = (ListView) findViewById(R.id.lv2);
                    lv2.setAdapter(adapter);
                    lv2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {//点击列表中视频传回播放
                            try {
                                EntityVideo entityVideo = entityVideoList.get(i);
                                File file = new File(entityVideo.getFile_path());
                                if (file.isDirectory()) {
                                    Toast.makeText(LocalVideoActivity.this, "文件夹不可播放", Toast.LENGTH_SHORT).show();
                                } else {//为文件
                                    Intent intent = new Intent();
                                    intent.putExtra("file_path", entityVideo.getFile_path());
                                    setResult(10, intent);
                                    finish();
                                }
                            } catch (Exception e) {
                                Toast.makeText(LocalVideoActivity.this, "文件打开错误！", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } else
                    Toast.makeText(LocalVideoActivity.this, "请输入查找条件！", Toast.LENGTH_LONG).show();
            }
        });

        //打开本地文件夹
        folder = (Button) findViewById(R.id.folder);
        folder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);//创建动作为“选取内容”的Intent
                i.setType("video/*");//要选取的所有视频类型
                startActivityForResult(i, 101);//以识别编号101来启动外部程序
            }
        });
    }

    //获取本地视频文件信息
    public List<EntityVideo> getList(Context context) {
        List<EntityVideo> sysVideoList = new ArrayList<>();
        // MediaStore.Video.Thumbnails.DATA:视频缩略图的文件路径
        String[] thumbColumns = {MediaStore.Video.Thumbnails.DATA, MediaStore.Video.Thumbnails.VIDEO_ID};
        // 视频其他信息的查询条件
        String[] mediaColumns = {MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DATA, MediaStore.Video.Media.DISPLAY_NAME, MediaStore.Video.Media.DURATION};

        cursor = context.getContentResolver().query(MediaStore.Video.Media
                        .EXTERNAL_CONTENT_URI,
                mediaColumns, null, null, null);
        if (cursor == null) {
            return sysVideoList;
        }
        if (cursor.moveToFirst()) {
            do {
                EntityVideo info = new EntityVideo();
                int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media._ID));
                Cursor thumbCursor = context.getContentResolver().query(MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI,
                        thumbColumns, MediaStore.Video.Thumbnails.VIDEO_ID
                                + "=" + id, null, null);
                if (thumbCursor.moveToFirst()) {
                    info.setThumb_path(thumbCursor.getString(thumbCursor.getColumnIndex(MediaStore.Video.Thumbnails.DATA)));//视频缩略图
                }
                info.setMovie_name(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)));//视频名称
                info.setMovie_uri(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)));//视频地址
                info.setDuration(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)));//视频时长
                sysVideoList.add(info);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return sysVideoList;
    }

    //添加数据
    private void addData(List<EntityVideo> List) {
        for (int i = 0; i < List.size(); i++) {
            EntityVideo entityVideo1 = new EntityVideo(List.get(i).thumb_path, List.get(i).movie_name, List.get(i).movie_uri, List.get(i).duration);
            entityVideoList.add(entityVideo1);
        }
    }

    private void addData2(List<EntityVideo> List) {
        for (int i = 0; i < List.size(); i++) {
            EntityVideo entityVideo1 = new EntityVideo(List.get(i).file_path, List.get(i).file_name);
            entityVideoList.add(entityVideo1);
        }
    }

    //处理通过文件夹选取的文件
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {//如果选取成功
            isVideo = (requestCode == 101);     //记录是否选取了视频文件（当识别码为101时）
            uri = convertUri(data.getData());      //获取选取文件的Uri并进行Uri格式转换
            String movie_uri = uri.getPath();
            Intent intent = new Intent(LocalVideoActivity.this, MainActivity.class);
            intent.putExtra("movie_uri", movie_uri);
            intent.putExtra("code", 10);
            setResult(10, intent);
            finish();
            Log.d(Lag, "本地视频提取成功");
        }
    }

    //处理选区的视频文件
    private Uri convertUri(Uri uri) {//将“convertUri”类型的Uri转换为“file：//的Uri”
        if (uri.toString().substring(0, 7).equals("content")) {//如果是以“content”开头
            String[] colName = {MediaStore.MediaColumns.DATA};  //声明要查询的字段
            Cursor cursor = getContentResolver().query(uri, colName, null, null, null);//以uri进行查询
            cursor.moveToFirst();           //移到查询结果的第一个目录
            uri = Uri.parse("file://" + cursor.getString(0));//将路径转换为Uri
            cursor.close();     //关闭查询结果+
        }
        return uri;//返回uri对象
    }

    //搜索本地文件
    private List<EntityVideo> searchKeyWord(Context context, String keyword) {
        List fileList = new ArrayList<>();
        ContentResolver resolver = context.getContentResolver();
        Uri uri = MediaStore.Files.getContentUri("external");
        Cursor cursor = resolver.query(uri,
                new String[]{MediaStore.Files.FileColumns.DATA, MediaStore.Files.FileColumns.SIZE},
                MediaStore.Files.FileColumns.TITLE + " LIKE '%" + keyword + "%'",
                null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                EntityVideo entityVideo = new EntityVideo();
                String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA));
                entityVideo.setFile_path(path);
                entityVideo.setFile_name(path.substring(path.lastIndexOf("/") + 1));
                fileList.add(entityVideo);
            }
        }
        cursor.close();
        return fileList;
    }
}
