package com.example.calendardiary;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CalendarView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pedro.library.AutoPermissions;
import com.pedro.library.AutoPermissionsListener;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.Month;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements AutoPermissionsListener {

    static ArrayList<String> dayList, sList;
    MyAdapter adapter;

    static String selectYear,selectMonth,selectDay;

    CalendarView calView1;
    RecyclerView rView;
    String dateName;
    MyDBHelper myDB;
    SQLiteDatabase sqlDB;
    Intent intent;
    Cursor cursor;
    boolean pCheck = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AutoPermissions.Companion.loadAllPermissions(this, 101);
        calView1 = findViewById(R.id.calView1);
        rView = findViewById(R.id.rView);
        myDB = new MyDBHelper(this); //DB와 테이블 생성
        sqlDB = myDB.getReadableDatabase();


        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat curYearFormat = new SimpleDateFormat("yyyy", Locale.KOREA);
        SimpleDateFormat curMonthFormat = new SimpleDateFormat("MM", Locale.KOREA);

        selectYear = String.valueOf(curYearFormat.format(date));
        selectMonth=String.valueOf(curMonthFormat.format(date));


        calView1.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                if (pCheck) {
                    selectYear = String.valueOf(year);
                    if(month+1<10){
                        selectMonth = 0+String.valueOf(month + 1);
                    }
                    else{
                        selectMonth = String.valueOf(month + 1);
                    }
                    if(dayOfMonth<10){
                        selectDay = 0+String.valueOf(dayOfMonth);
                    }
                    else{
                        selectDay = String.valueOf(dayOfMonth);
                    }
                    dateName = selectYear + "년 " + selectMonth + "월 " + selectDay + "일";
                    cursor = sqlDB.rawQuery("SELECT * FROM myCalDiaryTBL WHERE dDate='" + dateName + "';", null);

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle(selectYear + "년 " + selectMonth + "월 " + selectDay + "일");
                    builder.setMessage("해당 날짜의 다이어리 페이지로 이동하시겠습니까?");
                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (cursor.moveToFirst()) {
                                intent = new Intent(getApplicationContext(), DiaryActivity2.class);
                            } else {
                                intent = new Intent(getApplicationContext(), DiaryActivity.class);
                            }
                            intent.putExtra("Year", selectYear);
                            intent.putExtra("Month", selectMonth);
                            intent.putExtra("Day", selectDay);
                            intent.putExtra("DateName", dateName);
                            intent.putExtra("PCheck", pCheck);
                            startActivity(intent);
                        }
                    });
                    builder.setNegativeButton("취소", null);
                    builder.setCancelable(true);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    adapterList(selectYear,selectMonth);

                } else {
                    showToast("권한 거부로 다이어리를 작성할 수 없습니다.");
                }

            }
        });

    }
    //onCreate 메소드 끝

    @Override
    protected void onResume() {
        super.onResume();
        adapterList(selectYear,selectMonth);
    }


    void showToast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        AutoPermissions.Companion.parsePermissions(this, requestCode, permissions, this);
        if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
            showToast("권한을 거부하였습니다.");
            pCheck = false;
        } else {
            pCheck = true;
        }
    }


    @Override
    public void onDenied(int i, String[] strings) {

    }

    @Override
    public void onGranted(int i, String[] strings) {

    }


    //DB클래스
    public class MyDBHelper extends SQLiteOpenHelper {

        public MyDBHelper(@Nullable Context context) {
            super(context, "myCalDiaryDB.db", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE myCalDiaryTBL(dDate TEXT PRIMARY KEY,dDiary TEXT,dPic TEXT, dMusic TEXT);");
            db.execSQL("CREATE TABLE toDoListTBL(num INTEGER PRIMARY KEY AUTOINCREMENT, dDate TEXT, dSchedule TEXT)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }

    }

    //어댑터 클래스
    public void adapterList(String aYear, String aMonth){
        Cursor cursor1 = sqlDB.rawQuery("SELECT DISTINCT dDate FROM toDoListTBL WHERE dDate LIKE '"
                + aYear + "년 " + aMonth + "월%" + "' ORDER BY dDate;", null);
        dayList = new ArrayList<String>();
        sList = new ArrayList<String>();
        String listStr = "";

        if (cursor1.moveToFirst()) {
            for (int i = 0; i < cursor1.getCount(); i++) {
                dayList.add(cursor1.getString(0));
                cursor1.moveToNext();
            }
        }


        for(int i=1;i<32;i++){
            String k;
            if(i<10){
                k=0+String.valueOf(i);
            }
            else{
                k=String.valueOf(i);
            }
            Cursor cursor2=sqlDB.rawQuery("SELECT dDate, dSchedule FROM toDoListTBL WHERE dDate='"
                    +aYear + "년 " + aMonth + "월 " + k + "일"+"' ORDER BY dDate;",null);
            if(cursor2.moveToFirst()){
                for(int j=0;j<cursor2.getCount();j++){
                    cursor2.moveToPosition(j);
                    listStr+=cursor2.getString(1)+"\n";
                }
                sList.add(listStr);
                listStr="";
            }
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rView.setLayoutManager(layoutManager);
        adapter = new MyAdapter();
        for (int i = 0; i < dayList.size(); i++) {
            adapter.addItem(new ItemData(dayList.get(i),sList.get(i)));
        }
        adapter.notifyDataSetChanged();
        rView.setAdapter(adapter);
    }




}