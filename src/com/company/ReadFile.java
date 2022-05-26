package com.company;

import java.io.File;
import java.io.*;
import java.util.*;

class ReadFile {
    File spywareFile = new File("./inputText/Malware/Spyware.txt");
    File trojanFile = new File("./inputText/Malware/Trojan.txt");
    Vector<Character> textCode;
    Vector<Character> spywareCode;
    Vector<Character> trojanCode;
    // 일단은 생성자에 구현해놨는데 나중에 바꿀거임
    public ReadFile() {
        spywareCode = new Vector<>();
        trojanCode = new Vector<>();
        readMalwareFile(spywareFile, spywareCode);
        readMalwareFile(trojanFile, trojanCode);
        textCode = new Vector<Character>();
        File file = new File("./inputText/Files/File8.txt");
        FileReader fileReader;
        try {
            fileReader = new FileReader(file);
            int c = 0;
            int j = 0;
            while(((c = fileReader.read()) != -1)) {
                if(j == 100) {
                    break;
                }
                System.out.print((char)c);
                textCode.add((char)c);
                j++;
            }
            System.out.println();
            for(int i = 0; i< textCode.size(); i++) {
                char x = textCode.get(i);
            }
            fileReader.close();
        } catch (IOException e) {
            System.out.println("입출력 오류");
        }
        searchMalwareCode(0, textCode, trojanCode);
    }
    public void readMalwareFile(File file, Vector<Character> v) {
        FileReader fileReader;
        try {
            fileReader = new FileReader(file);
            int c = 0;
            while(((c = fileReader.read()) != -1)) {
                System.out.print((char)c);
                v.add((char)c);
            }
            System.out.println();
            for(int i=0; i<v.size(); i++) {
                char x = v.get(i);
            }
            System.out.println();
            fileReader.close();
        } catch (IOException e) {
            System.out.println("입출력 오류");
        }
    }
    public void searchMalwareCode(int initialIdx, Vector<Character> textCode, Vector<Character> malwareCode) {
        Vector<Character> resultCode = new Vector<>();
        boolean find = true;
        for(int i=initialIdx; i<textCode.size(); i++){
            find = true;
            for(int j=0; j<malwareCode.size(); j++){
                if(i+j >= textCode.size()) {
                    break;
                }
                if(textCode.get(i+j) != malwareCode.get(j)) {
                    find = false;
                    resultCode.clear();
                    break;
                }
                resultCode.add(textCode.get(i+j));
                if(resultCode.size() == malwareCode.size()){
                    find = true;
                    break;
                }
            }
            if(resultCode.size() == malwareCode.size()){
                find = true;
                break;
            }
        }
        System.out.println(find);
        for(int i=0; i<resultCode.size(); i++) {
            char x = resultCode.get(i);
            System.out.print(x + " ");
        }
    }
}
