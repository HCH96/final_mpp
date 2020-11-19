package org.tech.mobileprogrammingproject.Daily;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.tech.mobileprogrammingproject.FIREBASEDB.DailyDB;
import org.tech.mobileprogrammingproject.R;
import static org.tech.mobileprogrammingproject.Daily.firstPage.dateTime;
import java.util.ArrayList;
import java.util.Calendar;

    /**
      * secondPage.java
      * 주요 기능 : 당일 완료한 일을 24시간제 원형 시간표로 표시
      * Written by 한창희
     */

public class secondPage extends Fragment {

    // null Action에 대해서 같은 색으로 처리하기 위해서
    public static ArrayList<Integer> nullActionIdx = new ArrayList<>();

    // 몇개의 할일 덩어리가 있는지 파악하기 위해서
    public int idx;
    static DatabaseReference database = null;
    public static PieChart pieChart;
    Calendar cal = Calendar.getInstance();
    ViewGroup rootView;

    /*
    하루 24시간을 10분단위로 나누어서 144개의 index를 가진 Arraylist로 표현한다.
    ex) 2시 10분 >> 2x60+10 = 130이므로 index는 13
    */
    static ArrayList<String> timetable = new ArrayList<>(144);


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        rootView = (ViewGroup) inflater.inflate(R.layout.secondpage, container, false);
        nullActionIdx.clear();
        pieChart = rootView.findViewById(R.id.picChart);
        /*
            2020.11.06 황교민
            null Action에 대해서 같은 색상으로 표시하기 위해서 nullActionIdx를 받아옴.
            nullActionIdx에 해당하는 곳에는 하나의 color를 할당하고, 나머지는 미리 생성한 colorForAct에서 순차적으로 color을 받아옴.
            next Thing :
            1. 시간이 만약에 잘못 입력되었으면 어떻게 할 것인지(아마도 firstpage에서 처리할 듯 하다.)
            2. 만약에 표시할 수 없을 정도로 작은 할일은 어떻게 처리할 것인가(즉 10분 미만의 할일)
             => 아마도 layout을 하나 더 만들어서 동적으로 할일을 표시하면 가능할 듯 하다.
         */
        return rootView;
    }

    //새로운 data가 추가 될 때마다 reflesh하는 메소드
    public static void changeState(String date) {

        //새로운 database를 반영하기위해 기존 timetable 초기화
        timetable.clear();
        for (int i = 0; i < 144; i++) {
            timetable.add(" ");
        }

        //get database reference
        FirebaseDatabase mdata = FirebaseDatabase.getInstance();
        DatabaseReference mRef = mdata.getReference("daily/" + date + "/3");


        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot Snapshot : snapshot.getChildren()) {

                    //database에서 원하는 데이터(starttime,endtime,content)를 읽기
                    DailyDB get = Snapshot.getValue(DailyDB.class);

                    //읽어온 데이터를 이용하기 위해 내가 원하는 형식으로 변환
                    int starttime = ((get.startTime / 100) * 60 + (get.startTime - (get.startTime / 100) * 100)) / 10;
                    int endtime = ((get.endTime / 100) * 60 + (get.endTime - (get.endTime / 100) * 100)) / 10;
                    String content = get.content;

                    //변환된 데이터를 timetable에 저장
                    for (int i = starttime; i < endtime; i++) {
                        timetable.set(i, content);
                    }

                }

                //원형차트 데이터 생성
                ArrayList<PieEntry> piedata = new ArrayList<>();
                float piesize = 0f; //항목당 차지하는 pie의 크기를 나타내는 변수
                String precontent = timetable.get(0); //항목을 구분해주기위해 이전 index의 항목을 저장해주는 변수(초기값은 timetable의 첫번째 값)
                int idxSub = 0;
                nullActionIdx.clear();
                if (precontent.equals(" ")) nullActionIdx.add(idxSub);
                // 처음에 덩어리를 생성하여 오류가 계속 발생하였습니다.
                //piedata.add(new PieEntry(piesize, precontent));

                //모든 timetable을 돌면서 할일 항목 덩어리마다 데이터를 pieEntry로 저장해준다.
                for (int i = 0; i < timetable.size(); i++) {

                    //이전 index의 content와 현재 index의 context가 다르다면 이전 content는 그 시간부로 끝난 것이므로 데이터를 저장한다.
                    if (precontent != timetable.get(i)) {
                        piedata.add(new PieEntry(piesize, precontent));
                        piesize = 0f; //pie 크기 데이터를 이미 넘겨주었으므로 다시 0으로 초기화
                        precontent = timetable.get(i); // 새로운 content의 데이터를 처리해야 하므로 precontent의 값도 다음 항목의 데이터로 지정
                        idxSub++; //다음 항목으로 넘어갔으므로 1을 더해준다.
                        if (precontent.equals(" ")) nullActionIdx.add(idxSub); //만약 처리한 content가 null content일 경우 색을 같게 해주기위해 index를 따로 저장한다.
                    } else {
                        //이전 index와 현재 index의 content가 같다면 content가 끝나지 않은 것이므로 index만큼의 pie의 크기를 더해준다.
                        piesize = piesize + 1f;
                        precontent = timetable.get(i);
                    }
                }
                //마지막 index 할 일의 pieentry 데이터를 추가
                piedata.add(new PieEntry(piesize, precontent));

                //piedata로 넘겨줄 컬러를 저장하는 list
                ArrayList<Integer> colors = new ArrayList<Integer>();
                //사용할 color를 저장해주는 list
                ArrayList<Integer> colorForAct = new ArrayList<>();
                //ColorTemplate.LIBERTY_COLORS로부터 color들을 저장
                for (int c : ColorTemplate.LIBERTY_COLORS)
                    colorForAct.add(c);
                int colorIdx = 0;
                //idxsub를 탐색하면서 색깔을 지정
                for (int i = 0; i < idxSub + 1; i++) {
                    if (nullActionIdx.contains(i)) { //만약 idxsub에 저장된 content가 null content라면 한가지 색으로 통일
                        colors.add(Color.parseColor("#f6f6f6"));
                    } else { //null content가 아니라면 모든 content의 색을 다르게 함
                        colors.add(colorForAct.get(colorIdx++));
                    }
                }

                //dataset 생성
                PieDataSet pieDataSet = new PieDataSet(piedata, "오늘 한 일");
                pieDataSet.setSelectionShift(5f);//pie chart 크기

                pieDataSet.setValueLinePart1Length(0.6f);
                pieDataSet.setValueLinePart2Length(0.3f);
                pieDataSet.setValueLineWidth(2f);
                pieDataSet.setValueLineVariableLength(true);
                pieDataSet.setUsingSliceColorAsValueLineColor(true);
                pieDataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
                pieDataSet.setValueTypeface(Typeface.DEFAULT_BOLD);

                //piedata에 color를 넘겨줌
                pieDataSet.setColors(colors);

                //piedata에 piedataset 전달
                PieData pieData = new PieData(pieDataSet);
                pieData.setValueTextSize(0);//pie의 크기

                pieChart.clear();
                pieChart.setEntryLabelColor(Color.BLACK); //Label color
                pieChart.setDrawEntryLabels(true); //Label 표시
                pieChart.setRotationEnabled(false); //piechart 회전x
                pieChart.setUsePercentValues(false); //퍼센트로 표시x
                pieChart.setData(pieData); //data 전달

                /*
                2020.11.14 김지원
                pieChart UI 수정
                */
                pieChart.setDrawHoleEnabled(false); // 차트 가운데 hole 제거
                pieChart.getLegend().setEnabled(false); // 범례 안 보이도록 설정
                pieDataSet.setSliceSpace(0.0f); // 그래프 조각 사이 여백 설정
                pieChart.getDescription().setEnabled(false); // 설명 레이블 제거


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

}
