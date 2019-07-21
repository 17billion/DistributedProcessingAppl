# DistributedProcessingAppl

### 주요 고려사항
1) 요구사항에 맞게 프로세스 처리
2) 프로세스 실행 시 Producer, Consumer 각각 그룹으로 생성하여 싱글 스레드로 처리
3) 경과 사항을 로그에 기록 (dp.log, dp_err.log)
4) 진행 중 각 Word별 위치 정보를 저장 (위치 정보 : SEEK)
	> 중간에 강제로 프로세스가 종료되어도 재실행 시 파라메터를 통해 시작점을 지정 가능하도록 설계 (4번째 파라메터 : SEEK / NOT REQUIRED)
5) 진행 중 words.txt 파일에 append되어도 추가적으로 처리가능하도록 설계
6) 외부에서 인터럽트를 통해 프로세스 종료되도록 처리 (THREAD-STATUS의 내용을 "stop"으로 변경 후 저장 시 프로세스 종료)

### Class Diagram
![Class Diagram](https://github.com/17billion/DistributedProcessingAppl/blob/master/DistributedProcessingAppl/class_diagram.gif)
- Main Class
	> 어플리케이션이 시작되는 Class
- Constants Class
	> Static Resource 관리
- Record Class 
	> Word 처리에 사용되는 VO Class / word : word, seek : word가 끝나는 부분의 위치 정보
- ProducerGroup
	> Producer Thread를 실행시키는 Class
- ProducerThread
	> 실제 Producing을 하는 Class <br>
	> words.txt의 파일의 내용을 요구사항에 맞게 처리 후 ArrayList로 구성되어 있는 파티션(Queue)에 메세지를 추가 <br>
	> seek가 있을 경우 해당 위치부터 처리 시작  <br>
	> 10000개씩 처리 되거나 더 이상 처리할 Word가 없을 경우 로그에 기록  <br>
	> THREAD-STATUS 파일의 내용이 stop으로 변경될 경우 종료 <br>
- ConsumerGroup Class
	> Consumer Thread를 실행시키는 Class
- ConsumerThread Class
	> 실제 Consuming을 하는 Class  <br>
	> ArrayList로 된 각 파티션(Queue)내 Word를 poll하여 요구사항에 맞게 처리  <br>
	> 10000개씩 처리 되거나 더이상 읽을 Word가 없을 경우 로그에 기록  <br>
	> THREAD-STATUS 파일의 내용이 stop으로 변경될 경우 종료 <br>
- ObjectFileRWriter Class 
	> 파일 입출력을 담당하는 Class

### 디렉토리 설명
- 소스코드 디렉토리 : DistributedProcessingAppl
- 실행 가능한 디렉토리(jar) : ExecutionDirectory

### To start using DistributedProcessingAppl
1) ExecutionDirectory 디렉토리 다운로드
2) $ cd ExecutionDirectory
3) java -jar DistributedPocessing-0.1.jar {FileName} {Result Directory} {Partition  Count} {SEEK(NOT REQUIRED)} 실행
	> ex 1) java -jar DistributedPocessing-0.1.jar word.txt result/ 7  <br>
	> ex 2) java -jar DistributedPocessing-0.1.jar word.txt result/ 7 1519591 (gaine 단어부터 처리를 원할 경우)
4) 종료 시 THREAD-STATUS의 파일 내용을 stop으로 변경 후 저장

### 문의사항
Email. 17earlgrey@gmail.com <br>
Blog. https://17billion.github.io/
