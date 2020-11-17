package org.tech.mobileprogrammingproject.Daily;

import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.tech.mobileprogrammingproject.FIREBASEDB.DailyDB;
import org.tech.mobileprogrammingproject.FIREBASEDB.categoryDB;
import org.tech.mobileprogrammingproject.R;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * TodoAddedDialog.java
 * 주요 기능: 할 일 추가 팝업
 *
 * 2020.11.14 (마지막 Comment 수정)
 * @author 김지원
 */

public class TodoAddedDialog extends DialogFragment implements View.OnClickListener {

    // variables
    private Button bt_cancel;   // 등록 취소 버튼
    private Button bt_listUp;   // 할 일 등록 버튼
    private RadioGroup timeGroup;    // 3분할 설정 RadioGroup
    ImageButton delBtn;     // 삭제 버튼
    EditText content; // 할 일 입력 EditText
    Spinner spinner; // 카테고리 설정 spinner

    // firebase 연결 및 DB 변수
    DatabaseReference database = null;
    DailyDB dailydb = null;

    Calendar cal;

    // 새로운 TodoAddedDialog 생성 및 return
    public static final String TAG_EVENT_DIALOG = "dialog_event";
    public static TodoAddedDialog getInstance() {
        TodoAddedDialog e = new TodoAddedDialog();
        return e;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // firebase 연결
        database = FirebaseDatabase.getInstance().getReference();

        // 신규 생성 시
        if(getArguments().getInt("state") == 0) {
            // todo_popup layout과 spinner layout 받아옴
            View v = inflater.inflate(R.layout.todo_popup, container, false);
            spinner = (Spinner) v.findViewById(R.id.category_spinner);

            ArrayAdapter<CharSequence> adapterArray = ArrayAdapter.createFromResource(v.getContext(), R.array.category_list, android.R.layout.simple_spinner_item);
            adapterArray.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapterArray);
            cal = Calendar.getInstance();

            // layout과 button 연결
            content = v.findViewById(R.id.add_todo);
            bt_cancel = v.findViewById(R.id.bt_cancel);
            bt_listUp = v.findViewById(R.id.bt_listUp);
            timeGroup = v.findViewById(R.id.time_group);

            // 구현해둔 OnClick 함수 연결
            bt_cancel.setOnClickListener(this);
            bt_listUp.setOnClickListener(this);
            setCancelable(false);
            return v;

        }else{ // 기존 할 일 수정
            // todo_popup layout과 spinner layout 받아옴
            View v = inflater.inflate(R.layout.todo_popup, container, false);
            spinner = (Spinner) v.findViewById(R.id.category_spinner);

            ArrayAdapter<CharSequence> adapterArray = ArrayAdapter.createFromResource(v.getContext(), R.array.category_list, android.R.layout.simple_spinner_item);
            adapterArray.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapterArray);
            cal = Calendar.getInstance();

            // layout과 button 연결
            bt_cancel = v.findViewById(R.id.bt_cancel);
            bt_listUp = v.findViewById(R.id.bt_listUp);
            content = v.findViewById(R.id.add_todo);
            delBtn = v.findViewById(R.id.delBtn);
            timeGroup = v.findViewById(R.id.time_group);

            // 삭제 버튼 구현
            delBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    database.child("daily").child(Long.toString(getArguments().getLong("dateLong"))).child(Integer.toString(getArguments().getInt("timeline"))).child(getArguments().getString("createdDate")).removeValue();
                    dismiss();
                }
            });

            // 구현해둔 OnClick 함수 연결
            bt_cancel.setOnClickListener(this);
            bt_listUp.setOnClickListener(this);
            setCancelable(false);

            // 기존 할 일 수정 시 timeline 정보를 받아와 해당 timeGroup에 체크해줌
            switch (getArguments().getInt("timeline")){
                case 0:
                    timeGroup.check(R.id.time1);
                    break;
                case 1:
                    timeGroup.check(R.id.time2);
                    break;
                case 2:
                    timeGroup.check(R.id.time3);
                    break;
            }

            // 기존 할 일 수정 시 할 일 내용을 받아와 EditText에 넣어줌
            content.setText(getArguments().getString("content"));

            // 기존 할 일 수정 시 category 정보를 받아와 Spinner에서 select해줌
            switch (getArguments().getString("catalog")){
                case "미정":
                    spinner.setSelection(0);
                    break;
                case "공부":
                    spinner.setSelection(1);
                    break;
                case "과제":
                    spinner.setSelection(2);
                    break;
                case "운동":
                    spinner.setSelection(3);
                    break;
            }
            return v;
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_listUp: //확인 버튼을 눌렀을 때

                // 입력된 할 일이 없는 경우
                if (content.getText().toString().equals("")) {
                    Toast.makeText(v.getContext(), "할 일이 입력되지 않았습니다.", Toast.LENGTH_LONG).show();
                    break;
                }

                // 기존에 등록되어 있던 할 일을 수정한 경우
                if(getArguments().getInt("state") == 1){
                    System.out.println(Long.toString(getArguments().getLong("dateLong")));
                    System.out.println(Integer.toString(getArguments().getInt("timeline")));
                    System.out.println(getArguments().getString("createdDate"));
                    database.child("daily").child(Long.toString(getArguments().getLong("dateLong"))).child(Integer.toString(getArguments().getInt("timeline"))).child(getArguments().getString("createdDate")).removeValue();
                    if (timeGroup.getCheckedRadioButtonId() == R.id.time1) {
                        dailydb = new DailyDB();
                        dailydb.createDate = cal.getTime().toString();
                        dailydb.content = content.getText().toString();
                        dailydb.state = 0;
                        dailydb.timeline = 0;
                        dailydb.catalog = spinner.getSelectedItem().toString();
                        dailydb.date = getArguments().getLong("dateLong");
                        database.child("daily").child(Long.toString(dailydb.date)).child("0").child(cal.getTime().toString()).setValue(dailydb);

                        dismiss();
                        break;
                    }
                    else if(timeGroup.getCheckedRadioButtonId() == R.id.time2){
                        dailydb = new DailyDB();
                        dailydb.content = content.getText().toString();
                        dailydb.createDate = cal.getTime().toString();
                        dailydb.state = 0;
                        dailydb.timeline = 1;
                        dailydb.catalog = spinner.getSelectedItem().toString();
                        dailydb.date = getArguments().getLong("dateLong");
                        database.child("daily").child(Long.toString(dailydb.date)).child("1").child(cal.getTime().toString()).setValue(dailydb);

                        dismiss();
                        break;
                    } else if (timeGroup.getCheckedRadioButtonId() == R.id.time3){
                        dailydb = new DailyDB();
                        dailydb.content = content.getText().toString();
                        dailydb.createDate = cal.getTime().toString();
                        dailydb.state = 0;
                        dailydb.timeline = 2;
                        dailydb.catalog = spinner.getSelectedItem().toString();
                        dailydb.date = getArguments().getLong("dateLong");
                        database.child("daily").child(Long.toString(dailydb.date)).child("2").child(cal.getTime().toString()).setValue(dailydb);
                        dismiss();
                        break;
                    }
                }

                // 할 일 신규 등록 시
                if (timeGroup.getCheckedRadioButtonId() == R.id.time1) {
                    dailydb = new DailyDB();
                    dailydb.createDate = cal.getTime().toString();
                    dailydb.content = content.getText().toString();
                    dailydb.state = 0;
                    dailydb.timeline = 0;
                    dailydb.catalog = spinner.getSelectedItem().toString();
                    dailydb.date = getArguments().getInt("year") * 1000 + getArguments().getInt("month") * 100 + getArguments().getInt("day");
                    database.child("daily").child(Long.toString(dailydb.date)).child("0").child(cal.getTime().toString()).setValue(dailydb);

                    dismiss();
                    break;
                }
                else if(timeGroup.getCheckedRadioButtonId() == R.id.time2){
                    dailydb = new DailyDB();
                    dailydb.content = content.getText().toString();
                    dailydb.createDate = cal.getTime().toString();
                    dailydb.state = 0;
                    dailydb.timeline = 1;
                    dailydb.catalog = spinner.getSelectedItem().toString();
                    dailydb.date = getArguments().getInt("year") * 1000 + getArguments().getInt("month") * 100 + getArguments().getInt("day");
                    database.child("daily").child(Long.toString(dailydb.date)).child("1").child(cal.getTime().toString()).setValue(dailydb);

                    dismiss();
                    break;
                } else if (timeGroup.getCheckedRadioButtonId() == R.id.time3){
                    dailydb = new DailyDB();
                    dailydb.content = content.getText().toString();
                    dailydb.createDate = cal.getTime().toString();
                    dailydb.state = 0;
                    dailydb.timeline = 2;
                    dailydb.catalog = spinner.getSelectedItem().toString();
                    dailydb.date = getArguments().getInt("year") * 1000 + getArguments().getInt("month") * 100 + getArguments().getInt("day");
                    database.child("daily").child(Long.toString(dailydb.date)).child("2").child(cal.getTime().toString()).setValue(dailydb);
                    dismiss();
                    break;
                }

            case R.id.bt_cancel: //취소 버튼을 눌렀을 때
                dismiss();
                break;
        }
    }
}