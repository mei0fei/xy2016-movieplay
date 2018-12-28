package w.cn.movie;

public class EntityVideo {

    String thumb_path;//缩略图路径
    String movie_name;//视频名称
    String file_name;//文件名称
    String file_path;//文件路径
    String movie_uri;//视频地址
    String summary;//视频描述
    int duration;//视频时长//视频播放进度

    public EntityVideo(){
    }

    public EntityVideo(String thumb_path, String movie_name, String movie_uri, String summary, int duration) {
        this.thumb_path = thumb_path;
        this.movie_name = movie_name;
        this.movie_uri = movie_uri;
        this.summary = summary;
        this.duration = duration;
    }
    public EntityVideo(String thumb_path,String movie_name,String movie_uri,int duration){
        this.thumb_path=thumb_path;
        this.movie_name=movie_name;
        this.movie_uri=movie_uri;
        this.duration=duration;
    }
    public EntityVideo(String file_path,String file_name){
        this.file_path=file_path;
        this.file_name=file_name;
    }

    public void setThumb_path(String thumb_path) {
        this.thumb_path = thumb_path;
    }
    public void setMovie_name(String movie_name) {
        this.movie_name = movie_name;
    }
    public void setMovie_uri(String movie_uri) {
        this.movie_uri = movie_uri;
    }
    public void setDuration(int duration) {
        this.duration = duration;
    }
    public String getMovie_name() {
        return movie_name;
    }
    public String getMovie_uri() {
        return movie_uri;
    }
    public int getDuration() {
        return duration;
    }
    public String getThumb_path() {
        return thumb_path;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public void setFile_path(String file_path) {
        this.file_path = file_path;
    }

    public String getFile_name() {

        return file_name;
    }

    public String getFile_path() {
        return file_path;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getSummary() {

        return summary;
    }
}
