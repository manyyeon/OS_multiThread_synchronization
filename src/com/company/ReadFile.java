package com.company;

import java.io.File;
import java.io.*;
import java.util.*;

class ReadFile {
    Vector<Character> v;
    // 일단은 생성자에 구현해놨는데 나중에 바꿀거임
    public ReadFile() {
        v = new Vector<Character>();
        File file = new File("./inputText/Files/File1.txt");
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
                v.add((char)c);
                j++;
            }
            System.out.println();
            for(int i=0; i<v.size(); i++) {
                char x = v.get(i);
                System.out.print(x + " ");
            }
            fileReader.close();
        } catch (IOException e) {
            System.out.println("입출력 오류");
        }
    }
}
