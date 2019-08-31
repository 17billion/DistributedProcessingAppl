# DistributedProcessingAppl
### 프
　 - words.txt 파일의 단어를 복수의 파일로 분리하는 프로그램을 객체 지향 설계의 SOLID 원칙에 따라 구현 <br>
　　　· https://ko.wikipedia.org/wiki/SOLID_(객체_지향_설계) <br>

　 - words.txt 파일의 각 라인은 1개의 단어를 포함 <br>
　　　· 단어는 알파벳 또는 숫자로 시작하며 대소문자는 구분하지 않음 <br>
　　　· 알파벳이나 숫자가 아닌 문자로 시작하는 단어는 유효하지 않음 <br>
　　　　예) ab!23 (유효함), A12bd (유효함), 123abc (유효함), #abc (유효하지않음) <br>
    
### 요구사항
　  1. Maven 기반으로 프로젝트 구성 <br>
　  2. 프로그램의 실행 argument로 3개의 값을 입력 <br>
　　　- 처리해야 할 입력 파일명 <br>
　　　- 결과 파일들을 저장 할 디렉토리 경로 <br>
　　　- 병렬 처리를 위한 파티션 수 N (1 < N < 28) <br>
　  3. Producer-Consumer 디자인 패턴을 응용해 아래의 요구사항에 따라 로직 구현 <br>
　　　- Producer 요구사항 <br>
　　　　　1) 파일에서 각 라인을 읽어온다. <br>
　　　　　2) 각 라인의 주어진 단어가 유효한지 정규표현식을 활용해 검사한다.  <br>
　　　　　3) 유효하지 않은 단어들은 처리를 생략한다. <br>
　　　　　4) 유효한 단어들은 N개의 파티션으로 나눠서 Consumer에 전달한다. <br>
　　　　　　　　· 파티션 수는 프로그램의 실행 argument로 입력 받는다. <br>
　　　　　　　　· 동일한 단어는 항상 동일한 파티션에 포함되어야 한다. <br>
　　　- Consumer 요구사항 <br>
　　　　　1) 파티션에서 순차적으로 단어를 1개씩 가져온다. <br>
　　　　　2) 단어가 알파벳으로 시작한다면 단어의 첫 알파벳에 해당하는 파일 끝에 주어진 단어를 추가 해야한다. <br>
　　　　　　 예) apple, Apple은 a.txt 파일 끝에 추가 해야한다. (대소문자 구분없음) <br>
　　　　　3) 단어가 숫자로 시작한다면 number.txt 파일 끝에 주어진 단어를 추가 해야한다. <br>
　　　　　　 예) 1-point, 2-point는 number.txt 파일 끝에 추가 해야한다. <br>
　　　　　4) 주어진 단어가 대상 파일에 이미 쓰여진 단어인지 대소문자 구분 없이 중복검사를 수행하고, 중복되지 않은 단어라면 대상 파일 끝에 단어를 추가한다. <br>

　  4. 프로그램 종료시 사용된 리소스를 올바르게 정리 <br>
　  5. 주요 고려사항과 클래스 설계를 README.md에 서술 <br>


### 주요 고려사항
1) 요구사항에 맞게 프로세스 처리
2) 프로세스 실행 시 Producer, Consumer 각각 그룹으로 생성하여 Producer는 Single Thread, Consumer는 Multi Thread로 처리
3) 중간에 강제로 프로세스가 종료되어도 재실행 시 파라메터를 통해 시작점을 지정 가능하도록 설계 (4번째 파라메터 : seek / NOT REQUIRED)
	> 진행 중 각 Word별 위치 정보를 저장 (Thread 별 10,000개씩 처리될 때마다 로그에 위치 기록하도록 설정 / 위치 정보 : seek)
4) 프로세스 실행 중 words.txt 파일에 추가적으로 append되어도 실시간 처리가능
5) 외부에서 인터럽트를 통해 프로세스 종료가능 (THREAD-STATUS 파일의 내용을 "stop"으로 변경 후 저장 시 프로세스 종료)
6) 경과 사항을 로그에 기록 (dp.log, dp_err.log)

### Class Diagram
![Class Diagram](https://github.com/17billion/DistributedProcessingAppl/blob/master/DistributedProcessingAppl/class_diagram.gif)
- Main Class
	> 어플리케이션이 시작되는 Class
- ProducerGroup Class
	> Producer Thread를 실행시키는 Class
- ProducerThread Class
	> 실제 Producing을 하는 Class <br>
	> words.txt의 파일의 내용을 요구사항에 맞게 처리 후 ArrayList로 구성되어 있는 파티션(Queue)에 메세지를 추가 <br>
	> seek 파라메터가 있을 경우 해당 위치부터 처리 시작  <br>
	> 10000개씩 처리 되거나 더 이상 처리할 Word가 없을 경우 로그에 기록  <br>
	> THREAD-STATUS 파일의 내용이 "stop"으로 변경될 경우 종료 <br>
- ConsumerGroup Class
	> Consumer Thread를 실행시키는 Class
- ConsumerThread Class
	> 실제 Consuming을 하는 Class  <br>
	> 파티션 갯수와 동일한 Thread 생성(생성된 순서대로 파티션의 메세지 소비) <br>
	> 각 파티션(Queue) 내 Word를 poll하여 요구사항에 맞게 처리  <br>
	> Thread 별 10000개씩 처리 되거나 더이상 읽을 Word가 없을 경우 로그에 기록  <br>
	> THREAD-STATUS 파일의 내용이 "stop"으로 변경될 경우 종료 <br>
- ObjectFileRWriter Class 
	> 파일 입출력을 담당하는 Class
- Record Class 
	> Word 처리에 사용되는 VO Class <br>
	> word : 단어, seek : 단어가 끝나는 부분의 위치 정보
- Constants Class
	> Static Resource 관리

### 디렉토리 설명
- 소스코드 디렉토리 : <a href ='https://github.com/17billion/DistributedProcessingAppl/tree/master/DistributedProcessingAppl'>DistributedProcessingAppl</a>
- 실행 가능한 디렉토리 (jar) : <a href ='https://github.com/17billion/DistributedProcessingAppl/tree/master/ExecutionDirectory'> ExecutionDirectory </a>

### To start using DistributedProcessingAppl
1) ExecutionDirectory 디렉토리 다운로드
2) $ cd ExecutionDirectory
3) $ java -jar DistributedPocessing-0.1.jar {FileName} {Result Directory} {Partition  Count} {SEEK(NOT REQUIRED)} 실행
	> ex 기본) $ java -jar DistributedPocessing-0.1.jar words.txt result/ 7  <br>
	> ex 추가 파라메터) $ java -jar DistributedPocessing-0.1.jar words.txt result/ 7 3307412 (32000 라인의 pugmill 이후부터(pugmiller) 처리를 원할 경우)
4) 종료 시 THREAD-STATUS의 파일 내용을 "stop"으로 변경 후 저장

### 문의사항
Email. 17earlgrey@gmail.com <br>
Blog. https://17billion.github.io/
