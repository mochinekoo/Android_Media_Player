package mochineko.media_player_application.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Timer;
import java.util.TimerTask;

import mochineko.media_player_application.R;

public class HomeActivity extends AppCompatActivity {

    private static final int FILE_OPEN_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent fileIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        fileIntent.addCategory(Intent.CATEGORY_OPENABLE);
        fileIntent.setType("*/*");
        startActivityForResult(fileIntent, FILE_OPEN_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (requestCode == FILE_OPEN_CODE
                && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                VideoView videoView = findViewById(R.id.videoView);
                videoView.setVideoURI(uri);
                videoView.start();
            }

            Timer timer = new Timer();
            TimerTask task = new TimerTask() {
                private final VideoView videoView = findViewById(R.id.videoView);
                private final ProgressBar progressBar = findViewById(R.id.progressBar);
                @Override
                public void run() {
                    int videoCurrent = videoView.getCurrentPosition();
                    int videoDuration = videoView.getDuration();
                    float percent = ((float) videoCurrent / videoDuration) * 100;
                    progressBar.setProgress((int) percent);
                }
            };
            timer.schedule(task, 0L, 1000L);
        }
    }
}