package com.company;

import java.util.*;
import java.io.*;

// Main 클래스
public class Main {
    public static void main(String[] args) {
        int threadNum = 0; // 스레드 총 개수
        Runnable [] runnables; // runnable 객체 배열
        Thread [] threads; // 스레드 배열
        MalwareTestSystem malwareTestSystem = new MalwareTestSystem(); // 공유 객체 악성코드 검사 시스템 생성

        Scanner sc = new Scanner(System.in);
        System.out.print("스레드 개수 입력 >> ");
        threadNum = sc.nextInt();

        // 스레드 객체 생성
        runnables = new MalwareTestThread[threadNum];
        threads = new Thread[threadNum];

        for(int i=0; i<threadNum; i++) {
            runnables[i] = new MalwareTestThread(i, malwareTestSystem, threadNum);
            threads[i] = new Thread(runnables[i]);
            //threads[i].setName((i+1) + "번 스레드");
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
                        System.out.print("Spyware");
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
