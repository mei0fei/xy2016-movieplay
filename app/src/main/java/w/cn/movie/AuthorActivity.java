package w.cn.movie;

import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class AuthorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_author);
       /* TextView text=(TextView)findViewById(R.id.text);
        Typeface typeface=Typeface.createFromAsset(getAssets(),"ZT.ttf");
        text.setTypeface(typeface);//引入字体库*/
    }
}
