package org.tech.mobileprogrammingproject.Monthly;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import org.tech.mobileprogrammingproject.FIREBASEDB.MonthlyDB;
import org.tech.mobileprogrammingproject.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
/*
    황교민 2020.10.17
    1. Calendar view를 통한 캘린더 기능을 구현함.
    2. 실시간으로 데이터를 보여주기 위해서 Firebase DB로 변경함.

    황교민 2020.10.19
    1. 월별 캘린더에 들어가자 마자 당일날의 일정이 표시되지 않음. -> 따로 setting함수를 통해서 페이지에 들어가자마자 당일
    할일을 표현함.

    황교민 2020.10.21
    1. Calendar view로는 캘린더뷰 decoration하기 어렵기 때문에 material calendar view로 변경함.

    황교민 2020.10.30
    1. 기존에는 Calendar view에서 날짜를 변경하면 popup 페이지가 바로 나타난다. -> 할일 추가 버튼을 따로 만들어서,
    할일을 추가하기 위해서는 추가 버튼을 눌러야만 팝업창이 뜬다.
 */
public class Calender extends AppCompatActivity {
    ArrayList<String> itemIDArray = new ArrayList<>();

    MaterialCalendarView calender;
    LinearLayout showTodos;

    SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd");

    DatabaseReference database = null;
    Date date;
    String userSelectDate = "";
    long userSelectDateInt = 0;
    CalendarDay curr;
    Calendar calUser = Calendar.getInstance();
    Button btn;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calender);

        database = FirebaseDatabase.getInstance().getReference();
        btn = findViewById(R.id.add);
        calender = findViewById(R.id.calender);
        showTodos = findViewById(R.id.presentmonthly);
        String temp = transFormat.format(calUser.getTime());
        calender.addDecorators(new oneDayDacorator());

        try {
            date = transFormat.parse(temp);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        calUser.setTime(date);
        curr = CalendarDay.from(date);
        setting(calUser.getTimeInMillis());
        // 날짜가 변경되면 그 날짜에 해당하는 할일을 화면에 표시하도록 함.
        // 화면에 할일을 표현할 때, 동적으로 생성함.
        calender.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull final CalendarDay date, boolean selected) {
                curr = date;
                showTodos.removeViews(1,showTodos.getChildCount() - 1);
                userSelectDate = (2020 + date.getYear() - 100)+"-"+(date.getMonth()+1)+"-"+date.getDay();
                Date user = null;
                try {
                    user = transFormat.parse(userSelectDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                calUser.setTime(user);
                userSelectDateInt = date.getCalendar().getTimeInMillis();
                database.child("monthly").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        itemIDArray.clear();
                        calender.removeDecorators();
                        calender.addDecorators(new oneDayDacorator()); // 선택한 날짜를 진하게 표시함.
                        showTodos.removeViews(1,showTodos.getChildCount() - 1);
                        //Iterator<DataSnapshot> dataSnapshots = snapshot.getChildren().iterator();
                        for(DataSnapshot childSnapshot : snapshot.getChildren()) {
                            itemIDArray.add(childSnapshot.getKey());
                            MonthlyDB currMonDB = childSnapshot.getValue(MonthlyDB.class);
                            // 할일이 있는 날짜에 빨간 점을 표시하기 위함. k의 증가율은 하루를 long으로 환산한것임.
                            for(long k = currMonDB.startPoint ; k <= currMonDB.endPoint ; k += 1000 * 60 * 60 * 24){
                                Date temp = new Date();
                                temp.setTime(k);
                                calender.addDecorators(new EventDecorator(Color.RED, Collections.singletonList(CalendarDay.from(temp))));
                            }
                            // 사용자가 선택한 날짜가 childsnapshot에서 받아온 날짜 사이에 있으면 showtodo에 할일을 상세히 표시함.
                            if(currMonDB.startPoint <= userSelectDateInt && userSelectDateInt <= currMonDB.endPoint) {
                                LinearLayout tr = new LinearLayout(getApplicationContext());
                                tr.setOrientation(LinearLayout.HORIZONTAL);

                                TextView textview1 = new TextView(getApplicationContext());
                                textview1.setText(currMonDB.content);
                                textview1.setGravity(Gravity.CENTER_VERTICAL);
                                textview1.setPadding(60, 0, 0, 0);
                                LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
                                params1.weight = 0.45f;
                                textview1.setLayoutParams(params1);

                                TextView textview2 = new TextView(getApplicationContext());
                                Date presentDate = new Date(currMonDB.endPoint);
                                textview2.setText((presentDate.getYear() - 100) + "년 " +  (presentDate.getMonth() + 1) + "월 " + presentDate.getDate()+"일   ");
                                textview2.setGravity(Gravity.CENTER);
                                LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
                                params2.weight = 0.35f;
                                textview2.setLayoutParams(params2);

                                CheckBox cb = new CheckBox(getApplicationContext());
                                cb.setGravity(Gravity.CENTER);
                                LinearLayout.LayoutParams params3 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
                                params3.weight = 0.1f;
                                cb.setLayoutParams(params3);
                                cb.setId(itemIDArray.size() - 1);
                                cb.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        database.child("monthly").child(itemIDArray.get(v.getId())).removeValue();
                                    }
                                });

                                tr.addView(textview1);
                                tr.addView(textview2);
                                tr.addView(cb);
                                showTodos.addView(tr);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Calender.this, monthlyPopup.class);
                intent.putExtra("year", curr.getYear());
                intent.putExtra("month", curr.getMonth());
                intent.putExtra("dayOfMonth", curr.getDay());
                startActivity(intent);
            }
        });
    }

    // intent를 통해 들어오자 마자, 화면에 당일 할일을 표시하기 위해서 함수를 따로 제작함.
    public void setting(final long date){

        database.child("monthly").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                itemIDArray.clear();
                showTodos.removeViews(1,showTodos.getChildCount() - 1);
                for(DataSnapshot childSnapshot : snapshot.getChildren()) {
                    itemIDArray.add(childSnapshot.getKey());
                    MonthlyDB currMonDB = childSnapshot.getValue(MonthlyDB.class);
                    for(long k = currMonDB.startPoint ; k <= currMonDB.endPoint ; k += 1000 * 60 * 60 * 24){
                        Date temp = new Date();
                        temp.setTime(k);
                        calender.addDecorators(new EventDecorator(Color.RED, Collections.singletonList(CalendarDay.from(temp))));
                    }

                    if(currMonDB.startPoint <= date && date <= currMonDB.endPoint) {
                        LinearLayout tr = new LinearLayout(getApplicationContext());
                        tr.setOrientation(LinearLayout.HORIZONTAL);

                        TextView textview1 = new TextView(getApplicationContext());
                        textview1.setText(currMonDB.content);
                        textview1.setGravity(Gravity.CENTER);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
                        params.weight = 1.0f;
                        textview1.setLayoutParams(params);

                        TextView textview2 = new TextView(getApplicationContext());
                        Date presentDate = new Date(currMonDB.endPoint);
                        textview2.setText((presentDate.getYear() - 100) + "년 " +  (presentDate.getMonth() + 1) + "월 " + presentDate.getDate()+"일");
                        textview2.setGravity(Gravity.CENTER);
                        textview2.setLayoutParams(params);

                        CheckBox cb = new CheckBox(getApplicationContext());
                        cb.setGravity(Gravity.CENTER);
                        cb.setLayoutParams(params);
                        cb.setId(itemIDArray.size() - 1);
                        cb.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                database.child("monthly").child(itemIDArray.get(v.getId())).removeValue();
                            }
                        });

                        tr.addView(textview1);
                        tr.addView(textview2);
                        tr.addView(cb);
                        showTodos.addView(tr);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}