[firstPage.java]
1. 주요 기능
   > DatePicker Dialog를 통해 선택한 날짜의 할 일 리스트를 표시한다.
   > 계획의 효율 혹은 할 일의 구분을 위해 3분할 할 일 리스트를 구현하였다.
   (예시: 아침/점심/저녁, 프로젝트1/프로젝트2/프로젝트3, 일상/회사/루틴)
   > 3분할 할 일 리스트의 구분은 사용자별 활용방법이 다르므로, 색으로 구분하여 표현하였다.
   > 완료된 할 일은 3분할 할 일 리스트의 하단에 표시된다.
   > 완료된 할 일의 구분을 위해 회색조로 표현함과 동시에 완료 체크버튼을 보이지 않게 하였다.
   * 3분할로 나누어 리스트가 출력되도록 하는 부분은 황교민 학우의 도움을 받았습니다.
   * 날짜 이동 후 할 일 추가 시 이동된 날짜가 아닌 현재 날짜에 할 일이 추가되는 문제가 있었으나 황교민 학우의 도움을 받아 해결하였습니다. 

2. 구현
   1) onCreateView: 초기 화면 상태 지정
	- bt_date: 날짜 표시 및 날짜 선택 DatePickerDialog를 호출하는 버튼 연결
		setText를 통해 어플 구동 시 현재 날짜가 표시되도록 설정
	- bt_add: 할 일 추가 버튼 연결
	- showDailyTodo: 할 일 리스트를 추가할 Linear Layout 연결
	- bt_date.setOnClickListener: setText를 통해 출력되는 날짜 클릭 시 DatePickerDialog가 호출되어 날짜 선택 가능
	- bt_add.setOnClickListener: 할 일 추가 버튼 클릭 시 TodoAddedDialog가 호출됨
   2) onDateSet: 할 일 리스트에 추가 및 출력
	- bt_date: setText를 통해 DatePickerDialog에서 선택된 값으로 날짜 출력을 변경
	- database.child("daily").addValueEventListener: "daily" 하단 DB정보에 변동이 생길 경우 할 일을 출력하기 위해 호출됨
		> tr.setOnClickListener: 할 일 클릭 시에 수정이 가능하도록함
				    (기존 정보를 번들에 넣어 TodoAddedDialog(Fragment)로 보냄)
		> cb.setOnClickListener: 완료 클릭 시에 해당 할 일 DB를 기존 위치에서 삭제 후 timeline index 3(완료)에서 재생성함
				    (시간 정보 업데이트를 위해 기존 정보를 번들에 넣어 Status(Fragment)로 보냄)
   3) setting: 어플 구동 시 할 일이 표시되도록 하기 위해 사용
	   (onDateSet의 database.child("daily").addValueEventListener 내의 내용과 동일하게 구현됨)



[TodoAddedDialog.java]
1. 주요 기능
   > 할 일 추가 팝업의 기능을 담당한다.
   > 할 일의 내용을 입력받고, 3분할 리스트 중 어떤 그룹에 위치할 것인지와 할 일의 카테고리를 입력받는다.
   * spinner 연동에 문제가 있었지만 황교민 학우의 도움을 받아 해결하였습니다.
   * 할 일 수정 및 삭제가 가능하도록 하는 부분은 황교민 학우의 도움을 받아 해결하였습니다.

2. 구현
   1) getInstance: 새로운 TodoAddedDialog 생성 및 return
   2) onCreateView: 초기 화면 상태 지정
	- content: 할 일 내용 EditText 연결
	- bt_cancel: 등록 취소 버튼 연결
	- bt_listUp: 할 일 등록 버튼 연결
	- timeGroup: 시간 그룹(radioGroup) 연결
	- delBtn: 할 일 삭제 버튼 연결
	- delBtn.setOnClickListener: 해당 할 일의 DB를 삭제함
		> 삭제 클릭 시 "삭제되었습니다." Toast 메세지 출력
	- bt_cancel.setOnClickListener: onClick에 구현된 함수 호출
		> 취소 클릭 시 "취소되었습니다." Toast 메세지 출력
	- bt_listUp.setOnClickListener: onClick에 구현된 함수 호출
		> 할 일이 입력된 경우에만 등록 가능
		> 기존 할 일 수정 시, firstPage에서 번들에 넣어온 정보를 활용하여 수정함
		> 할 일 등록 시 DB를 업데이트
		> 수정 및 등록 완료 시 "수정/등록되었습니다." Toast 메세지 출력
   3) onClick: 등록과 취소 버튼 클릭 시 호출될 함수 구현



[Status.java]
1. 주요 기능
   > 완료 체크 시 시간 정보를 입력받는 팝업의 기능을 담당한다. (DB정보 업데이트)
   > 시작 시간과 종료 시간을 입력받는다.
   > 시간 정보가 없는 경우 시간 미지정을 체크하도록 구현하였다.
   * 기존에 등록된 시간과 중복되는 경우의 예외처리는 황교민님의 도움을 받았습니다.
 
2. 구현
   1) getInstance: 새로운 Status생성 및 return
   2) onCreateView: 초기 화면 상태 지정
	- et_startTime: 시작 시간 입력 EditText 연결
	- et_endTime: 종료 시간 입력 EditText 연결
	- bt_cancelDone: 취소 버튼 연결
	- bt_done: 등록 버튼 연결
	- cb_none: 시간 미지정 체크박스 연
	- onDataChange: 완료 할 일이 추가된 경우 시작시간과 종료시간을 각각 array에 담아줌
		> 시간 중복 여부 체크를 위함
	- et_startTime.setOnClickListener: TimePickerDialog를 통해 시작 시간 정보를 입력받음
		> DB정보 업데이트를 위해 시작시간 정보를 variables에 담아줌
		> 시간 미지정 체크박스가 체크되어 있는 상태에서 시간 정보가 입력될 경우 시간 미지정 체크 해제함
	- et_endTime.setOnClickListener: TimePickerDialog를 통해 종료 시간 정보를 입력받음
		> DB정보 업데이트를 위해 종료시간 정보를 variables에 담아줌
		> 시간 미지정 체크박스가 체크되어 있는 상태에서 시간 정보가 입력될 경우 시간 미지정 체크 해제함
	- cb_none.setOnClickListener: 시간 미지정 체크박스 체크 시 시간 정보 초기화함
	- bt_cancelDone.setOnClickListener: 취소 클릭 시 "취소되었습니다." Toast 메세지 출력
	- bt_done.setOnClickListener: 시간 정보 업데이트 후 timeline index 3(완료)로 DB 업데이트
		> 시간 정보가 입력되지 않은 경우, 잘못 입력된 경우 안내 문구를 표시하고 시간 정보가 등록되지 못하도록 함
		> 시간 미지정에 체크된 경우 시간 정보 0으로 변경해준 후 기존 DB 삭제 후 timeline index 3(완료)로 DB 업데이트
		> 시간 정보가 올바르게 입력된 경우 기존 DB 삭제 후 timeline index 3(완료)로 DB 업데이트
		> 기존 시간 정보와 중복되는 경우 안내 문구를 표시해줌
		> 올바르게 시간 정보가 입력된 후 등록되는 경우 "할 일을 완료하였습니다." Toast 메세지 출력




[Stats.java]
1. 주요 기능
   > 완료된 할 일의 시간 정보를 통해 할 일 등록 시 입력한 카테고리 별 사용 시간의 월 별 통계를 보여줌
   * 달이 변경될 경우 해당 월의 통계 정보를 받아오는 부분은 황교민님의 도움을 받았습니다.

2. 구현
   1) onCreate: 통계 파이차트를 보여줌
	- onDataChange: timeline index 3(완료)에 DB가 추가될 경우 호출되어 파이차트를 업데이트함
		> 시간 정보를 받아와 분으로 변환하여 저장
		> 카테고리 별 시간을 누적하여 저장함
   2) goToMain: MainActivity와 연결