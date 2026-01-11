package com.xiaowang.xwpicturebackend;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main {
    public static void main(String[] args) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(df.format(new Date()));
    }
}

