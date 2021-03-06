package com.example.calendardiary;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseBooleanArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class DiaryActivity extends AppCompatActivity {
    TextView txtNum,txtMusic;
    Button btnAdd,btnSave,btnBack,btnDelete;
    ListView lSchedule;
    EditText edtDiary,edtSchedule;
    ImageView imgPhoto;
    ScrollView sView1;

    String selectedYear,selectedMonth,selectedDay;
    String mp3Path=null;
    String dateName;
    String mp3Name;
    String picPath;
    String picName;
    String pPicPath;
    String pMp3Path;
    boolean pCheck;

    SQLiteDatabase sqlDB;

    ArrayList<String> todoList;
    ArrayAdapter<String> adapter;
    Bitmap picBitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary);
        txtNum=findViewById(R.id.txtNum);
        txtMusic=findViewById(R.id.txtMusic);
        btnAdd=findViewById(R.id.btnAdd);
        btnSave=findViewById(R.id.btnSave);
        btnBack=findViewById(R.id.btnBack);
        btnDelete=findViewById(R.id.btnDelete);
        lSchedule=findViewById(R.id.lSchedule);
        edtDiary=findViewById(R.id.edtDiary);
        edtSchedule=findViewById(R.id.edtSchdule);
        imgPhoto=findViewById(R.id.imgPhoto);
        sView1=findViewById(R.id.sView1);

        Intent intent=getIntent();
        selectedYear=intent.getStringExtra("Year");
        selectedMonth=intent.getStringExtra("Month");
        selectedDay=intent.getStringExtra("Day");
        dateName=intent.getStringExtra("DateName");
        pCheck=intent.getBooleanExtra("PCheck",true);



        ActionBar bar=getSupportActionBar();
        bar.setTitle(selectedYear+"??? "+selectedMonth+"??? "+selectedDay+"??? ???????????? ??????");

        sqlDB=SQLiteDatabase.openDatabase("/data/data/com.example.calendardiary/databases/myCalDiaryDB.db",null,SQLiteDatabase.OPEN_READWRITE);

        Cursor cursor=sqlDB.rawQuery("SELECT * FROM myCalDiaryTBL WHERE dDate='"+dateName+"';",null);
        Cursor cursor1=sqlDB.rawQuery("SELECT * FROM toDoListTBL WHERE dDate='"+dateName+"';",null);
        readDiary(cursor);
        readSchedule(cursor1);

        adapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice,todoList);

        lSchedule.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        lSchedule.setAdapter(adapter);

        setListViewHeight(adapter,lSchedule);

        lSchedule.setOnTouchListener(new View.OnTouchListener() { //????????????
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                sView1.requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });


        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DiaryActivity.this);
                builder.setTitle("???????????? ?????? ??????");
                builder.setMessage("?????? ????????? ???????????? ????????? ???????????? ??????\n?????????????????????????");
                builder.setNegativeButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                builder.setPositiveButton("??????", null);
                builder.setCancelable(true);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        //?????? ??????
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!pCheck){
                    showToast("?????? ????????? ????????? ????????? ??? ????????????.");
                }
                else {
                    String schedule=edtSchedule.getText().toString();
                    if(!schedule.equals("") && schedule!=null){
                        Cursor cursor2=sqlDB.rawQuery("SELECT * FROM toDoListTBL WHERE dDate='"+dateName+"'AND dSchedule='"+schedule+"';",null);
                        if(cursor2.moveToFirst()){
                            showToast("?????? ?????? ???????????? ??????????????????. ?????? ???????????? ??????????????????.");
                            edtSchedule.setText("");

                        }
                        else{
                            todoList.add(schedule);
                            sqlDB.execSQL("INSERT INTO toDoListTBL (dDate, dSchedule) VALUES('"+dateName+"','"+schedule+"');");
                            adapter.notifyDataSetChanged();
                            showToast("????????? ??????????????????.");
                        }
                    }
                    else{
                        showToast("??? ?????? ??????????????????.");
                    }
                    edtSchedule.setText("");
                }
            }
        });
        //?????? ??????
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!pCheck){
                    showToast("????????? ???????????? ????????? ????????? ??? ????????????.");
                }
                else{
                    SparseBooleanArray checkedItems=lSchedule.getCheckedItemPositions();
                    int count=adapter.getCount();
                    int fCount=0;
                    for(int i=0;i<count;i++){
                        if(!checkedItems.get(i)){
                            fCount+=1;
                        }
                    }
                    if(fCount==count){
                        showToast("????????? ????????? ????????????.");
                    }
                    else{
                        if(count!=0){
                            AlertDialog.Builder builder=new AlertDialog.Builder(DiaryActivity.this);
                            builder.setTitle("?????? ?????? ??????");
                            builder.setMessage("????????? ????????? ?????????????????????????");
                            builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    for(int i=count-1;i>=0;i--){
                                        if(checkedItems.get(i)){
                                            String removeStr=todoList.get(i).toString();
                                            todoList.remove(i);
                                            sqlDB.execSQL("DELETE FROM toDoListTBL WHERE dDate='"+dateName+"' AND dSchedule='"+removeStr+"';");
                                        }
                                    }
                                    lSchedule.clearChoices();
                                    adapter.notifyDataSetChanged();
                                }
                            });
                            builder.setNegativeButton("??????",null);
                            AlertDialog dialog=builder.create();
                            dialog.show();
                        }
                        else{
                            showToast("????????? ????????? ????????????.");
                        }
                    }
                }

            }
        });



        //Diary ????????? ?????? ??????
        TextWatcher watcher=new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                byte[] bytes=null;
                    try {
                        bytes=s.toString().getBytes("KSC5601");
                        int strCount=bytes.length;
                        txtNum.setText(strCount+"/200");
                    } catch (UnsupportedEncodingException e) {
                        showToast("?????? ?????? ???????????????.");
                    }

            }

            @Override
            public void afterTextChanged(Editable s) {
                String str=s.toString();
                try {
                    byte[] strBytes=str.getBytes("KSC5601");
                    if(strBytes.length>200){
                        s.delete(s.length()-2,s.length()-1);
                    }
                } catch (UnsupportedEncodingException e) {
                    showToast("?????? ?????? ???????????????.");
                }
            }
        };
        edtDiary.addTextChangedListener(watcher);

        //??????????????? ?????? ???????????? ??????
        imgPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!pCheck){
                    showToast("?????? ????????? ????????? ????????? ??? ????????????.");
                }
                else {
                    Intent intent=new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(intent,30);
                }
            }
        });

        //?????? ?????? ???????????? ??????
        txtMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!pCheck){
                    showToast("?????? ????????? ????????? ????????? ??? ????????????.");
                }
                else{
                    Intent intent=new Intent();
                    intent.setType("audio/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(intent,50);
                }

            }
        });



        //??? ?????? ?????? ??????
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pCheck) {
                    if(cursor.moveToFirst()){
                        pPicPath=cursor.getString(2);
                        pMp3Path=cursor.getString(3);
                    }
                    if(!cursor.moveToFirst()){
                        sqlDB.execSQL("INSERT INTO myCalDiaryTBL VALUES('"+dateName+"','"+edtDiary.getText().toString()+"','"+picPath+"','"+mp3Path+"');");
                        showToast("????????? ?????????????????????.");
                    }
                    else{
                        if(!pPicPath.equals("android.resource://com.example.calendardiary/2131165280") && picPath.equals("android.resource://com.example.calendardiary/2131165280")){
                            picPath=pPicPath;
                        }
                        if(!pMp3Path.equals("null") && mp3Path==null){
                            mp3Path=pMp3Path;
                        }
                        sqlDB.execSQL("UPDATE myCalDiaryTBL SET dDiary='"+edtDiary.getText().toString()+"', dPic='"+picPath+"', dMusic='"+mp3Path+"' WHERE dDate='"+dateName+"';");
                        showToast("????????? ?????????????????????.");
                    }
                    Intent intent1 = new Intent(getApplicationContext(), DiaryActivity2.class);
                    intent1.putExtra("Year", selectedYear);
                    intent1.putExtra("Month", selectedMonth);
                    intent1.putExtra("Day", selectedDay);
                    intent1.putExtra("DateName",dateName);
                    intent1.putExtra("PCheck",pCheck);
                    startActivity(intent1);
                    finish();

                }
                else {
                    showToast("????????? ???????????? ????????? ????????? ??? ????????????.");
                }
            }
        });
    }//onCreate ????????? ???

    void showToast(String msg){
        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==30 && resultCode==RESULT_OK){
            try{

                Uri imgUri=data.getData();
                Bitmap bitmap=MediaStore.Images.Media.getBitmap(this.getContentResolver(),imgUri);
                picBitmap=bitmap;
                picPath=getRealPathFromURI(imgUri);

                File file=new File(picPath);
                String fileName=file.getName();
                picName=fileName;
                imgPhoto.setImageBitmap(bitmap);
            }catch(Exception e){
            }
        }
        else if(requestCode==50 && resultCode==RESULT_OK){
            Uri audioFileUri=data.getData();
            mp3Path=getRealPathFromURI(audioFileUri);
            File file=new File(mp3Path);
            String fileName=file.getName();
            mp3Name=fileName;
            txtMusic.setText(mp3Name);
        }
    }


    private String getRealPathFromURI(Uri contentUri){
        if(contentUri.getPath().startsWith("/storage")){
            return contentUri.getPath();
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            String id= DocumentsContract.getDocumentId(contentUri).split(":")[1];
            String[] columns={MediaStore.Files.FileColumns.DATA};
            String selection=MediaStore.Files.FileColumns._ID+"="+id;
            Cursor cursor=getContentResolver().query(MediaStore.Files.getContentUri("external"),columns,selection,null,null);
            try{
                int columnIndex=cursor.getColumnIndex(columns[0]);
                if(cursor.moveToFirst()){
                    return cursor.getString(columnIndex);
                }
            }finally {
            }

        }
        return null;
    }
    public String getURLForResource(int resld) {
        return Uri.parse("android.resource://" + R.class.getPackage().getName() + "/" + resld).toString();
    }

    public void readDiary(Cursor cursor){
        if(pCheck){
            picPath=getURLForResource(R.drawable.camera2);
            if(cursor.moveToFirst()){
                if(cursor.getString(1)==null || cursor.getString(1).trim().equals("")){
                    edtDiary.setHint("?????? ????????? ?????? ??????????????????.(?????? 200???)");
                }
                else {
                    edtDiary.setText(cursor.getString(1));
                }
                if(cursor.getString(3).equals("null") || cursor.getString(3).trim().equals("")){
                    txtMusic.setText("???????????? ?????? ?????? ????????????");
                }
                else {
                    File file=new File(cursor.getString(3));
                    mp3Name=file.getName();
                    txtMusic.setText(mp3Name);
                }
                imgPhoto.setImageURI(Uri.parse(cursor.getString(2)));
            }
            else{
                edtDiary.setHint("?????? ????????? ?????? ??????????????????.(?????? 200???)");
                txtMusic.setText("???????????? ?????? ?????? ????????????");
                imgPhoto.setImageURI(Uri.parse(getURLForResource(R.drawable.camera2)));
            }
        }
        else {
            showToast("????????? ???????????? ??????????????? ????????? ??? ????????????.");
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
            showToast("????????? ???????????? ???????????? ????????? ??? ????????????.");
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
}