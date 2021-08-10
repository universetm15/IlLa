package com.example.calendardiary;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class DiaryActivity2 extends AppCompatActivity {
    LinearLayout lPlay;
    SeekBar sBarMp3;
    TextView txtSchedule2,txtMusic2,txtDiary2,txtTime;
    Button btnChange2,btnBack2;
    ListView lSchedule2;
    ImageView imgPhoto2,imgPlay,imgStop;
    String selectedYear,selectedMonth,selectedDay;
    String dateName;
    String mp3Name;
    String mp3Path;
    SQLiteDatabase sqlDB;
    boolean pCheck=true;
    int playCheck=0;
    MediaPlayer mp;

    ArrayList<String> todoList;
    ArrayAdapter<String> adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary2);
        txtSchedule2=findViewById(R.id.txtSchedule2);
        txtMusic2=findViewById(R.id.txtMusic2);
        txtTime=findViewById(R.id.txtTime);
        lSchedule2=findViewById(R.id.lSchedule2);
        txtDiary2=findViewById(R.id.txtDiary2);
        imgPhoto2=findViewById(R.id.imgPhoto2);
        imgPlay=findViewById(R.id.imgPlay);
        imgStop=findViewById(R.id.imgStop);
        btnChange2=findViewById(R.id.btnChange2);
        btnBack2=findViewById(R.id.btnBack2);
        sBarMp3=findViewById(R.id.sBarMp3);
        lPlay=findViewById(R.id.lPlay);

        Intent intent=getIntent();
        selectedYear=intent.getStringExtra("Year");
        selectedMonth=intent.getStringExtra("Month");
        selectedDay=intent.getStringExtra("Day");
        dateName=intent.getStringExtra("DateName");
        pCheck=intent.getBooleanExtra("PCheck",true);

        ActionBar bar=getSupportActionBar();
        bar.setTitle(selectedYear+"년 "+selectedMonth+"월 "+selectedDay+"일");

        sqlDB=SQLiteDatabase.openDatabase("/data/data/com.example.calendardiary/databases/myCalDiaryDB.db",null,SQLiteDatabase.OPEN_READWRITE);

        Cursor cursor=sqlDB.rawQuery("SELECT * FROM myCalDiaryTBL WHERE dDate='"+dateName+"';",null);
        Cursor cursor1=sqlDB.rawQuery("SELECT * FROM toDoListTBL WHERE dDate='"+dateName+"';",null);
        readDiary(cursor);
        readSchedule(cursor1);

        adapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,todoList);
        lSchedule2.setAdapter(adapter);
        setListViewHeight(adapter,lSchedule2);
        imgPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(pCheck){
                    if(playCheck==0){
                        imgPlay.setImageResource(R.drawable.pause);
                        mp=new MediaPlayer();
                        try {
                            mp.setDataSource(mp3Path);
                            mp.prepare();
                            mp.start();
                            ThreadProcess();
                        } catch (IOException e) {
                            showToast("음악을 재생할 수 없습니다.");
                        }
                        playCheck+=1;
                    }
                    else if(playCheck%2==1){
                        imgPlay.setImageResource(R.drawable.play);
                        mp.pause();
                        playCheck+=1;
                    }
                    else if(playCheck%2==0){
                        mp.start();
                        ThreadProcess();
                        imgPlay.setImageResource(R.drawable.pause);
                        playCheck+=1;
                    }
                }
                else {
                    showToast("권한을 거부하여 음악을 이용할 수 없습니다.");
                }
            }
        });
        imgStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(pCheck){
                    mp.stop();
                    mp.reset();
                    playCheck=0;
                    sBarMp3.setProgress(0);
                    txtTime.setText("00:00");
                    imgPlay.setImageResource(R.drawable.play);
                    ThreadProcess();
                }
                else{
                    showToast("권한을 거부하여 음악을 이용할 수 없습니다.");
                }
            }
        });
        btnChange2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(getApplicationContext(), DiaryActivity.class);
                intent1.putExtra("Year", selectedYear);
                intent1.putExtra("Month", selectedMonth);
                intent1.putExtra("Day", selectedDay);
                intent1.putExtra("DateName",dateName);
                intent1.putExtra("PCheck",pCheck);
                startActivity(intent1);
                finish();
            }

        });

        //시크바
        sBarMp3.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    mp.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        btnBack2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }//onCreate 메소드 끝

    void showToast(String msg){
        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
    }


    public String getURLForResource(int resld) {
        return Uri.parse("android.resource://" + R.class.getPackage().getName() + "/" + resld).toString();
    }

    public void readDiary(Cursor cursor) {
        if(pCheck){
            if(cursor.moveToFirst()){
                if (cursor.getString(1) == null || cursor.getString(1).trim().equals("")) {
                    txtDiary2.setText("");
                } else {
                    txtDiary2.setText(cursor.getString(1));
                }
                if (cursor.getString(2) == null || cursor.getString(2).trim().equals("")) {
                    imgPhoto2.setImageURI(Uri.parse(getURLForResource(R.drawable.camera2)));
                } else {
                    imgPhoto2.setImageURI(Uri.parse(cursor.getString(2)));
                }
                if (cursor.getString(3).equals("null") || cursor.getString(3).trim().equals("")) {
                    txtMusic2.setText("");
                    lPlay.setVisibility(View.GONE);
                } else {
                    mp3Path=cursor.getString(3);
                    File file=new File(mp3Path);
                    mp3Name=file.getName();
                    txtMusic2.setText(mp3Name);
                    lPlay.setVisibility(View.VISIBLE);
                }
            }
        }
        else {
            showToast("권한을 거부하여 저장한 정보를 읽을 수 없습니다.");
        }

    }
    public void readSchedule(Cursor cursor){
        if(pCheck){
            todoList=new ArrayList<String>();
            if(cursor.moveToFirst()){
                int cCount=cursor.getCount();
                for(int i=0;i<=cCount;i++){
                    todoList.add(cursor.getString(2));
                    if(!cursor.moveToNext()){
                        break;
                    }
                    cursor.moveToPrevious();
                    cursor.moveToNext();
                }
            }
        }
        else {
            showToast("권한을 거부하여 일정을 읽을 수 없습니다.");
        }
    }
    public void setListViewHeight(ArrayAdapter adapter,ListView listView){
        int totalHeight=0;
        int desiredWidth= View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.AT_MOST);

        for(int size=0;size<listView.getCount();size++){
            View listItem=adapter.getView(size,null,listView);
            listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight+=listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params=listView.getLayoutParams();
        params.height=totalHeight+(listView.getDividerHeight()*(listView.getCount()-1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }
    void ThreadProcess(){
        SimpleDateFormat timeFormat=new SimpleDateFormat("mm:ss");
        new Thread(){
            @Override
            public void run() {
                if(mp!=null){
                    sBarMp3.setMax(mp.getDuration());
                    while(mp.isPlaying()){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                sBarMp3.setProgress(mp.getCurrentPosition());
                                txtTime.setText(timeFormat.format(mp.getCurrentPosition()));
                            }
                        });
                        SystemClock.sleep(100);
                    }
                }
            }
        }.start();
    }
}

