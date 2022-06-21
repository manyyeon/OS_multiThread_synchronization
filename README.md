# OS_multiThread_synchronization
멀티미디어운영체제 과제3
Multi Thread와 Critical Section을 이용한 악성코드 패턴 탐지 시스템 구현
구현 언어 : java

# 목차
1.	Critical Section Synchronization 문제를 해결하기 위한 방법
2.	전체적인 구조
3.	프로그램 동작 방식
4.	실행결과
5.	소스코드
 
# 1.	Critical Section Synchronization 문제를 해결하기 위한 방법
이 시스템에서 critical sction은 9개의 테스트 파일이다. 9개의 테스트 파일은 동시에 검사하지만 1개의 파일을 여러 스레드가 동시에 접근하면 안된다. 이 문제를 해결하기 위해 세마포어와 lock 변수를 사용했다. Java에서 제공하는 Semaphore API 클래스를 활용하였다.
<img width="417" alt="image" src="https://user-images.githubusercontent.com/87538540/174722105-6f008b53-41cc-4ce5-80a3-f57633a2d702.png">

<img width="417" alt="image" src="https://user-images.githubusercontent.com/87538540/174722124-dbd59134-5dfb-48da-83ca-a4107aa1a067.png">

자바 API 문서를 보면 첫번째 파라미터는 공유 자원의 개수를 의미하고 이걸 이제부터 S라고 하겠다. Semaphore에는 binary semaphore와 counting semaphore가 있다. binary semaphore는 공유 자원이 2개인 거고 counting semaphore는 공유 자원이 여러 개인 것이다. 
S의 개수를 정해주면 누군가 들어와서 자원 한 개를 가져가려고 할 때 S의 숫자를 본다. S > 0이면 들어와서 공유 자원 하나를 점유하고 S – 1을 한다. 점유를 끝내고 나갈 때 다시 S + 1을 하고 나간다. 여기서 S > 0인지 확인하고 S-1을 한 후 cs에 접근하게 해주는 함수가 acquire() 함수이다. 그리고 cs가 끝났을 때 S + 1을 하고 공유자원을 방출하게 하는 함수가 release() 함수이다. S = 0 이면 남은 공유 자원이 없다는 뜻이므로 대기한다. S가 다시 양수가 될 때까지 대기한다. 대기 상태에 들어간 스레드들은 List에 저장이 된다. 사용 가능한 공유 자원이 다시 생기면 이 List에 들어가 있는 스레드들 중에 하나를 깨워서 공유 자원을 사용하게 한다. 이 때, 누구를 먼저 깨울지 결정해야 하는데 Semaphore 클래스 생성자 2번째 파라미터로 들어가는 fair에 의해 결정된다. true이면 FIFO 방식으로 제일 처음 들어갔던 스레드를 깨우는 것이고 false이면 무작위로 아무 스레드나 깨운다.
 악성코드 탐지 시스템에서는 공유 자원이 9개이다. 따라서 counting semaphore를 사용했다.
 
<img width="536" alt="image" src="https://user-images.githubusercontent.com/87538540/174722337-f871fb85-0d8d-4ba0-aef7-64e34f16d470.png">


```
// 세마포어 객체 생성 (Counting Semaphore)
private Semaphore semaphore = new Semaphore(TEST_CODE_NUM, true);
```

첫번째 파라미터로는 테스트 파일의 개수인 9를 넣어주어서 S = 9가 되었고 두번째 파라미터로는 true를 넣어주어서 대기하는 스레드를 깨울 때 FIFO 방식을 사용하였다.
 전체적인 알고리즘 작동 방식은 다음과 같다.
스레드가 20개 있다고 하자. 20개의 스레드는 모두 동시에 start해서 공유 자원에 접근하려는 시도를 한다. 그 중 먼저 S를 얻는 9개의 스레드들만 Critical Section에 들어가는 것이다. 나머지 스레드들은 대기하다가 다른 스레드가 100글자를 다 읽거나 그 파일을 모두 읽어서 공유 자원인 파일 점유를 끝내고 나오게 되면 그 때 들어갈 수 있는 구조이다. 9개의 스레드들이 cs 안으로 들어간 다음에 어떤 테스트 파일이 읽을 수 있는 상태인지 탐색하게 된다. 파일을 끝까지 다 읽지 않았고 다른 스레드가 읽는 중이 아닌 파일을 찾으면 그 파일에 lock을 걸고 그 파일을 읽기 시작한다. 그리고 100글자를 다 읽을 때까지 계속 한 글자씩 읽으면서 악성코드가 있나 검사를 한다. 만약 100글자를 다 읽었는데 이 파일에 읽을 글자들이 아직 남아 있다면 다른 스레드가 또 와서 계속 읽어야 하므로 lock을 풀고 isTerminated = true로 설정한 후 종료한다. 아니면 100글자를 아직 다 읽지 못했는데 파일을 다 읽어서 끝나버릴 수도 있다. 그러면 아직 다 안 읽은 파일이 존재하는지 검사를 하고 만약에 더 이상 읽을 파일이 없다면 isTerminated = true로 설정하고 종료한다. 아직 100글자를 다 읽지 못했어도 더 이상 읽을 파일이 없으면 어차피 또 읽으러 들어가봤자 읽을 파일이 없어서 다시 나오게 될 거고 또 들어가고 또 나오고 이렇게 무한루프에 빠지게 된다. 만약에 더 읽을 파일들이 남아 있다면 그냥 나간다. 그럼 다시 스레드 run() 함수에서 다시 use() 함수를 호출하여 공유 자원을 획득하러 들어갈 것이다.
 
# 2.	전체적인 구조
총 5개의 클래스로 구성되어 있다.

## 1)	Main class
스레드 개수를 입력받고 스레드 객체를 생성하고 모든 스레드들을 start 시킨다. 그리고 마지막에 최종 결과인 정상 파일로 분류된 파일들과 악성 파일로 분류된 파일들을 출력한다.
Runnables 배열에 객체를 생성할 때 스레드 id와 공유 객체인 악성코드 검사 시스템을 파라미터로 넘겨준다.
파일을 먼저 다 불러온 후에 악성코드 검사 시작하는 버튼을 누르면 검사가 시작된다.

### 변수
```
int threadNum = 0; // 스레드 총 개수
Runnable[] runnables; // runnable 객체 배열
Thread[] threads; // 스레드 배열
ImportFiles importFiles = new ImportFiles(); // 파일 읽어오는 객체
MalwareTestSystem malwareTestSystem; // 공유 객체 악성코드 검사 시스템 생성
JButton startButton = importFiles.startButton; // 악성코드 검사를 시작하는 버튼
```

---------

## 2)	ImportFiles Class
파일을 Import하기 위한 GUI를 구현해놓은 클래스이다.
### 변수
```
Container c = getContentPane();
JButton startButton = new JButton("여기를 누르면 악성코드 검사 시작(파일을 먼저 import 한 후에 시작하세요)");
String [] testFilePathList; // 테스트 파일 경로
String [] testFileNameList; // 테스트 파일 이름
String [] malwareFilePathList; // 악성 코드 파일 경로
String [] malwareFileNameList; // 악성 코드 파일 이름
버튼을 눌러서 파일들을 불러오면 테스트 파일 경로와 이름, 악성코드 파일 경로와 이름을 배열에 저장해놓는다.
```

-------

## 3)	MalwareTestThread class
악성코드를 탐지하는 스레드 클래스이다. Runnable 인터페이스를 implements하여 스레드 클래스를 만들었다. 
### 변수
```
public static int MAX_AVAILABLE_READ_NUM = 100; // 스레드가 읽을 수 있는 최대 문자 개수
int countLetterNum = 0; // 스레드가 읽은 글자 개수
int tid; // 이 스레드 id(tid)
MalwareTestSystem malwareTestSystem; // 스레드들이 공유하는 악성코드 검사 시스템 선언
TestObj currentTestObj; // 현재 검사하고 있는 테스트 코드
boolean isTerminated = false; // 스레드가 종료되었는지
boolean isNeedNextStartTest; // 끊긴 일치한 부분 검사가 필요한지
```

스레드가 읽을 수 있는 최대 문자 개수를 상수로 정의 해두었다. 그리고 스레드가 읽은 문자 개수를 계속 업데이트 해주면서 최대 문자 개수와 같아질 때까지 스레드를 계속 실행한다. 더 이상 읽을 파일이 없어서 악성코드 탐지 시스템 안에서 스레드를 종료 시키기 위한 변수인 isTerminated가 있다. 현재 검사하고 있는 테스트 파일이 무엇인지 알기 위해서 currentTestObj 변수를 선언했다. 이 변수는 TestObj 클래스의 객체이다. TestObj 클래스는 뒤에서 설명할 거지만 테스트 파일의 정보를 담고 있다. 스레드 id인 tid 변수와 스레드들이 공유하는 악성코드 검사 시스템 객체가 선언되어있다. 이 두 변수는 Main 함수가 이 클래스의 객체를 만들 때 넘겨받는다. 그리고 isNeedNextStartTest는 악성코드를 검사할 때 스레드가 검사를 하다가 중간에 나오게 되면 악성코드가 부분적으로 일치하는 부분을 찾았는데 다음에 다른 스레드가 들어갈 때 이 끊긴 부분이 악성코드와 전체적으로 일치하는지 검사를 해줘야 한다. 그래서 malwareTestSystem에 그 검사를 하는 코드를 작성했고 스레드가 파일을 읽기 시작하는 처음 인덱스에서만 검사를 진행할 수 있도록 이 변수를 두었다. 이 변수가 true일 때만 그 검사를 진행한다.

### 함수
```
@Override
public void run() {
    // 이 스레드가 종료되었다고 알려주는 isTerminated가 true이거나 스레드가 100글자를 다 읽을 때까지 계속 공유 객체로 들어가서 use 함수를 실행함
    while(isTerminated == false && countLetterNum < MAX_AVAILABLE_READ_NUM) {
        // 악성코드 검사 시스템의 use 함수 실행(파라미터로 이 스레드 객체를 넘겨줌)
        malwareTestSystem.use(this);
    }
}
```

run() 함수에서 이 스레드가 종료되었다고 알려주는 isTerminated 변수가 true이거나 스레드가 100글자를 다 읽을 때까지 계속 공유 객체로 들어가서 use() 함수를 실행한다. malwareTestSystem 클래스의 함수인 use() 함수에서는 세마포어가 작동한다. S > 0 이면 공유자원을 획득하고 cs로 들어가고 S = 0이면 대기한다. 공유 자원을 획득하고 들어갔다가 방출하고 나오게 되는데 이 때 또 while문의 조건을 만족하면(아직 글자 100개를 다 못읽었고 isTerminate == true) 다시 들어가는 것이다. 다시 들어가서 또 다른 파일을 검사할 수 있다. 100 글자를 다 읽을 때까지 반복한다.

---------

## 4)	MalwareTestSystem class(공유자원)
이 클래스는 공유 자원이다. 여기서 Semaphore 객체를 생성하고 S > 0일 때만 공유 자원(파일)에 접근하고 S = 0이면 대기하는 것이다.
-	변수
public static final int TEST_CODE_NUM = 9;
private Semaphore semaphore = new Semaphore(TEST_CODE_NUM,true); // 세마포어 객체 생성 (Counting Semaphore)
Malware Spyware; // 악성코드 Spyware
Malware Trojan; // 악성코드 Trojan
TestObj [] testObjs = new TestObj [TEST_CODE_NUM]; // 검사할 파일 코드들
검사할 악성코드들 변수를 선언했다. Spyware와 Trojan 2개이다. Malware 클래스는 악성코드에 대한 정보를 담고 있는 클래스이다. 그리고 검사할 파일 코드들 배열을 선언했다. TestObj 클래스는 검사할 파일의 정보를 담고 있는 클래스이다.
-	use() 함수
public void use(MalwareTestThread currentThread) {
    try {
        semaphore.acquire(); // 공유 자원 획득(S-1)
        System.out.println(currentThread.tid + "번 스레드 세마포어 시작");
        currentThread.isNeedNextStartTest = true; // 진입해서 끊긴 부분 검사
        searchMalwareCode(currentThread); // 스레드가 공유 자원을 획득할 때마다 이 함수를 호출함
        Thread.sleep(new Random().nextInt(100));
        semaphore.release(); // 공유 자원 방출(S+1)
        System.out.println(currentThread.tid + "번 스레드 세마포어 종료");
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
}
use() 함수에서 cs 관리를 한다. semaphore.acquire()과 semaphore.release()로 최대 9개까지의 스레드가 공유 자원에 접근할 수 있도록 한다.
-	악성코드를 탐지하는 함수
이 함수에서 악성코드를 탐지한다.
public void searchMalwareCode(MalwareTestThread currentThread) {
    String subTestCodeSpyware;
    String subTestCodeTrojan;
    String subSpyware;
    String subTrojan;

    // 어떤 test 파일 코드가 읽을 수 있는 상태인지 탐색
    for (int i = 0; i < TEST_CODE_NUM; i++) {
        // 파일을 아직 다 안읽었고 && 누가 읽는 중이 아니라면(안 잠겨있으면)
        if (testObjs[i].bookMarkIdx != (testObjs[i].testCodeLength) && testObjs[i].lock == false) {
            testObjs[i].lock = true; // lock을 건다
            currentThread.currentTestObj = testObjs[i]; // 현재 검사 중인 test 코드 설정
            System.out.println(currentThread.tid + "번 스레드가 " + "File" + currentThread.currentTestObj.testId + ".txt를 검사하기 시작합니다...");
            break;
        }
    }

    // 스레드가 100글자 읽을 때까지 반복
    while (currentThread.countLetterNum <= currentThread.MAX_AVAILABLE_READ_NUM) {
        // 스레드가 읽은 글자 수가 100개면 스레드 수행 종료
        if (currentThread.countLetterNum == currentThread.MAX_AVAILABLE_READ_NUM) {
            // 종료를 위한 처리
        }

        // 파일을 다 읽었을 경우
        if (currentThread.currentTestObj.bookMarkIdx == currentThread.currentTestObj.testCodeLength) {
            // 아직 다 안 읽은 파일이 존재하는지 검사

            break; // 이 반복문을 나가고 만약에 아직 이 스레드가 100글자를 다 안읽었으면 세마포어를 다시 얻어서 읽을 수 있는 파일을 알아서 찾아서 들어갈거임
        }

        // 저번 검사에서 악성코드 일치한 게 끊겼을 때 그게 이어지는지 검사
        // 파일을 처음 검사하는 게 아니라서 북마크 인덱스가 0이 아님
        if (currentThread.currentTestObj.bookMarkIdx != 0 && currentThread.isNeedNextStartTest) {
            // 끊겼으면 nextTestStartIdx_spyware이 0이 아닌 다른 인덱스 값으로 설정되어있음
            if(currentThread.currentTestObj.nextTestStartIdx_spyware != 0) {
              // spyware 검사
            }
            // 끊겼으면 nextTestStartIdx_trojan이 0이 아닌 다른 인덱스 값으로 설정되어있음
            else if(currentThread.currentTestObj.nextTestStartIdx_trojan != 0) {
             // trojan 검사
                }
                // nextTestStartIdx_trojan 값이 0인지 아닌지로 끊긴 일치한 부분이 있는지를 판단하기 때문에 여기서 초기화를 해줘야 다음에도 판단이 가능
                currentThread.currentTestObj.nextTestStartIdx_trojan = 0;
            }
            currentThread.isNeedNextStartTest = false;
        }

        // 스레드가 읽은 글자 수가 (100-spyware 길이)인 부분까지는 통째로 검사
        if (currentThread.countLetterNum <= currentThread.MAX_AVAILABLE_READ_NUM - Spyware.codeLength) {
            // spyware 검사
        }

        // 스레드가 읽은 글자 수가 (100-trojan 길이)인 부분까지는 통째로 검사
        if (currentThread.countLetterNum <= currentThread.MAX_AVAILABLE_READ_NUM - Trojan.codeLength) {
            // trojan 검사
        }
        // 스레드가 읽다가 100글자가 채워져서 중간에 끊기는데 이 부분에서 악성코드가 발견되면 그 다음 스레드가 이어서 검사하면서 앞에 꺼랑 이어졌을 때 악성코드랑 일치하는 지 봐줘야 함
        // 끊기는 부분
        if (currentThread.countLetterNum > currentThread.MAX_AVAILABLE_READ_NUM - Spyware.codeLength) {
          // Spyware 검사
        }

        // 끊기는 부분
        if (currentThread.countLetterNum > currentThread.MAX_AVAILABLE_READ_NUM - Trojan.codeLength) {
            // trojan 검사
        }

        currentThread.countLetterNum++; // 스레드가 읽은 글자 수 += 1
        currentThread.currentTestObj.bookMarkIdx++; // 파일의 북마크 인덱스 += 1
    }
}

-----------

## 5)	Malware class
이 클래스는 악성코드의 정보를 담고 있고 파일에서 문자열을 읽어와서 String에 저장한다.
-	변수
String name = ""; // 악성코드 이름
String code = ""; // 악성코드 문자열
int codeLength; // 악성코드 길이
-	함수
public void readMalwareFile() {
    String filePath = "./inputText/Malware/" + name + ".txt";
    FileReader fileReader;
    try {
        fileReader = new FileReader(filePath);
        int c;
        while(((c = fileReader.read()) != -1)) {
            code += Character.toString((char)c);
        }
        System.out.println(code);
        fileReader.close();
    } catch (IOException e) {
        System.out.println("입출력 오류");
    }
}
텍스트 파일에서 악성코드를 읽어와 code 변수에 저장한다.

------

## 6)	TestObj class
이 클래스는 검사할 텍스트 파일의 정보를 담고 있고 문자열을 읽어와서 String에 저장한다.
-	변수
int testId; // test 코드 id
String testCode = ""; // testCode
int testCodeLength; // testCode 전체 길이
boolean lock = false; // 이 파일이 지금 검사하는 중인지(잠겨있는지) 판단하는 변수
int bookMarkIdx = 0; // 어디부터 읽으면 되는지 표시하는 인덱스
boolean isFindSpyware = false; // spyware 악성코드 찾았는지 판단해주는 변수
boolean isFindTrojan = false; // trojan 악성코드 찾았는지 판단해주는 변수
int nextTestStartIdx_spyware = 0; // 악성코드 일치하는 게 끊겼을 때 다음에 어디부터 검사하면 되는지 알려주는 인덱스
int nextTestStartIdx_trojan = 0; // 악성코드 일치하는 게 끊겼을 때 다음에 어디부터 검사하면 되는지 알려주는 인덱스
testId는 테스트 파일의 번호이다. testCode는 이 파일의 문자열 전체를 저장하는 변수이다. testCodeLength는 testCode의 전체 길이를 나타낸다. 어떤 스레드가 이 파일에 접근해서 파일을 읽으려고 하면 lock을 true로 하고 읽기 시작한다. 그리고 그 스레드가 그만 읽고 나갈 때 lock을 false로 해준다. 이 변수로 인해서 이 파일에 여러 스레드가 동시에 접근할 수 없게 된다. bookMarkIdx는 이 파일의 어디부터 읽으면 되는지 표시하는 변수이다. bookMarkIdx == testCodeLength가 되면 이 파일을 끝까지 다 읽은 것이다. isFindSpyware와 isFindTrojan은 이 파일에서 악성코드를 찾았으면 true로 해준다. 그래서 맨 마지막에 결과 출력할 때 이 변수가 true이면 악성파일로 분류한다. nextTestStartIdx_spyware와 nextTestStartIdx_trojan은 악성코드 일치하는 게 중간에 끊기고 다른 스레드가 그 부분부터 검사할 때 어디부터 악성코드의 어느 부분까지 일치했고 어디부터 검사해서 일치하면 찾은 것으로 판단할지 알려주는 변수이다. 
-	함수
public void readTestFile(){
    String filePath = "./inputText/Files/File" + testId + ".txt";
    FileReader fileReader;
    try {
        fileReader = new FileReader(filePath);
        int c;
        while((c = fileReader.read()) != -1) {
            testCode += Character.toString((char)c);
        }
        System.out.println(testCode);
        fileReader.close();
    } catch (IOException e) {
        System.out.println("입출력 오류");
    }
}
이 함수에서 테스트 파일의 문자열 전체를 읽어와서 testCode 변수에 저장한다.
3.	실행결과
파일을 불러오는 버튼과 검사를 시작하는 버튼이 있다. 먼저 파일을 불러오는 버튼을 눌러서 테스트 파일과 악성코드 파일들을 한꺼번에 불러온다.
 
테스트 파일들을 불러온다. 불러오면 어떤 파일들을 불러왔는지 출력해준다.
 
 
악성코드 파일들을 불러온다. 불러오면 어떤 파일들을 불러왔는지 출력해준다.
 
 
 
시작 버튼을 누르면 테스트 파일과 악성코드를 먼저 모두 출력해준다. 테스트 파일의 길이도 출력해주고 전체 테스트 파일들의 길이를 고려하여 스레드의 최소 개수를 출력해주고 스레드 개수를 입력 받는다. 
 
 
그리고 어떤 스레드가 S를 얻어 임계영역에 들어갔는지 출력해준다. 나갈 때는 세마포어를 종료한다고 출력해준다. 그리고 어떤 스레드가 어떤 파일을 읽는지 출력해준다. 그리고 100글자를 다 읽으면 다 읽었다고 출력해준다. 악성코드를 찾으면 어떤 스레드가 어떤 악성코드를 찾았는지 출력해준다. 어떤 스레드가 어떤 파일을 어디까지 읽고 나가려는 건지, 그 읽고 있던 파일의 전체 길이는 몇인지 출력해준다. 만약 아직 그 파일에 읽을 글자들이 더 남아있다면 아직 읽을 글자들이 남아있으니 다른 스레드가 또 와서 읽어줄 것이라고 출력해준다. 어떤 스레드가 검사를 하다가 악성코드의 일부분을 발견하고 본인은 100글자를 다 읽어서 나가야 한다면 어느 악성코드의 어느 부분을 찾았는지 출력해준다. 다른 스레드가 와서 처음에 검사했을 때 앞에서 발견한 악성코드의 일부분이 그대로 이어져서 악성코드 전체가 발견이 되었다면 발견되었다고 출력해준다. 파일을 다 읽었다면 어떤 파일을 다 읽었는지, 어떤 스레드가 마지막으로 읽었는지 출력해준다.
 
 

 
 
 
 
최종 결과인 정상 파일로 분류된 파일들과 그 개수, 악성 파일로 분류된 파일들과 그 개수, 그리고 어떤 악성 코드를 포함하고 있는지 출력해준다.
 
4.	소스코드
1)	Main class
package com.company;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.io.*;

// Main 클래스
public class Main {
    int threadNum = 0; // 스레드 총 개수
    Runnable[] runnables; // runnable 객체 배열
    Thread[] threads; // 스레드 배열
    ImportFiles importFiles = new ImportFiles(); // 파일 읽어오는 객체
    MalwareTestSystem malwareTestSystem; // 공유 객체 악성코드 검사 시스템 생성
    JButton startButton = importFiles.startButton; // 악성코드 검사를 시작하는 버튼
    public Main() {
        startButton.addActionListener(new StartActionListener());
    }

    public static void main(String[] args) {
        Main mainClass = new Main();
    }

    class StartActionListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            malwareTestSystem = new MalwareTestSystem(importFiles);
            Scanner sc = new Scanner(System.in);
            System.out.print("스레드 개수 입력 >> ");
            threadNum = sc.nextInt();

            // 스레드 객체 생성
            runnables = new MalwareTestThread[threadNum];
            threads = new Thread[threadNum];

            for(int i=0; i<threadNum; i++) {
                runnables[i] = new MalwareTestThread(i, malwareTestSystem);
                threads[i] = new Thread(runnables[i]);
                threads[i].start();
            }
            try {
                for(int i=0; i<threadNum; i++) {
                    threads[i].join();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                System.out.println("-----결과 출력-----");
                System.out.println("정상 파일로 분류된 파일들");
                int count = 0;
                for(int i=0; i<MalwareTestSystem.TEST_CODE_NUM; i++) {
                    if(malwareTestSystem.testObjs[i].isFindSpyware == false && malwareTestSystem.testObjs[i].isFindTrojan == false) {
                        System.out.println("File" + malwareTestSystem.testObjs[i].testId + ".txt");
                        count++;
                    }
                }
                System.out.println("총 " + count + "개");
                System.out.println("악성 파일로 분류된 파일들");
                count = 0;
                for(int i=0; i<MalwareTestSystem.TEST_CODE_NUM; i++) {
                    if(malwareTestSystem.testObjs[i].isFindSpyware == true || malwareTestSystem.testObjs[i].isFindTrojan == true) {
                        System.out.print("File" + malwareTestSystem.testObjs[i].testId + ".txt --> ");
                        if(malwareTestSystem.testObjs[i].isFindSpyware == true) {
                            System.out.print("Spyware ");
                        }
                        if(malwareTestSystem.testObjs[i].isFindTrojan == true) {
                            System.out.print("Trojan");
                        }
                        System.out.println();
                        count++;
                    }
                }
                System.out.println("총 " + count + "개");
            }
        }

    }
}
2)	ImportFiles class
package com.company;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

class ImportFiles extends JFrame {
    Container c = getContentPane();
    JButton startButton = new JButton("여기를 누르면 악성코드 검사 시작(파일을 먼저 import 한 후에 시작하세요)");
    String [] testFilePathList; // 테스트 파일 경로
    String [] testFileNameList; // 테스트 파일 이름
    String [] malwareFilePathList; // 악성 코드 파일 경로
    String [] malwareFileNameList; // 악성 코드 파일 이름
    public ImportFiles() {
        setTitle("File 가져오기");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        createMenu();
        setSize(500,500);
        setLocation(200, 200);
        setVisible(true);
    }
    private void createMenu() {
        JMenuBar mb = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem testFileOpenItem = new JMenuItem("Test File Open");
        JMenuItem malwareFileOpenItem = new JMenuItem("Malware File Open");
// testFile들 불러오기
        testFileOpenItem.addActionListener(new ActionListener() {
            private JFileChooser chooser = new JFileChooser();
            @Override
            public void actionPerformed(ActionEvent e) {
                chooser.setMultiSelectionEnabled(true);
                FileNameExtensionFilter filter = new FileNameExtensionFilter(".txt", "txt");
                chooser.setFileFilter(filter);
                int ret = chooser.showOpenDialog(null);
                if(ret != JFileChooser.APPROVE_OPTION) {
                    JOptionPane.showMessageDialog(null, "파일을 선택하지 않았습니다", "경고", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                File[] files = chooser.getSelectedFiles();
                testFilePathList = new String[files.length];
                testFileNameList = new String[files.length];
                for(int i=0; i<files.length; i++) {
                    testFilePathList[i] = files[i].getPath();
                    testFileNameList[i] = files[i].getName();
                    System.out.println(testFileNameList[i] + "불러옴");
                }
            }
        });
// 악성코드들 불러오기
        malwareFileOpenItem.addActionListener(new ActionListener() {
            private JFileChooser chooser = new JFileChooser();
            @Override
            public void actionPerformed(ActionEvent e) {
                chooser.setMultiSelectionEnabled(true);
                FileNameExtensionFilter filter = new FileNameExtensionFilter(".txt", "txt");
                chooser.setFileFilter(filter);
                int ret = chooser.showOpenDialog(null);
                if(ret != JFileChooser.APPROVE_OPTION) {
                    JOptionPane.showMessageDialog(null, "파일을 선택하지 않았습니다", "경고", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                File[] files = chooser.getSelectedFiles();
                malwareFilePathList = new String[files.length];
                malwareFileNameList = new String[files.length];
                for(int i=0; i<files.length; i++) {
                    malwareFilePathList[i] = files[i].getPath();
                    malwareFileNameList[i] = files[i].getName();
                    System.out.println(malwareFileNameList[i] + "불러옴");
                }
            }
        });
        fileMenu.add(testFileOpenItem);
        fileMenu.add(malwareFileOpenItem);
        mb.add(fileMenu);
        c.add(startButton);
        setJMenuBar(mb);
    }
}
 
3)	MalwareTestThread class
package com.company;

// 공유 자원에 접근하는 스레드 클래스
class MalwareTestThread implements Runnable{
    public static int MAX_AVAILABLE_READ_NUM = 100; // 스레드가 읽을 수 있는 최대 문자 개수
    int countLetterNum = 0; // 스레드가 읽은 글자 개수
    int tid; // 이 스레드 id(tid)
    MalwareTestSystem malwareTestSystem; // 스레드들이 공유하는 악성코드 검사 시스템 선언
    TestObj currentTestObj; // 현재 검사하고 있는 테스트 코드
    boolean isTerminated = false; // 스레드가 종료되었는지
    boolean isNeedNextStartTest; // 끊긴 일치한 부분 검사가 필요한지
    public MalwareTestThread(int tid, MalwareTestSystem malwareTestSystem) {
        this.tid = tid;
        this.malwareTestSystem = malwareTestSystem; // 스레드들이 공유하는 악성코드 검사 시스템 객체 할당
    }
    @Override
    public void run() {
        // 이 스레드가 종료되었다고 알려주는 isTerminated가 true이거나 스레드가 100글자를 다 읽을 때까지 계속 공유 객체로 들어가서 use 함수를 실행함
        while(isTerminated == false && countLetterNum < MAX_AVAILABLE_READ_NUM) {
            // 악성코드 검사 시스템의 use 함수 실행(파라미터로 이 스레드 객체를 넘겨줌)
            malwareTestSystem.use(this);
        }
    }
}

4)	MalwareTestSystem class(공유자원)
package com.company;

import java.util.Random;
import java.util.concurrent.Semaphore;

// 악성코드 탐지 시스템
class MalwareTestSystem {
    public static final int TEST_CODE_NUM = 9;
    private Semaphore semaphore = new Semaphore(TEST_CODE_NUM,true); // 세마포어 객체 생성 (Counting Semaphore)
    ImportFiles importFiles; // 파일을 불러오는 클래스
    Malware Spyware; // 악성코드 Spyware
    Malware Trojan; // 악성코드 Trojan
    TestObj [] testObjs = new TestObj [TEST_CODE_NUM]; // 검사할 파일 코드들


    public MalwareTestSystem(ImportFiles importFiles) {
        // 검사할 텍스트 파일들 불러오기
        this.importFiles = importFiles;
        // testId는 파일 번호랑 같게 함
        for(int i=0; i<TEST_CODE_NUM; i++) {
            testObjs[i] = new TestObj(Integer.parseInt(importFiles.testFileNameList[i].substring(4,5)), importFiles.testFilePathList[i]);
        }

        // 악성코드 읽어오기
        for(int i=0; i<2; i++) {
            if(importFiles.malwareFileNameList[i].equals("Spyware.txt")) {
                Spyware = new Malware(importFiles.malwareFileNameList[i], importFiles.malwareFilePathList[i]);
            } else if(importFiles.malwareFileNameList[i].equals("Trojan.txt")) {
                Trojan = new Malware(importFiles.malwareFileNameList[i], importFiles.malwareFilePathList[i]);
            }
        }

        // 스레드 몇 개 필요한지 구하기(예외 처리)
        int totalTxtLength = 0;
        for(int i=0; i<TEST_CODE_NUM; i++) {
            totalTxtLength += testObjs[i].testCodeLength;
        }
        if(totalTxtLength % 10 == 0) {
            System.out.println("\n스레드 " + (totalTxtLength/100) + "개 이상 필요");
        } else {
            System.out.println("\n스레드 " + (totalTxtLength/100 + 1) + "개 이상 필요");
        }
    }

    public void use(MalwareTestThread currentThread) {
        try {
            semaphore.acquire(); // 공유 자원 획득(S-1)
            System.out.println(currentThread.tid + "번 스레드 세마포어 시작");
            currentThread.isNeedNextStartTest = true; // 진입해서 끊긴 부분 검사
            searchMalwareCode(currentThread); // 스레드가 공유 자원을 획득할 때마다 이 함수를 호출함
            Thread.sleep(new Random().nextInt(100));
            semaphore.release(); // 공유 자원 방출(S+1)
            System.out.println(currentThread.tid + "번 스레드 세마포어 종료");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void searchMalwareCode(MalwareTestThread currentThread) {
        String subTestCodeSpyware;
        String subTestCodeTrojan;
        String subSpyware;
        String subTrojan;

        // 어떤 test 파일 코드가 읽을 수 있는 상태인지 탐색
        for (int i = 0; i < TEST_CODE_NUM; i++) {
            // 파일을 아직 다 안읽었고 && 누가 읽는 중이 아니라면(안 잠겨있으면)
            if (testObjs[i].bookMarkIdx != (testObjs[i].testCodeLength) && testObjs[i].lock == false) {
                testObjs[i].lock = true; // lock을 건다
                currentThread.currentTestObj = testObjs[i]; // 현재 검사 중인 test 코드 설정
                System.out.println(currentThread.tid + "번 스레드가 " + "File" + currentThread.currentTestObj.testId + ".txt를 검사하기 시작합니다...");
                break;
            }
        }

        // 스레드가 100글자 읽을 때까지 반복
        while (currentThread.countLetterNum <= currentThread.MAX_AVAILABLE_READ_NUM) {
            // countLetterNum이 0~99일 때 악성코드 검사를 수행하고 마지막에 countLetterNum == 100이면 수행 종료하는 부분만 실행하고 끝
            // 100번 검사 끝난 이후에 반복문이 한 번 더 도는 거라 bookMarkIdx는 다음 스레드가 검사 시작할 인덱스가 됨
            // 스레드가 읽은 글자 수가 100개면 스레드 수행 종료
            if (currentThread.countLetterNum == currentThread.MAX_AVAILABLE_READ_NUM) {
                //System.out.println(currentThread.tid + "의 현재 countLetterNum : " + currentThread.countLetterNum);
                System.out.println(currentThread.tid + "번 스레드가 " + "File" + currentThread.currentTestObj.testId + ".txt를 읽다가 100글자 한계를 마주했습니다.");
                System.out.println(currentThread.tid + "번 스레드가 읽고 있던 File" + currentThread.currentTestObj.testId+ ".txt의 bookMarkIdx : " + currentThread.currentTestObj.bookMarkIdx + ", " +"이 파일의 전체 텍스트 길이 : " + currentThread.currentTestObj.testCodeLength);
                // 아직 파일에 읽을 글자들이 남아있다면
                if (currentThread.currentTestObj.bookMarkIdx != currentThread.currentTestObj.testCodeLength) {
                    System.out.println("아직 " + "File" + currentThread.currentTestObj.testId + ".txt에는 읽을 글자들이 남아있으므로 다른 스레드가 읽어줄 것입니다.");
                    // 다른 스레드가 파일을 읽을 수 있도록 lock을 풀어줌
                    currentThread.currentTestObj.lock = false;
                }
                currentThread.isTerminated = true;
                break;
            }

            // 파일을 다 읽었을 경우
            if (currentThread.currentTestObj.bookMarkIdx == currentThread.currentTestObj.testCodeLength) {
                System.out.println("\n**********" + "File" + currentThread.currentTestObj.testId + ".txt를 다 읽었습니다. " + currentThread.tid + "번 스레드가 마지막으로 읽었습니다." + "**********\n");
                //System.out.println("File" + currentThread.currentTestObj.testId + ".txt의 길이 : " + currentThread.currentTestObj.testCodeLength + ", " + currentThread.currentTestObj.bookMarkIdx + "까지 해서 다 읽음");
                // 아직 다 안 읽은 파일이 존재하는지 검사
                boolean isExistFile = false;
                for (int i = 0; i < TEST_CODE_NUM; i++) {
                    if (testObjs[i].bookMarkIdx != (testObjs[i].testCodeLength)) {
                        isExistFile = true;
                    }
                }
                if(isExistFile == false) {
                    currentThread.isTerminated = true;
                }
                break; // 이 반복문을 나가고 만약에 아직 이 스레드가 100글자를 다 안읽었으면 세마포어를 다시 얻어서 읽을 수 있는 파일을 알아서 찾아서 들어갈거임
            }

            // 저번 검사에서 악성코드 일치한 게 끊겼을 때 그게 이어지는지 검사
            // 파일을 처음 검사하는 게 아니라서 북마크 인덱스가 0이 아님
            if (currentThread.currentTestObj.bookMarkIdx != 0 && currentThread.isNeedNextStartTest) {
                // 끊겼으면 nextTestStartIdx_spyware이 0이 아닌 다른 인덱스 값으로 설정되어있음
                if(currentThread.currentTestObj.nextTestStartIdx_spyware != 0) {
                    subSpyware = Spyware.code.substring(currentThread.currentTestObj.nextTestStartIdx_spyware);
                    subTestCodeSpyware = currentThread.currentTestObj.testCode.substring(currentThread.currentTestObj.bookMarkIdx, currentThread.currentTestObj.bookMarkIdx + subSpyware.length());
                    if (subTestCodeSpyware.equals(subSpyware)) {
                        System.out.println("\n----------" + currentThread.tid + "번 스레드가 " + "File" + currentThread.currentTestObj.testId + ".txt에서 Spyware를 찾는 추가적인 검사를 통해 " + subTestCodeSpyware + "를 발견했고 Spyware를 찾았습니다." + "----------\n");
                        currentThread.currentTestObj.isFindSpyware = true;
                    }else {
                        System.out.println("\n----------" + currentThread.tid + "번 스레드가 " + "File" + currentThread.currentTestObj.testId + ".txt에서 Spyware를 찾는 추가적인 검사를 진행하였지만 맞는 코드가 아니었습니다." + "----------\n");
                    }
                    // nextTestStartIdx_spyware 값이 0인지 아닌지로 끊긴 일치한 부분이 있는지를 판단하기 때문에 여기서 초기화를 해줘야 다음에도 판단이 가능
                    currentThread.currentTestObj.nextTestStartIdx_spyware = 0;
                }
                // 끊겼으면 nextTestStartIdx_trojan이 0이 아닌 다른 인덱스 값으로 설정되어있음
                else if(currentThread.currentTestObj.nextTestStartIdx_trojan != 0) {
                    subTrojan = Trojan.code.substring(currentThread.currentTestObj.nextTestStartIdx_trojan);
                    subTestCodeTrojan = currentThread.currentTestObj.testCode.substring(currentThread.currentTestObj.bookMarkIdx, currentThread.currentTestObj.bookMarkIdx + subTrojan.length());
                    if (subTestCodeTrojan.equals(subTrojan)) {
                        System.out.println("\n----------" + currentThread.tid + "번 스레드가 " + "File" + currentThread.currentTestObj.testId + ".txt에서 Trojan를 찾는 추가적인 검사를 통해 " + subTestCodeTrojan + " 를 발견했고 Trojan을 찾았습니다." + "----------\n");
                        currentThread.currentTestObj.isFindTrojan = true;
                    }else {
                        System.out.println("\n----------" + currentThread.tid + "번 스레드가 " + "File" + currentThread.currentTestObj.testId + ".txt에서 Trojan를 찾는 추가적인 검사를 진행하였지만 맞는 코드가 아니었습니다." + "----------\n");
                    }
                    // nextTestStartIdx_trojan 값이 0인지 아닌지로 끊긴 일치한 부분이 있는지를 판단하기 때문에 여기서 초기화를 해줘야 다음에도 판단이 가능
                    currentThread.currentTestObj.nextTestStartIdx_trojan = 0;
                }
                currentThread.isNeedNextStartTest = false;
            }

            // 스레드가 읽은 글자 수가 (100-spyware 길이)인 부분까지는 통째로 검사
            if (currentThread.countLetterNum <= currentThread.MAX_AVAILABLE_READ_NUM - Spyware.codeLength) {
                //System.out.println(currentThread.tid + "번 스레드의 countLetterNum : "+ currentThread.countLetterNum);
                //System.out.println(currentThread.tid + "번 스레드가 검사 중인 " + "File" + currentThread.currentTestObj.testId + ".txt의 bookMarkIdx : " + currentThread.currentTestObj.bookMarkIdx);
                // 파일의 끝부분이라 IndexOutOfBound 에러를 막기 위해서 substring 자르는 범위를 bookMarkIdx부터 끝까지로 설정
                if(currentThread.currentTestObj.bookMarkIdx + Spyware.codeLength - 1 >= currentThread.currentTestObj.testCodeLength) {
                    //System.out.println("파일의 끝부분");
                    subTestCodeSpyware = currentThread.currentTestObj.testCode.substring(currentThread.currentTestObj.bookMarkIdx);
                }
                // 파일 중간 부분이면 악성코드 길이만큼 자르면 됨
                else {
                    subTestCodeSpyware = currentThread.currentTestObj.testCode.substring(currentThread.currentTestObj.bookMarkIdx, currentThread.currentTestObj.bookMarkIdx + Spyware.codeLength);
                }
                //System.out.println(currentThread.tid + "번 스레드가 " + currentThread.currentTestObj.testId + ".txt를 통째로 자름 : " + subTestCodeSpyware);
                if (subTestCodeSpyware.equals(Spyware.code)) {
                    System.out.println("\n----------" + currentThread.tid + "번 스레드가 " + "File" + currentThread.currentTestObj.testId + ".txt에서 Spyware를 찾았습니다." + "----------\n");
                    currentThread.currentTestObj.isFindSpyware = true;
                }
            }

            // 스레드가 읽은 글자 수가 (100-trojan 길이)인 부분까지는 통째로 검사
            if (currentThread.countLetterNum <= currentThread.MAX_AVAILABLE_READ_NUM - Trojan.codeLength) {
                //System.out.println(currentThread.tid + "번 스레드의 countLetterNum : "+ currentThread.countLetterNum);
                //System.out.println(currentThread.tid + "번 스레드가 검사 중인 " + "File" + currentThread.currentTestObj.testId + ".txt의 bookMarkIdx : " + currentThread.currentTestObj.bookMarkIdx);

                // 파일의 끝부분이라 IndexOutOfBound 에러를 막기 위해서 substring 자르는 범위를 bookMarkIdx부터 끝까지로 설정
                if(currentThread.currentTestObj.bookMarkIdx + Trojan.codeLength - 1 >= currentThread.currentTestObj.testCodeLength) {
                    //System.out.println("파일의 끝부분");
                    subTestCodeTrojan = currentThread.currentTestObj.testCode.substring(currentThread.currentTestObj.bookMarkIdx);
                }
                // 파일 중간 부분이면 악성코드 길이만큼 자르면 됨
                else {
                    subTestCodeTrojan = currentThread.currentTestObj.testCode.substring(currentThread.currentTestObj.bookMarkIdx, currentThread.currentTestObj.bookMarkIdx + Trojan.codeLength);
                }
                //System.out.println(currentThread.tid + "번 스레드가 File" + currentThread.currentTestObj.testId + ".txt를 통째로 자름 : " + subTestCodeTrojan);
                if (subTestCodeTrojan.equals(Trojan.code)) {
                    System.out.println("\n----------" + currentThread.tid + "번 스레드가 " + "File" + currentThread.currentTestObj.testId + ".txt에서 Trojan를 찾았습니다." + "----------\n");
                    currentThread.currentTestObj.isFindTrojan = true;
                }
            }

            // 끊기는 부분 Spyware 검사
            if (currentThread.countLetterNum > currentThread.MAX_AVAILABLE_READ_NUM - Spyware.codeLength) {
                //System.out.println("끊기는 부분");
                //System.out.println(currentThread.tid + "번 스레드의 countLetterNum : "+ currentThread.countLetterNum);
                //System.out.println(currentThread.tid + "번 스레드가 검사 중인 " + "File" + currentThread.currentTestObj.testId + ".txt의 bookMarkIdx : " + currentThread.currentTestObj.bookMarkIdx);
                // 악성코드를 검사할 앞부분만 자름(스레드가 검사할 테스트 코드의 글자가 남은 개수만큼)
                subSpyware = Spyware.code.substring(0, currentThread.MAX_AVAILABLE_READ_NUM - currentThread.countLetterNum);
                // 파일의 끝부분이라 IndexOutOfBound 에러를 막기 위해서 substring 자르는 범위를 bookMarkIdx부터 끝까지로 설정
                if(currentThread.currentTestObj.bookMarkIdx + Spyware.codeLength - 1 >= currentThread.currentTestObj.testCodeLength) {
                    //System.out.println("파일의 끝부분");
                    subTestCodeSpyware = currentThread.currentTestObj.testCode.substring(currentThread.currentTestObj.bookMarkIdx);
                }
                else {
                    // 잘라낸 부분 악성코드 길이만큼 검사할 테스트 코드도 자름
                    subTestCodeSpyware = currentThread.currentTestObj.testCode.substring(currentThread.currentTestObj.bookMarkIdx, currentThread.currentTestObj.bookMarkIdx + subSpyware.length());
                }
                if (subTestCodeSpyware.equals(subSpyware)) {
                    currentThread.currentTestObj.nextTestStartIdx_spyware = subSpyware.length();
                    System.out.println("\n----------" + currentThread.tid + "번 스레드가 " + "File" + currentThread.currentTestObj.testId + ".txt에서 Spyware의 일부분인 " + subTestCodeSpyware + "를 찾았습니다. 추가적인 검사를 통해 판단할 수 있습니다." + "----------\n");
                }
            }

            // 끊기는 부분 Trojan 검사
            if (currentThread.countLetterNum > currentThread.MAX_AVAILABLE_READ_NUM - Trojan.codeLength) {
                //System.out.println("끊기는 부분");
                //System.out.println(currentThread.tid + "번 스레드의 countLetterNum : "+ currentThread.countLetterNum);
                //System.out.println(currentThread.tid + "번 스레드가 검사 중인 " + "File" + currentThread.currentTestObj.testId + ".txt의 bookMarkIdx : " + currentThread.currentTestObj.bookMarkIdx);
                // 악성코드를 검사할 앞부분만 자름(스레드가 검사할 테스트 코드의 글자가 남은 개수만큼)
                subTrojan = Trojan.code.substring(0, currentThread.MAX_AVAILABLE_READ_NUM - currentThread.countLetterNum);
                // 파일의 끝부분이라 IndexOutOfBound 에러를 막기 위해서 substring 자르는 범위를 bookMarkIdx부터 끝까지로 설정
                if(currentThread.currentTestObj.bookMarkIdx + Trojan.codeLength - 1 >= currentThread.currentTestObj.testCodeLength) {
                    //System.out.println("파일의 끝부분");
                    subTestCodeTrojan = currentThread.currentTestObj.testCode.substring(currentThread.currentTestObj.bookMarkIdx);
                }
                else {
                    // 잘라낸 부분 악성코드 길이만큼 검사할 테스트 코드도 자름
                    subTestCodeTrojan = currentThread.currentTestObj.testCode.substring(currentThread.currentTestObj.bookMarkIdx, currentThread.currentTestObj.bookMarkIdx + subTrojan.length());
                }
                if (subTestCodeTrojan.equals(subTrojan)) {
                    currentThread.currentTestObj.nextTestStartIdx_trojan = subTrojan.length();
                    System.out.println("\n----------" + currentThread.tid + "번 스레드가 " + "File" + currentThread.currentTestObj.testId + ".txt에서 Trojan의 일부분인 " + subTestCodeTrojan + " 를 찾았습니다. 추가적인 검사를 통해 판단할 수 있습니다." + "----------\n");
                }
            }

            currentThread.countLetterNum++; // 스레드가 읽은 글자 수 += 1
            currentThread.currentTestObj.bookMarkIdx++; // 파일의 북마크 인덱스 += 1
            //System.out.println(currentThread.tid + "번 스레드의 countLetterNum ++ -> "+ currentThread.countLetterNum);
            //System.out.println(currentThread.tid + "번 스레드가 검사 중인 " + "File" + currentThread.currentTestObj.testId + ".txt의 bookMarkIdx ++ -> " + currentThread.currentTestObj.bookMarkIdx);
        }
    }
}

5)	Malware class
package com.company;

import javax.swing.*;
import java.io.FileReader;
import java.io.IOException;

class Malware {
    String name = ""; // 악성코드 이름
    String filePath = ""; // 악성코드 파일 경로
    String code = ""; // 악성코드 문자열
    int codeLength; // 악성코드 길이

    public Malware(String name, String filePath) {
        this.name = name;
        this.filePath = filePath;
        readMalwareFile(); // 악성코드 파일 읽어서 문자열에 저장
        codeLength = code.length(); // 악성코드 길이 구하기
    }
    public void readMalwareFile() {
        FileReader fileReader;
        try {
            fileReader = new FileReader(filePath);
            int c;
            while(((c = fileReader.read()) != -1)) {
                code += Character.toString((char)c);
            }
            System.out.println("-----악성 코드 " + name + "-----");
            System.out.println(code);
            fileReader.close();
        } catch (IOException e) {
            System.out.println("입출력 오류");
        }
    }
}

6)	TestObj class
package com.company;

import java.io.FileReader;
import java.io.IOException;

// 악성코드 있는지 테스트할 코드 클래스
class TestObj {
    int testId; // test 코드 id
    String filePath; // 파일 경로
    String testCode = ""; // testCode
    int testCodeLength; // testCode 전체 길이
    boolean lock = false; // 이 파일이 지금 검사하는 중인지(잠겨있는지) 판단하는 변수
    int bookMarkIdx = 0; // 어디부터 읽으면 되는지 표시하는 인덱스
    boolean isFindSpyware = false; // spyware 악성코드 찾았는지 판단해주는 변수
    boolean isFindTrojan = false; // trojan 악성코드 찾았는지 판단해주는 변수
    int nextTestStartIdx_spyware = 0; // 악성코드 일치하는 게 끊겼을 때 다음에 어디부터 검사하면 되는지 알려주는 인덱스
    int nextTestStartIdx_trojan = 0; // 악성코드 일치하는 게 끊겼을 때 다음에 어디부터 검사하면 되는지 알려주는 인덱스

    public TestObj(int testId, String filePath) {
        this.testId = testId;
        this.filePath = filePath;
        readTestFile(); // test 파일 읽어서 문자열에 저장
        testCodeLength = testCode.length(); // test 코드 길이 구하기
        System.out.println("File" + testId + ".txt의 길이 : " + testCodeLength + "\n");
    }

    public void readTestFile(){
        FileReader fileReader;
        try {
            fileReader = new FileReader(filePath);
            int c;
            while((c = fileReader.read()) != -1) {
                testCode += Character.toString((char)c);
            }
            System.out.println("-----File" + testId + ".txt-----");
            System.out.println(testCode);
            fileReader.close();
        } catch (IOException e) {
            System.out.println("입출력 오류");
        }
    }
}
