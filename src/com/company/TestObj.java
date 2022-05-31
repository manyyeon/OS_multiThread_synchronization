package com.company;

import java.io.FileReader;
import java.io.IOException;

// 악성코드 있는지 테스트할 코드 클래스
class TestObj {
    int testId; // test 코드 id
    String testCode = ""; // testCode
    int testCodeLength; // testCode 전체 길이
    boolean lock = false; // 이 파일이 지금 검사하는 중인지(잠겨있는지) 판단하는 변수
    int bookMarkIdx = 0; // 어디부터 읽으면 되는지 표시하는 인덱스
    boolean isFindSpyware = false; // spyware 악성코드 찾았는지 판단해주는 변수
    boolean isFindTrojan = false; // trojan 악성코드 찾았는지 판단해주는 변수
    int nextTestStartIdx_spyware = 0; // 악성코드 일치하는 게 끊겼을 때 다음에 어디부터 검사하면 되는지 알려주는 인덱스
    int nextTestStartIdx_trojan = 0; // 악성코드 일치하는 게 끊겼을 때 다음에 어디부터 검사하면 되는지 알려주는 인덱스
    public TestObj(int testId) {
        this.testId = testId;
        readTestFile(); // test 파일 읽어서 문자열에 저장
        testCodeLength = testCode.length(); // test 코드 길이 구하기
        System.out.println("File" + testId + ".txt의 길이 : " + testCodeLength);
    }

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
}
