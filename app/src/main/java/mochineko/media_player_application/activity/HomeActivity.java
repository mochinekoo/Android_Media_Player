package mochineko.media_player_application.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
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
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        enterPictureInPictureMode();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);

        VideoView videoView = findViewById(R.id.fragmentContainerView).findViewById(R.id.videoView);
        Log.d("HomeActivity", "videoView = " + videoView);
        ProgressBar progressBar = findViewById(R.id.progressBar);
        TextView currentText = findViewById(R.id.videoCurrentText);
        TextView durationText = findViewById(R.id.videoDurationText);

        if (requestCode == FILE_OPEN_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri;
            if (resultData != null) {
                uri = resultData.getData();

                videoView.post(() -> {
                    videoView.setVideoURI(uri);
                    videoView.setOnPreparedListener(mp -> videoView.start());
                    videoView.setOnErrorListener((mp, what, extra) -> {
                        Log.e("VideoView", "error what=" + what + " extra=" + extra);
                        return true;
                    });
                });

                Cursor cursor = getContentResolver().query(
                        uri,
                        new String[]{OpenableColumns.DISPLAY_NAME},
                        null,
                        null,
                        null
                );
                try (cursor) {
                    cursor.moveToFirst();
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
                    ((TextView)findViewById(R.id.nowVideoName_Text)).setText(name);
                }

            } else {
                uri = null;
            }

            Timer timer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    int videoCurrent = videoView.getCurrentPosition();
                    int videoDuration = videoView.getDuration();
                    if (videoDuration <= 0) {
                        return;
                    }
                    float percent = ((float) videoCurrent / videoDuration) * 100;
                    runOnUiThread(() -> {
                        progressBar.setProgress((int) percent);
                        currentText.setText(String.format("%02d:%02d", videoCurrent/1000/60, videoCurrent/1000%60));
                        durationText.setText(String.format("%02d:%02d", videoDuration/1000/60, videoDuration/1000%60));
                    });
                }
            };
            timer.schedule(task, 0L, 1000L);
        }

    }
}