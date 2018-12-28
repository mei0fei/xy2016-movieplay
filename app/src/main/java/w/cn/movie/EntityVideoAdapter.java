package w.cn.movie;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.Formatter;
import java.util.List;
import java.util.Locale;

public class EntityVideoAdapter extends ArrayAdapter<EntityVideo> {
    private int resourceId;
    int judge;
    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;
    private Context mContext;

    public EntityVideoAdapter(Context context, int Id, List<EntityVideo> objects, int judge) {
        super(context, Id, objects);
        resourceId = Id;
        this.judge = judge;
        this.mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        EntityVideo entityVideo = getItem(position);//获取当前项的实例
        View view = null;
        ViewHolder1 viewHolder1;
        ViewHolder2 viewHolder2;
        ViewHolder3 viewHolder3;
        ViewHolder4 viewHolder4;
        switch (judge) {
            case 1://历史纪录适配器
                if (convertView == null) {
                    view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
                    viewHolder1 = new ViewHolder1();
                    viewHolder1.thumbnail1 = (ImageView) view.findViewById(R.id.thumbnail1);
                    viewHolder1.movie_name1 = (TextView) view.findViewById(R.id.movie_name1);
                    viewHolder1.pos1 = (TextView) view.findViewById(R.id.pos1);
                    view.setTag(viewHolder1);//将ViewHolder 存储在view中
                } else {
                    view = convertView;
                    viewHolder1 = (ViewHolder1) view.getTag();//重新获取viewholder
                }
                Glide.with(mContext)//加载本地缩略图
                        .load(entityVideo.getThumb_path()) //加载地址
                        .placeholder(R.drawable.video_icon)//加载未完成时显示占位图
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(viewHolder1.thumbnail1);//显示的位置
                viewHolder1.movie_name1.setText(entityVideo.getMovie_name());
                viewHolder1.pos1.setText(stringForTime(entityVideo.getDuration()));
                break;
            case 2://本地文件遍历适配器
                if (convertView == null) {
                    view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
                    viewHolder2 = new ViewHolder2();
                    viewHolder2.thumbnail2 = (ImageView) view.findViewById(R.id.thumbnail2);
                    viewHolder2.movie_name2 = (TextView) view.findViewById(R.id.movie_name2);
                    viewHolder2.duration2 = (TextView) view.findViewById(R.id.duration2);
                    view.setTag(viewHolder2);//将ViewHolder 存储在view中
                } else {
                    view = convertView;
                    viewHolder2 = (ViewHolder2) view.getTag();//重新获取viewholder
                }
                Glide.with(mContext)//加载本地缩略图
                        .load(entityVideo.getThumb_path()) //加载地址
                        .placeholder(R.drawable.video_icon)//加载未完成时显示占位图
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(viewHolder2.thumbnail2);//显示的位置
                viewHolder2.movie_name2.setText(entityVideo.getMovie_name());
                viewHolder2.duration2.setText(stringForTime(entityVideo.getDuration()));
                break;
            case 3://文件搜索适配器
                if (convertView == null) {
                    view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
                    viewHolder3 = new ViewHolder3();
                    viewHolder3.file_name = (TextView) view.findViewById(R.id.file_name);
                    viewHolder3.file_path = (TextView) view.findViewById(R.id.file_path);
                    view.setTag(viewHolder3);
                } else {
                    view = convertView;
                    viewHolder3 = (ViewHolder3) view.getTag();
                }
                viewHolder3.file_name.setText(entityVideo.getFile_name());
                viewHolder3.file_path.setText(entityVideo.getFile_path());
                break;
            case 4://加载网络数据列表
                if (convertView == null) {
                    view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
                    viewHolder4 = new ViewHolder4();
                    viewHolder4.thumbnail4 = (ImageView) view.findViewById(R.id.thumbnail4);
                    viewHolder4.movie_name4 = (TextView) view.findViewById(R.id.movie_name4);
                    viewHolder4.summary4 = (TextView) view.findViewById(R.id.summary4);
                    viewHolder4.duration4 = (TextView) view.findViewById(R.id.duration4);
                    view.setTag(viewHolder4);//将ViewHolder 存储在view中
                } else {
                    view = convertView;
                    viewHolder4 = (ViewHolder4) view.getTag();//重新获取viewholder
                }
                Glide.with(mContext)//加载网络缩略图
                        .load(entityVideo.getThumb_path()) //加载地址
                        .placeholder(R.drawable.video_icon)//加载未完成时显示占位图
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(viewHolder4.thumbnail4);//显示的位置
                viewHolder4.movie_name4.setText(entityVideo.getMovie_name());
                viewHolder4.summary4.setText(entityVideo.getSummary());
                viewHolder4.duration4.setText(stringForTime(entityVideo.getDuration()));
                break;
        }
        return view;
    }

    private class ViewHolder1 {//播放历史
        ImageView thumbnail1;
        TextView movie_name1;
        TextView pos1;
    }

    private class ViewHolder2 {//本地视频
        ImageView thumbnail2;
        TextView movie_name2;
        TextView duration2;
    }

    private class ViewHolder3 {//文件
        TextView file_name;
        TextView file_path;
    }

    private class ViewHolder4 {//网络视频
        ImageView thumbnail4;
        TextView movie_name4;
        TextView summary4;
        TextView duration4;

    }


    //时间转换
    public String stringForTime(int timeMs) {
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
        int totalSeconds = timeMs / 1000;
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

}
