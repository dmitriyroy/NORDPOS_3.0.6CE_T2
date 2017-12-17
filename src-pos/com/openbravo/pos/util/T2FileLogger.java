package com.openbravo.pos.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dmitriyroy
 */
public class T2FileLogger {
    
    static String filePath;
    static SimpleDateFormat simpleDateFormat;

    public T2FileLogger() {
        filePath = new File("").getAbsolutePath()+"/T2_nordpos.log";
        simpleDateFormat = new SimpleDateFormat("yyy-MM-dd hh.MM.ss");
        if(!new File(filePath).exists()){
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(filePath);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(T2FileLogger.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        try {
            Files.write(Paths.get(filePath), (simpleDateFormat.format(new Date()) + " : ---------------------------------------------------------------------------------\n").getBytes(), StandardOpenOption.APPEND);
        } catch (IOException ex) {
            Logger.getLogger(T2FileLogger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void writeLog(String className, String str){  
        try {
            Files.write(Paths.get(filePath), (className + " : " + simpleDateFormat.format(new Date()) + " : " + str + "\n").getBytes(), StandardOpenOption.APPEND);
        }
        catch (IOException e) {
            System.out.println(e);
        }
    }
}
