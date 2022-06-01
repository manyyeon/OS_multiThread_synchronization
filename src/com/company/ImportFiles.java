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
