package org.tech.mobileprogrammingproject.Daily;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.tech.mobileprogrammingproject.FIREBASEDB.DailyDB;
import org.tech.mobileprogrammingproject.R;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;

import static org.tech.mobileprogrammingproject.Daily.firstPage.dateTime;

/**
 * Status.java
 * 주요 기능: 완료 체크 및 시간 정보 업데이트 팝업
 *
 * 2020.11.14 (마지막 Comment 수정)
 * @author 김지원
 */

public class Status extends DialogFragment {

    // variables
    private EditText et_startTime; // 시작 시간 입력
    private EditText et_endTime; // 종료 시간 입력
    private CheckBox cb_none; // 시간 미지정 CheckBox
    private Button bt_cancelDone; // 등록 취소 버튼
    private Button bt_done; // 등록 완료 버튼
    private Calendar currentTime;

    // 시간 정보 기본 값 설정
    // 시간 미지정에 체크하는 경우를 위해 실제 시간 정보로는 불가능한 임의의 값 할당
    int startHour = 30;
    int startMin = 90;
    int endHour = 40;
    int endMin = 100;

    // firstPage에서 번들로 저장해온 값을 받아줄 variables
    String createdDate;
    String content;
    String catalog;
    String dateTime;
    int timeline;
    Long dateLong;

    // firebase 연결 및 DB 변수
    DatabaseReference database = null;
    DailyDB timeUpdateDb = null;

    // 기존에 등록된 시간정보와 동일한 정보인지 체크하기 위한 ArrayList
    ArrayList<Integer> startArray = new ArrayList<>();
    ArrayList<Integer> endArray = new ArrayList<>();

    // 새로운 Status 생성 및 return
    public static final String TAG_STATUS_DIALOG = "status_event";
    public static Status getinstance() {
        Status s = new Status();
        return s;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // status_popup layout과 연결
        View v = inflater.inflate(R.layout.status_popup, container, false);

        // layout과 button 연결
        et_startTime = v.findViewById(R.id.startTime);
        et_endTime = v.findViewById(R.id.endTime);
        bt_cancelDone = v.findViewById(R.id.bt_cancelDone);
        bt_done = v.findViewById(R.id.bt_done);
        cb_none = v.findViewById(R.id.cb_none);

        // firebase 연결
        database = FirebaseDatabase.getInstance().getReference();

        // firstPage에서 번들로 받아온 값 저장
        createdDate = getArguments().getString("createdDate");
        content = getArguments().getString("content");
        catalog = getArguments().getString("catalog");
        dateTime = getArguments().getString("dateTime");
        timeline = getArguments().getInt("timeline");
        dateLong = getArguments().getLong("dateLong");

        // 완료된 할 일이 추가될 경우 시작시간과 종료 시간을 Array에 추가해줌
        // for 시간 중복 여부 체크
        database.child("daily").child(Long.toString(dateLong)).child("3").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot childData : snapshot.getChildren()){
                    DailyDB currDailyDB = childData.getValue(DailyDB.class);
                    startArray.add(currDailyDB.startTime);
                    endArray.add(currDailyDB.endTime);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // setOnClickListner를 통해 시작 시간 정보 입력
        et_startTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 시간 정보 입력 시 시간 미지정 체크박스 해제
                cb_none.setChecked(false);

                currentTime = Calendar.getInstance();
                int hour = currentTime.get(Calendar.HOUR_OF_DAY);
                int minute = currentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        // 선택한 시간 정보 EditText에 출력
                        et_startTime.setText(selectedHour + "시 " + selectedMinute + "분");
                        // 시작 시간 정보 저장
                        startHour = selectedHour;
                        startMin = selectedMinute;
                    }
                }, hour, minute, false);
                mTimePicker.setTitle("Start Time");
                mTimePicker.show();
            }
        });

        // setOnClickListner를 통해 종료 시간 정보 입력
        et_endTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 시간 정보 입력 시 시간 미지정 체크박스 해제
                cb_none.setChecked(false);

                currentTime = Calendar.getInstance();
                int hour = currentTime.get(Calendar.HOUR_OF_DAY);
                int minute = currentTime.get(Calendar.MINUTE);
                TimePickerDialog endTimePicker;
                endTimePicker = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int endSelectedHour, int endSelectedMinute) {
                        // 선택한 시간 정보 EditText에 출력
                        et_endTime.setText(endSelectedHour + "시 " + endSelectedMinute + "분");
                        // 종료 시간 정보 저장
                        endHour = endSelectedHour;
                        endMin = endSelectedMinute;
                    }
                }, hour, minute, false);

                endTimePicker.setTitle("End Time");
                endTimePicker.show();
            }
        });

        // 시간 미지정 체크 시 시간 정보 초기화
        cb_none.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_startTime.getText().clear();
                et_endTime.getText().clear();
            }
        });

        // 취소 버튼 클릭 시 이동 된 DB를 원래의 자리로 돌려놓음
        bt_cancelDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "취소되었습니다.", Toast.LENGTH_LONG).show();
                dismiss();
            }
        });

        // 완료 버튼 클릭 시 시간정보 업데이트 후 완료 할 일로 등록
        bt_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 시간 정보가 입력되지 않은 경우와 잘못 등록된 경우 완료하지 못하도록 설정
                if (startHour == 30 && endHour == 40 && cb_none.isChecked() == false) {
                    et_startTime.setText("시간 정보가 입력되지 않았습니다.");
                    et_startTime.setTextColor(Color.parseColor("#7bb0db"));
                    et_startTime.setTextSize(15);
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            et_startTime.getText().clear();
                            et_startTime.setTextColor(Color.parseColor("#000000"));
                            et_startTime.setTextSize(18);
                        }
                    }, 2000);
                } else if (startHour != 30 && endHour == 40) {
                    et_endTime.setText("종료 시간이 입력되지 않았습니다.");
                    et_endTime.setTextColor(Color.parseColor("#7bb0db"));
                    et_endTime.setTextSize(15);
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            et_endTime.getText().clear();
                            et_endTime.setTextColor(Color.parseColor("#000000"));
                            et_endTime.setTextSize(18);
                        }
                    }, 2000);
                } else if (startHour == 30 && endHour != 40) {
                    et_startTime.setText("시작 시간이 입력되지 않았습니다.");
                    et_startTime.setTextColor(Color.parseColor("#7bb0db"));
                    et_startTime.setTextSize(15);
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            et_startTime.getText().clear();
                            et_startTime.setTextColor(Color.parseColor("#000000"));
                            et_startTime.setTextSize(18);
                        }
                    }, 2000);
                } else if(endHour < startHour && cb_none.isChecked() == false || endHour == startHour && endMin < startMin && cb_none.isChecked() == false){
                    et_endTime.setText("시작 시간보다 빠릅니다!\n시간을 재설정해주세요.");
                    et_endTime.setTextColor(Color.parseColor("#ff8682"));
                    et_endTime.setTextSize(15);
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            et_endTime.setText(endHour + "시 " + endMin + "분");
                            et_endTime.setTextColor(Color.parseColor("#000000"));
                            et_endTime.setTextSize(18);
                        }
                    }, 2000);
                } else if(endHour == startHour && endMin == startMin && cb_none.isChecked() == false){
                    et_endTime.setText("시작 시간과 같습니다!\n시간을 재설정해주세요.");
                    et_endTime.setTextColor(Color.parseColor("#ff8682"));
                    et_endTime.setTextSize(15);
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            et_endTime.setText(endHour + "시 " + endMin + "분");
                            et_endTime.setTextColor(Color.parseColor("#000000"));
                            et_endTime.setTextSize(18);
                        }
                    }, 2000);
                } else if (cb_none.isChecked() == true){
                    // 시간 미지정의 경우 시간 정보 0으로 변경
                    timeUpdateDb = new DailyDB();
                    timeUpdateDb.createDate = createdDate;
                    timeUpdateDb.content = content;
                    timeUpdateDb.state = 0;
                    timeUpdateDb.timeline = 3;
                    timeUpdateDb.catalog = catalog;
                    timeUpdateDb.date = dateLong;
                    timeUpdateDb.startTime = 0;
                    timeUpdateDb.endTime = 0;

                    database.child("daily").child(Long.toString(timeUpdateDb.date)).child(String.valueOf(timeline)).child(timeUpdateDb.createDate).removeValue();

                    database.child("daily").child(Long.toString(timeUpdateDb.date)).child("3").child(timeUpdateDb.createDate).removeValue();
                    database.child("daily").child(Long.toString(timeUpdateDb.date)).child("3").child(timeUpdateDb.createDate).setValue(timeUpdateDb);

                    Toast.makeText(v.getContext(), "할 일을 완료하였습니다.", Toast.LENGTH_LONG).show();
                    dismiss();
                } else {
                    // 시간 정보가 제대로 된 경우 업데이트
                    timeUpdateDb = new DailyDB();
                    timeUpdateDb.createDate = createdDate;
                    timeUpdateDb.content = content;
                    timeUpdateDb.state = 0;
                    timeUpdateDb.timeline = 3;
                    timeUpdateDb.catalog = catalog;
                    timeUpdateDb.date = dateLong;
                    timeUpdateDb.startTime = startHour*100 + startMin;
                    timeUpdateDb.endTime = endHour*100 + endMin;

                    // 기존 시간 정보와 중복되는지 여부 체크
                    boolean isValid = true;
                    for(int i = 0 ; i < startArray.size() ; i++) {
                        if ((timeUpdateDb.startTime <= startArray.get(i) && startArray.get(i) <= timeUpdateDb.endTime) | (timeUpdateDb.startTime <= endArray.get(i) && endArray.get(i) <= timeUpdateDb.endTime) | (timeUpdateDb.startTime >= startArray.get(i) && timeUpdateDb.endTime <= endArray.get(i))){
                            isValid = false;
                            break;
                        }
                    }
                    if(!isValid){
                        et_endTime.setText("시간이 중복되었습니다!\n시간을 재설정해주세요.");
                        et_endTime.setTextColor(Color.parseColor("#ff8682"));
                        et_endTime.setTextSize(15);
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                et_endTime.setText(endHour + "시 " + endMin + "분");
                                et_endTime.setTextColor(Color.parseColor("#000000"));
                                et_endTime.setTextSize(18);
                            }
                        }, 2000);
                    }
                    if(isValid) {
                        database.child("daily").child(Long.toString(timeUpdateDb.date)).child(String.valueOf(timeline)).child(timeUpdateDb.createDate).removeValue();

                        database.child("daily").child(Long.toString(timeUpdateDb.date)).child("3").child(timeUpdateDb.createDate).removeValue();
                        database.child("daily").child(Long.toString(timeUpdateDb.date)).child("3").child(timeUpdateDb.createDate).setValue(timeUpdateDb);

                        Toast.makeText(v.getContext(), "할 일을 완료하였습니다.", Toast.LENGTH_LONG).show();
                        dismiss();
                    }
                }
            }
        });
        setCancelable(false);
        return v;
    }
}
