package com.example.videos_in_sqlite;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.VideoView;

import java.lang.reflect.Field;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    VideoView Video_viewer;
    DBHelper dbhelper;
    Cursor cursor;
    SimpleCursorAdapter sca;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = this.findViewById(R.id.listview);
        Video_viewer = this.findViewById(R.id.videoView4);

        dbhelper = new DBHelper(this);
        addVideosFromRawResourceToDB();
    }

    @Override
    protected void onDestroy() {
        cursor.close();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        manageListView();
    }
    private void manageListView() {
        cursor = dbhelper.getVideos();


        if (sca == null) {

            sca = new SimpleCursorAdapter(
                    this,
                    android.R.layout.simple_list_item_1,
                    cursor,
                    new String[]{DBHelper.COL_VIDEO_PATH},
                    new int[]{android.R.id.text1},
                    0
            );
            listView.setAdapter(sca);
            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    dbhelper.deleteVideoFromDB(id);
                    manageListView(); // <<<<<<<<<< refresh the ListView as data has changed
                    return true;
                }

            });
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    setCurrentVideo(cursor.getString(cursor.getColumnIndex(DBHelper.COL_VIDEO_PATH)));
                }
            });
        } else {
            sca.swapCursor(cursor);
        }
    }

    private void setCurrentVideo(String path) {

        Video_viewer.setVideoURI(
                Uri.parse(
                        "android.resource://" + getPackageName() + "/" + String.valueOf(
                                getResources().getIdentifier(
                                        path,
                                        "raw",
                                        getPackageName())
                        )
                )
        );
        Video_viewer.start();
    }

    private void addVideosFromRawResourceToDB() {
        Field[] fields=R.raw.class.getFields();
        for(int count=0; count < fields.length; count++){
            Log.i("Raw Asset: ", fields[count].getName());
            dbhelper.addVideo(fields[count].getName());
        }
    }
}