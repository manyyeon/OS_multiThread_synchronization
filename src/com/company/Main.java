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
