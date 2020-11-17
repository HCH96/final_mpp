[secondPage.java]
1. 주요 기능 : 
-> firstpage에서 선택한 날짜의 할 일들을 시간표로 출력해준다.
-> 만약 firstpage의 날짜가 변경되면 변경된 날짜의 시간표를 출력한다.(황교민학우의 도움)
2. 구현
-> 1. onCreateView : 초기 화면의 상태를 지정
	- nullActionIdx(nullcontent의 index) 초기화
	- pieChart : 시간표를 나타내는 piechart
-> 2. changeState : database를 읽어 시간표를 출력
	- timetable : 하루 24시간을 10분단위로 쪼개 144개의 index로 나누어 각각의 index에는 해당하는 시간동안 
		     완료한 content를 저장한다.
		     ex) 02시 10분 >> 2 x 60 +10 = 130 (10분단위로 쪼개지므로 뒤에 있는 0의 값은 삭제) >> index 13 (02시 10분~ 02 20분)
		          00시 00분 ~ 02시 10분 까지 공부라는 content를 완료했을 경우 0부터 12까지의 index에 공부가 저장된다.
	- nullActionIdx : 아무 content도 하지 않은 시간대의 piechart의 색을 같은 색으로 지정하기 위해 할 일 index를 따로 생성해서
		          nullActionIdx에 따로 저장해 처리한다. (황교민학우의 도움)
	- 새로운 데이터가 들어올 때마다 기존 timetable을 clear하고 새로운 데이터를 받아와 데이터를 가공하여 화면에 piechart로 표시해준다.

*전체적인 UI는 김지원 학우의 도움을 받았습니다.


[Search.java]
1. 주요 기능 :
-> 검색창에 특정 문자를 검색하면 daily와 monthly에서 해당 문자가 포함된 데이터를 읽어와 각각의 Listview에 출력해준다.
-> 검색창에 변화가 있을 때마다 이를 반영해서 다시 검색한다.
2. 구현
-> 1. onCreate : 초기 화면을 지정
	- editSearch : 검색어를 입력 받는 EditText
	- listview : daily의 검색결과를 보여주는 ListView
	- listview2 : monthly의 검색결과를 보여주는 ListView
	- edittext.addTextchangeListener : edittext의 내용이 변할 때 마다 search, search2 메소드를 실행해서 다시 검색해서 보여준다.
	- setTextLength : 데이터를 읽어와 저장할 때 항목별로 간격을 맞춰주는 메소드
	- search, search2 : 문자를 입력 받을 때마다 사용자에게 보여주는 list를 초기화시키고 다시 검색해서 list에 뿌려준다.
	- insert : 문자열을 수정하거나 삽입하는 메소드
	- addValueEventListenner : 데이터가 변경 될 때 마다 daily 데이터베이스를 모두 돌면서 데이터를 받아온다.

-> 2. SearchAdapter.java : 사용자가 정의한 데이터를 Listview에 출력하기 위해 사용, 사용자 데이터와 화면 출력 View로 이루어진 두 개의 부분을 이어준다.
	- ListView에서 문자열을 출력하기 위해 TextView를 포함한다.
	- searchlistview.xml : ListView 아이템에 대한 Layout
	- list : 데이터들이 저장 될 list
	- class ViewHolder : TextView를 가지는 class, 동적으로 생성되어 list에 있는 데이터를 리스트뷰 셀에 뿌린다.

* 전체적인 UI는 김지원 학우의 도움을 받았습니다.

