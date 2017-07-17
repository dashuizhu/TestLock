package com.zby.chest.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;
import android.util.Log;


public class FileUtil {
    private static final String PARENT_DIR = "Lock";
    private static final String CAMERA_DIR = "Camera";
    private static final String Record_DIR = "Record";
    private static final String Video_DIR  = "Video";
    private static final String LOG_DIR = "Log";
    private static final String CRASHLOG_DIR = "Crash";
    private static final String APK_DIR= "download";

    private static File getFile() {
//      final int currentUser = android.app.ActivityManager.getCurrentUser();
//      android.os.Environment.UserEnvironment userEnvironment = new android.os.Environment.UserEnvironment(currentUser);
//      File  file = userEnvironment.getExternalStorageDirectory();
        File file = Environment.getExternalStorageDirectory();
        return file;
    }

    /**
     * 文件是否存在
     * @param filePath
     * @return
     */
    public static boolean isFileExists(String filePath) {

        return false;
    }

    public static File getDownloadApkDirectory(String sdPath) {
        File subDir = new File(sdPath+"/" + PARENT_DIR+"/"+APK_DIR);
        if(!subDir.exists())
        {
            subDir.mkdirs();
        }
        return subDir;
    }
    
    public static File getCameraShutterDirectory() {
        //File sdDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File sdDir = getFile();
        File subDir = new File(sdDir, PARENT_DIR+"/"+CAMERA_DIR);
        if(!subDir.exists())
        {
            subDir.mkdirs();
        }
        return subDir;
    }

    public static File getSoundRecordDirectory() {
        //系统隐藏的api ， 如果是manifest.xml中设定了 shareUserId
        //使用下面这函数 会判断是shareUserId调用，就直接抛出异常，
        // ， 就可以使用 Environment， 重写了getExternalStorageDirectory 方法，但乜有userId判断
        File sdDir = getFile();
        File subDir = new File(sdDir, PARENT_DIR+"/"+Record_DIR);
        if(!subDir.exists())
        {
            subDir.mkdirs();
        }
        return subDir;
    }

    public static File getVideoRecordDirectory() {
        File sdDir =getFile();
        File subDir = new File(sdDir, PARENT_DIR+"/"+Video_DIR);
        if(!subDir.exists())
        {
            subDir.mkdirs();
        }
        return subDir;
    }

    public static File getLogDirectory() {
        File sdDir = getFile();
        File subDir = new File(sdDir, PARENT_DIR+"/"+LOG_DIR);
        if(!subDir.exists())
        {
            subDir.mkdirs();
        }
        return subDir;
    }

    public static File getCrashLogDirectory() {
        File sdDir = getFile();
        File subDir = new File(sdDir, PARENT_DIR+"/"+CRASHLOG_DIR);
        if(!subDir.exists())
        {
            subDir.mkdirs();
        }
        return subDir;
    }

    public static void DeleteFile(File file) { 
        if (file == null || file.exists() == false) {
            return; 
        } else {
            file.delete(); 
        } 
    }

    public static void DeleteFile(String filepath) { 
        if (filepath == null || filepath.equals("")) {
            return; 
        } else {
            File file = new File(filepath);
            DeleteFile(file);
        } 
    }
    
    public static void DeleteFolder(File file) { 
        if (file.exists() == false) {
            return; 
        } else { 
            if (file.isFile()) { 
                file.delete(); 
                return; 
            } 
            if (file.isDirectory()) { 
                File[] childFile = file.listFiles(); 
                if (childFile == null || childFile.length == 0) { 
                    file.delete(); 
                    return; 
                } 
                for (File f : childFile) { 
                    DeleteFolder(f); 
                } 
                file.delete(); 
            } 
        } 
    }


    /**
     * SD卡是否挂载可用
     * @return
     */
    public static boolean  isSdcardEnable() {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            return true;
        }
        return false;
    }

    /**
     * 获取外置SD卡路径
     * @return  应该就一条记录或空
     */
    public static List<String> getExtSDCardPath()
    {
        List<String> lResult = new ArrayList<String>();
        lResult.add( Environment.getExternalStorageDirectory().getPath());
        try {
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec("mount");
            InputStream is = proc.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("storage/sdcard1"))
                {
                    try {
                        String [] arr = line.split(" ");
                        String path = arr[1];
                        File file = new File(path);
                        if (file.isDirectory())
                        {
                            lResult.add(path);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }

                }
            }
            isr.close();
        } catch (Exception e) {
        }
        return lResult;
    }


    /**
     * 根据路径获取内存可用空间
     * 字节b,如果未挂载返回0
     * @param path
     * @return
     */
    public static long getMemoryInfo(Context mContext , File path) {
        // 获得一个磁盘状态对象
        StatFs stat = new StatFs(path.getPath());

        long blockSize = stat.getBlockSize(); // 获得一个扇区的大小

        long totalBlocks = stat.getBlockCount(); // 获得扇区的总数

        long availableBlocks = stat.getAvailableBlocks(); // 获得可用的扇区数量

        // 总空间
        String totalMemory = Formatter.formatFileSize(mContext, totalBlocks * blockSize);
        // 可用空间
        String availableMemory = Formatter.formatFileSize(mContext, availableBlocks * blockSize);

        //System.out.println("总空间: " + totalMemory + "\n可用空间: " + availableMemory);
        long  size = (long) (availableBlocks * blockSize );
        Log.d("BabyService_fileUtils", "~~~ path:"+path.getAbsolutePath()+ " size:"+size);
        return size;
    }


    /**
     * 获得可用的sd路径
     * @param mContext
     * @param needSize   需要的空间 单位b
     * @return  返回可用的 拥有空间的sd卡路径，   sd卡不可用或没空间  返回null
     */
    public static String getAvaibleSDPath (Context mContext ,long  needSize) {
        List<String> list = getExtSDCardPath();
        String path ="";
        Log.d("test", "~~~" + list.size());
        for(int i=0;i<list.size();i++) {
            path = list.get(i);
            File f = new File(path);
            if(needSize<getMemoryInfo(mContext, f)) {
                return path;
            }
        }
        return null;
    }
}
