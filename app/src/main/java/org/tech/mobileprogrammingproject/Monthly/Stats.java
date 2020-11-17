package org.tech.mobileprogrammingproject.Monthly;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.tech.mobileprogrammingproject.Daily.MainActivity;
import org.tech.mobileprogrammingproject.Daily.firstPage;
import org.tech.mobileprogrammingproject.FIREBASEDB.DailyDB;
import org.tech.mobileprogrammingproject.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Stats.java
 * 주요 기능: 카테고리 별 시간 통계
 *
 * 2020.11.14 (마지막 Comment 수정)
 * @author 김지원
 */

public class Stats extends AppCompatActivity {

    //variables
    PieChart pieChart; // 차트 생성
    int category0_sumTime = 0; // category0(미정)의 시간합계를 저장할 변수
    int category1_sumTime = 0; // category1(공부)의 시간합계를 저장할 변수
    int category2_sumTime = 0; // category2(과제)의 시간합계를 저장할 변수
    int category3_sumTime = 0; // category3(운동)의 시간합계를 저장할 변수

    // Firebase DatabaseReference
    DatabaseReference database = null;
    DailyDB doneDB = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stats);

        // firebase 연결
        database = FirebaseDatabase.getInstance().getReference();

        // pieChart layout 연결
        pieChart = (PieChart) findViewById(R.id.piechart);

        /*
        통계 구현할 data 생성 (from Firebase)
        완료 카테고리(timeline == 3)에 데이터가 추가될 경우 startTime과 endTime을 받아옴
        이 때 시간 정보를 분으로 변환해서 저장
        */
        database.child("daily").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Intent intent = getIntent();
                for (final DataSnapshot childData : snapshot.getChildren()) {
                    if(childData.getKey().substring(3,5).equals(intent.getStringExtra("month"))) {
                        database.child("daily/" + childData.getKey()).child("3").addValueEventListener(
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot2) {
                                        for (DataSnapshot timeData : snapshot2.getChildren()) {
                                            doneDB = timeData.getValue(DailyDB.class);

                                            // variables
                                            int startTime;
                                            int endTime;

                                            // 시작 시간와 종료 시간 정보를 각각 분으로 변환해서 저장
                                            if ((int) (Math.log10(doneDB.startTime) + 1) == 2)
                                                startTime = ((doneDB.startTime / 10) * 60 + doneDB.startTime - (doneDB.startTime / 10));
                                            else
                                                startTime = ((doneDB.startTime / 100) * 60 + doneDB.startTime - (doneDB.startTime / 100));
                                            if ((int) (Math.log10(doneDB.endTime) + 1) == 2)
                                                endTime = ((doneDB.endTime / 10) * 60 + doneDB.endTime - (doneDB.endTime / 10));
                                            else
                                                endTime = ((doneDB.endTime / 100) * 60 + doneDB.endTime - (doneDB.endTime / 100));

                                            // category 별로 분류하여 합계 시간을 누적하여 저장해줌
                                            if (doneDB.catalog.equals("미정"))
                                                category0_sumTime += endTime - startTime;
                                            else if (doneDB.catalog.equals("공부"))
                                                category1_sumTime += endTime - startTime;
                                            else if (doneDB.catalog.equals("과제"))
                                                category2_sumTime += endTime - startTime;
                                            else if (doneDB.catalog.equals("운동"))
                                                category3_sumTime += endTime - startTime;

                                            // pie chart 기본 옵션 설정
                                            pieChart.setUsePercentValues(false); // 퍼센트값을 제외한 실제 값으로 설정 (단위: 분)
                                            pieChart.getDescription().setEnabled(false); // 설명 레이블 제거
                                            pieChart.setExtraOffsets(10, 10, 10, 5); // 차트 주위 여백 설정
                                            pieChart.setDrawHoleEnabled(false); // 차트 가운데 hole 제거
                                            pieChart.setTouchEnabled(false); // 애니메이션 제거
                                            pieChart.getLegend().setEnabled(false); // 범례 안 보이도록 설정

                                        /*
                                        통계 값 넣어주기
                                        value: category별 수행 시간
                                        label: category
                                        */
                                            ArrayList<PieEntry> yValues = new ArrayList<>();
                                            if(category0_sumTime != 0) yValues.add(new PieEntry(category0_sumTime, "미정"));
                                            if(category1_sumTime != 0) yValues.add(new PieEntry(category1_sumTime, "공부"));
                                            if(category2_sumTime != 0) yValues.add(new PieEntry(category2_sumTime, "과제"));
                                            if(category3_sumTime != 0)yValues.add(new PieEntry(category3_sumTime, "운동"));
                                            // 데이터 추가 후 새로고침
                                            pieChart.notifyDataSetChanged();
                                            pieChart.invalidate();

                                            // 그래프 세팅
                                            PieDataSet dataSet = new PieDataSet(yValues, "");
                                            dataSet.setSliceSpace(3f); // 그래프 조각 사이 여백 설정

                                            // 그래프 색상 설정
                                            List<Integer> colors = new ArrayList<>();
                                            colors.add(Color.parseColor("#809cc9"));
                                            colors.add(Color.parseColor("#3b5998"));
                                            colors.add(Color.parseColor("#8ca1af"));
                                            colors.add(Color.parseColor("#607d8b"));
                                            dataSet.setColors(colors);

                                            // PieChart에 표시되는 value값 형태 지정
                                            PieData data = new PieData((dataSet));
                                            data.setValueTextSize(15f); // 사이즈
                                            data.setValueTextColor(Color.parseColor("#222222")); // 색상
                                            dataSet.setValueTypeface(Typeface.DEFAULT_BOLD); // 굵기
                                            pieChart.setData(data);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}