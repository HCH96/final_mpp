package org.tech.mobileprogrammingproject.Daily;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.tech.mobileprogrammingproject.FIREBASEDB.DailyDB;
import org.tech.mobileprogrammingproject.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static org.tech.mobileprogrammingproject.Daily.thirdPage.changeMemo;
import static org.tech.mobileprogrammingproject.Daily.secondPage.changeState;

/**
 * firstPage.java
 * 주요 기능: 3분할 할 일 체크리스트 구현
 *
 * 2020.11.14 (마지막 Comment 수정)
 * @author 김지원
 */

public class firstPage extends Fragment implements DatePickerDialog.OnDateSetListener{

    // variables
    public static String dateTime;
    private Button bt_date;     // 날짜 출력 및 DatePickerDialog 호출 버튼
    private Button bt_add;      // 할 일 추가 버튼
    private DatePickerDialog dateDialog;    // DatePickerDialog
    private LinearLayout showDaliyTodo;     // 할 일 목록 동적 생성을 위한 LinearLayout
    public int idx = 1;

    // row의 index를 담기 위한 DailyDB ArrayList
    ArrayList<DailyDB> itemIDArrayForRow = new ArrayList<>();

    // firebase 연결 및 DB 변수
    DatabaseReference database = null;
    DailyDB solved_db = null;

    SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd");
    Calendar cal;

    static ViewGroup rootView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        // firstpage의 layout 받아옴
        rootView = (ViewGroup) inflater.inflate(R.layout.firstpage, container, false);
        cal = Calendar.getInstance();

        // layout과 button 연결
        bt_date = rootView.findViewById(R.id.bt_date);
        bt_add = rootView.findViewById(R.id.bt_add);
        showDaliyTodo = rootView.findViewById(R.id.todolist);

        // firebase 연결
        database = FirebaseDatabase.getInstance().getReference();

        // 어플 구동 시 firstpage에 표시될 날짜 초기 값 지정
        bt_date.setText((cal.get(Calendar.MONTH)+1) + "월 " + cal.get(Calendar.DATE) + "일");

        // dateDialog 작동 시 나타날 초기 날짜 설정
        dateDialog = new DatePickerDialog(getContext(), this,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));

        // setOnClickListner를 통해 bt_date를 클릭할 경우 DatePickerDialog 호출
        bt_date.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                dateDialog.show();
            }
        });


        // setOnClickListner를 통해 bt_add 클릭할 경우 TodoAddedDialog(할 일 추가) 팝업 호출
        bt_add.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                TodoAddedDialog e = TodoAddedDialog.getInstance();

                // 날짜 정보를 bundle에 넣어 TodoAddedDialog로 보냄
                // 오늘 날짜에 할 일이 생성될 수 있도록
                Bundle args = new Bundle();
                args.putInt("state",0);
                args.putInt("year",dateDialog.getDatePicker().getYear());
                args.putInt("month", dateDialog.getDatePicker().getMonth() + 1);
                args.putInt("day", dateDialog.getDatePicker().getDayOfMonth());
                e.setArguments(args);

                e.show(getChildFragmentManager(), TodoAddedDialog.TAG_EVENT_DIALOG);
            }
        });
        setting();

        dateTime = Integer.toString(cal.get(Calendar.YEAR) * 1000 + (cal.get(Calendar.MONTH) + 1) * 100 + cal.get(Calendar.DAY_OF_MONTH));
        changeState(dateTime);
        return rootView;
    }


    /**
     * 할 일 리스트 추가, 출력
     *
     * @ return LinearLayout tr를 showDaliyTodo에 넣어 보여줌
     */
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        bt_date.setText((month+1) + "월 " + dayOfMonth + "일");

        // daily 하단 DB 정보에 변동이 생길 때마다 호출
        database.child("daily").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                itemIDArrayForRow.clear();
                itemIDArrayForRow.add(new DailyDB());
                showDaliyTodo.removeViews(1,showDaliyTodo.getChildCount() - 1);
                dateTime = Integer.toString(dateDialog.getDatePicker().getYear() * 1000 + (dateDialog.getDatePicker().getMonth() + 1) * 100 + dateDialog.getDatePicker().getDayOfMonth());
                DataSnapshot today = snapshot.child(dateTime);
                idx = 1;

                for (int i = 0; i < 4; i++) {
                    /*
                    i = 0 : 아침(radioButton01)
                    i = 1 : 점심(radioButton02)
                    i = 2 : 저녁(radioButton03)
                    i = 3 : 완료된 할 일
                    */
                    DataSnapshot currData = today.child(Integer.toString(i));
                    for (DataSnapshot childData : currData.getChildren()) {
                        DailyDB currDailyDB = childData.getValue(DailyDB.class);

                        // 할 일을 담기 위한 LinearLayout 생성
                        LinearLayout tr = new LinearLayout(getActivity());
                        tr.setOrientation(LinearLayout.HORIZONTAL);

                        // 3분할 정보를 나타내주는 textView1 생성
                        // 아침, 점심, 저녁에 따라 색상을 다르게 설정함 (아침에서 저녁까지 점점 더 색이 짙어짐)
                        TextView textView1 = new TextView(getContext());
                        if(i == 0) textView1.setBackgroundColor(Color.parseColor("#ccdeeb"));
                        else if(i == 1) textView1.setBackgroundColor(Color.parseColor("#B4D3E7"));
                        else textView1.setBackgroundColor(Color.parseColor("#92b5d8"));
                        textView1.setText("");
                        textView1.setGravity(Gravity.CENTER);
                        textView1.setPadding(3,10,3,10);
                        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
                        params1.weight = 0.15f;
                        textView1.setLayoutParams(params1);

                        // 할 일의 내용을 담는 textView2 생성
                        TextView textView2 = new TextView(getContext());
                        textView2.setText(currDailyDB.content);
                        textView2.setGravity(Gravity.CENTER);
                        textView2.setPadding(3,10,3,10);
                        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
                        params2.weight = 0.7f;
                        textView2.setLayoutParams(params2);

                        // 할 일의 완료 여부를 체크하는 Button 생성
                        Button cb = new Button(getContext());
                        cb.setGravity(Gravity.CENTER);
                        LinearLayout.LayoutParams params3 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
                        params3.weight = 0.15f;
                        cb.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ck_button));
                        cb.setLayoutParams(params3);

                        // 완료된 할 일 시각적으로 표시 (컬러가 아닌 회색조로 표현)
                        if(i==3) {
                            tr.setBackgroundColor(Color.parseColor("#ededed"));
                            textView1.setBackgroundColor(Color.parseColor("#BDBDBD"));
                            textView2.setTextColor(Color.parseColor("#9b9b9b"));
                        }

                        // 할 일을 담기위해 생성해둔 LinearLayout에 내용을 담음
                        tr.addView(textView1);
                        tr.addView(textView2);
                        tr.addView(cb);

                        cb.setId(-idx);
                        tr.setId(idx++);
                        itemIDArrayForRow.add(currDailyDB);

                        // 수정을 위해 할 일 LinearLayout을 클릭이 가능하도록 설정
                        tr.setClickable(true);

                        // 클릭 시에 DB에 담아두었던 정보를 받아와 bundle을 통해 TodoAddedDialog로 보냄
                        // TodoAddedDialog 팝업 시에 기존 정보가 저장된 채로 팝업되게 하기 위함 (for 할 일 수정)
                        tr.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                TodoAddedDialog e = TodoAddedDialog.getInstance();
                                Bundle args = new Bundle();
                                args.putInt("state",1);
                                int i = v.getId();
                                DailyDB curr = itemIDArrayForRow.get(i);
                                args.putString("createdDate",curr.createDate);
                                args.putString("content",curr.content);
                                args.putString("catalog",curr.catalog);
                                args.putInt("timeline",curr.timeline);
                                args.putString("currDate", dateTime);
                                args.putLong("dateLong", curr.date);

                                e.setArguments(args);
                                e.show(getChildFragmentManager(), TodoAddedDialog.TAG_EVENT_DIALOG);
                            }
                        });

                        // CheckBox 체크 시 해당 할 일 DB를 database.child("daily").child("할 일이 작성된 날짜").child("3")으로 이동
                        // 기존의 DB 위치를 체크하여 삭제하고, Status 팝업에서 시간 정보까지 받아 새로운 위치에서 재생성되도록 구현함
                        cb.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Status s = Status.getinstance();

                                // Status 팝업에서 시간정보를 업데이트 해야하므로 번들에 정보를 넣어 전송해줌
                                Bundle bundle = new Bundle();
                                int i = v.getId();
                                DailyDB solve = itemIDArrayForRow.get((-1 * i));

                                bundle.putString("createdDate",solve.createDate);
                                bundle.putInt("timeline",solve.timeline);
                                bundle.putLong("dateLong", solve.date);
                                bundle.putString("content",solve.content);
                                bundle.putString("catalog",solve.catalog);
                                bundle.putString("currDate", dateTime);
                                s.setArguments(bundle);

                                s.show(getChildFragmentManager(), Status.TAG_STATUS_DIALOG);
                            }
                        });

                        // 할 일이 완료되었을 경우 완료된 할 일이 담긴 LinearLayout이 클릭되지 않고, Button이 보이지 않도록 설정
                        if(currDailyDB.timeline == 3) {
                            tr.setEnabled(false);
                            cb.setVisibility(View.INVISIBLE);
                        }
                        showDaliyTodo.addView(tr);
                    }
                }
                // 2020.11.01 황교민
                // 사용자가 선택한 날짜를 공유하는 static 변수인 dateTime이 변경되었을 경우, fragment에서는 변화를 인식하지 못함.
                // changeMemo메소드를 import하여 memo의 내용을 수정하도록 함.
                changeMemo(dateTime);
                changeState(dateTime);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    /**
     * firstPage에 처음 접속 시 화면에 할 일이 표시되지 않는 기능을 보완하기 위한 함수
     * (코드 내용은 위와 같음)
     *
     * @ return LinearLayout tr를 showDaliyTodo에 넣어 보여줌
     */
    public void setting(){

        // daily 하단 DB 정보에 변동이 생길 때마다 호출
        database.child("daily").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                itemIDArrayForRow.clear();
                itemIDArrayForRow.add(new DailyDB());
                idx = 1;
                showDaliyTodo.removeViews(1,showDaliyTodo.getChildCount() - 1);
                final String currDate = Long.toString(cal.get(Calendar.YEAR) * 1000 + (cal.get(Calendar.MONTH) + 1) * 100 + cal.get(Calendar.DATE));
                DataSnapshot today = snapshot.child(currDate);

                for (int i = 0; i < 4; i++) {
                    /*
                    i = 0 : 아침(radioButton01)
                    i = 1 : 점심(radioButton02)
                    i = 2 : 저녁(radioButton03)
                    i = 3 : 완료된 할 일
                    */
                    DataSnapshot currData = today.child(Integer.toString(i));
                    for (DataSnapshot childData : currData.getChildren()) {
                        DailyDB currDailyDB = childData.getValue(DailyDB.class);

                        // 할 일을 담기 위한 LinearLayout 생성
                        LinearLayout tr = new LinearLayout(getActivity());
                        tr.setOrientation(LinearLayout.HORIZONTAL);

                        // 3분할 정보를 나타내주는 textView1 생성
                        // 아침, 점심, 저녁에 따라 색상을 다르게 설정함 (아침에서 저녁까지 점점 더 색이 짙어짐)
                        TextView textView1 = new TextView(getContext());
                        if(i == 0) textView1.setBackgroundColor(Color.parseColor("#ccdeeb"));
                        else if(i == 1) textView1.setBackgroundColor(Color.parseColor("#B4D3E7"));
                        else textView1.setBackgroundColor(Color.parseColor("#92b5d8"));
                        textView1.setText("");
                        textView1.setGravity(Gravity.CENTER);
                        textView1.setPadding(3,10,3,10);
                        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
                        params1.weight = 0.15f;
                        textView1.setLayoutParams(params1);

                        // 할 일의 내용을 담는 textView2 생성
                        TextView textView2 = new TextView(getContext());
                        textView2.setText(currDailyDB.content);
                        textView2.setGravity(Gravity.CENTER);
                        textView2.setPadding(3,10,3,10);
                        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
                        params2.weight = 0.7f;
                        textView2.setLayoutParams(params2);

                        // 할 일의 완료 여부를 체크하는 Button 생성
                        Button cb = new Button(getContext());
                        cb.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ck_button));
                        LinearLayout.LayoutParams params3 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
                        params3.weight = 0.15f;
                        cb.setLayoutParams(params3);

                        cb.setId(-1 * idx);
                        tr.setId(idx++);
                        itemIDArrayForRow.add(currDailyDB);

                        // 수정을 위해 할 일 LinearLayout을 클릭이 가능하도록 설정
                        tr.setClickable(true);

                        // 클릭 시에 DB에 담아두었던 정보를 받아와 bundle을 통해 TodoAddedDialog로 보냄
                        // TodoAddedDialog 팝업 시에 기존 정보가 저장된 채로 팝업되게 하기 위함 (for 할 일 수정)
                        tr.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                TodoAddedDialog e = TodoAddedDialog.getInstance();
                                Bundle args = new Bundle();
                                args.putInt("state",1);
                                int i = v.getId();
                                DailyDB curr = itemIDArrayForRow.get(i);
                                args.putString("createdDate",curr.createDate);
                                args.putString("content",curr.content);
                                args.putString("catalog",curr.catalog);
                                args.putInt("timeline",curr.timeline);
                                args.putString("currDate", dateTime);
                                args.putLong("dateLong", curr.date);
                                System.out.println(curr.createDate);
                                e.setArguments(args);
                                e.show(getChildFragmentManager(), TodoAddedDialog.TAG_EVENT_DIALOG);
                            }
                        });

                        // CheckBox 체크 시 해당 할 일 DB를 database.child("daily").child("할 일이 작성된 날짜").child("3")으로 이동
                        // 기존의 DB 위치를 체크하여 삭제하고, Status 팝업에서 시간 정보까지 받아 새로운 위치에서 재생성되도록 구현함
                        cb.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Status s = Status.getinstance();

                                // Status 팝업에서 시간정보를 업데이트 해야하므로 번들에 정보를 넣어 전송해줌
                                Bundle bundle = new Bundle();
                                int i = v.getId();
                                DailyDB solve = itemIDArrayForRow.get((-1 * i));

                                bundle.putInt("cbId",v.getId());
                                bundle.putString("createdDate",solve.createDate);
                                bundle.putInt("timeline",solve.timeline);
                                bundle.putLong("dateLong", solve.date);
                                bundle.putString("content",solve.content);
                                bundle.putString("catalog",solve.catalog);
                                bundle.putString("currDate", dateTime);

                                s.setArguments(bundle);
                                s.show(getChildFragmentManager(), Status.TAG_STATUS_DIALOG);
                                System.out.println(111);
                            }
                        });

                        // 할 일이 완료되었을 경우 완료된 할 일이 담긴 LinearLayout과 CheckBox가 클릭되지 않도록 설정함
                        if(currDailyDB.timeline == 3) {
                            tr.setEnabled(false);
                            cb.setVisibility(View.INVISIBLE);
                        }

                        // 완료된 할 일 시각적으로 표시 (컬러가 아닌 회색조로 표현)
                        if(i==3) {
                            tr.setBackgroundColor(Color.parseColor("#ededed"));
                            textView1.setBackgroundColor(Color.parseColor("#BDBDBD"));
                            textView2.setTextColor(Color.parseColor("#9b9b9b"));
                        }

                        // 할 일을 담기위해 생성해둔 LinearLayout에 내용을 담음
                        tr.addView(textView1);
                        tr.addView(textView2);
                        tr.addView(cb);

                        showDaliyTodo.addView(tr);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}