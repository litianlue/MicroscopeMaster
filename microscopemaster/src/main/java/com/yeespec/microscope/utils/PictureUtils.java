package com.yeespec.microscope.utils;import android.content.Context;import android.content.SharedPreferences;import android.graphics.Bitmap;import android.graphics.BitmapFactory;import android.graphics.Color;import android.graphics.Matrix;import android.os.Environment;import android.util.Log;import com.yeespec.libuvccamera.uvccamera.encoder.MediaMuxerWrapper;import com.yeespec.microscope.master.activity.PhotoAlbumActivity;import com.yeespec.microscope.master.application.BaseApplication;import com.yeespec.microscope.utils.bluetooth.BlueUtil;import com.yeespec.microscope.utils.bluetooth.DataUtil;import java.io.File;import java.io.FileNotFoundException;import java.io.FileOutputStream;import java.io.IOException;import java.lang.ref.WeakReference;import java.text.SimpleDateFormat;import java.util.ArrayList;import java.util.Arrays;import java.util.Collections;import java.util.Comparator;import java.util.GregorianCalendar;import java.util.HashSet;import java.util.List;import java.util.Locale;import java.util.Set;import java.util.concurrent.ExecutorService;import java.util.concurrent.Executors;/** * Created by Administrator on 2016/3/6. */public class PictureUtils {    private ExecutorService pool = Executors.newFixedThreadPool(1);//创建可重用固定线程池    private ArrayList<File> allFile2 = new ArrayList<>();    private final SimpleDateFormat mDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US);    public String getDateTimeString() {        GregorianCalendar now = new GregorianCalendar();        return mDateTimeFormat.format(now.getTime());    }    public int decountPercentage(Bitmap graymap, int temp) {        int percentage = 0;        float decount = 0;        float allDp = 0;        //得到图形的宽度和长度        int width = graymap.getWidth();        int height = graymap.getHeight();        allDp = width * height;        //创建二值化图像//        Bitmap binarymap = null;//        binarymap = graymap.copy(Bitmap.Config.ARGB_8888, true);        //依次循环，对图像的像素进行处理        for (int i = 0; i < width; i++) {            for (int j = 0; j < height; j++) {                //得到当前像素的值                int col = graymap.getPixel(i, j);                //得到alpha通道的值              //  int alpha = col & 0xFF000000;                //得到图像的像素RGB的值                int red = (col & 0x00FF0000) >> 16;                int green = (col & 0x0000FF00) >> 8;                int blue = (col & 0x000000FF);                // 用公式X = 0.3×R+0.59×G+0.11×B计算出X代替原来的RGB                int gray = (int) ((float) red * 0.3 + (float) green * 0.59 + (float) blue * 0.11);                //对图像进行二值化处理                if (gray <= temp) {                    decount++;                    gray = 0;                } else {                    gray = 255;                }                // 新的ARGB                //  int newColor = alpha | (gray << 16) | (gray << 8) | gray;                //设置新图像的当前像素值                //  binarymap.setPixel(i, j, newColor);            }        }        percentage = (int) (decount / allDp * 100);        // Log.i("UVCService","decount="+decount);        //  Log.i("UVCService","allDp="+allDp);        return percentage;    }    public String numberString(int number, int saturation) {        int filenumber = number + 1;        if (saturation > 1) {            if (filenumber < 10) {                return "10000" + filenumber;            } else if (filenumber < 100) {                return "1000" + filenumber;            } else if (filenumber < 1000) {                return "100" + filenumber;            } else {                return "10" + filenumber;            }        } else {            if (filenumber < 10) {                return "00000" + filenumber;            } else if (filenumber < 100) {                return "0000" + filenumber;            } else if (filenumber < 1000) {                return "000" + filenumber;            } else {                return "00" + filenumber;            }        }    }    private void contrastCompoudMethod(final Bitmap[] rbitmaps, final int size, final int[] isTranlates) {        pool.execute(new Runnable() {            @Override            public void run() {                int width = rbitmaps[0].getWidth();                int height = rbitmaps[0].getHeight();                int A = 0, R = 0, G = 0, B = 0;                int pixelColor;                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);//重新建一个bitmap，存新生成的数据                WeakReference<Bitmap> rbitmap = new WeakReference<Bitmap>(bitmap);                for (int y = 0; y < height; y++) {//循环每个像素点                    for (int x = 0; x < width; x++) {                        for (int j = 0; j < size; j++) {//循环每个图片                            if (isTranlates[j] == 1) {//带荧光图片                                int y1;                                int x1;                                //y坐标校准                                if ((y - ConstantUtil.tranlateY) < 0) {                                    y1 = 0;                                } else if ((y - ConstantUtil.tranlateY) > (height - 1)) {                                    y1 = height - 1;                                } else {                                    y1 = y - ConstantUtil.tranlateY;                                }                                //x坐标校准                                if ((x - ConstantUtil.tranlateX) < 0) {                                    x1 = 0;                                } else if ((x - ConstantUtil.tranlateX) > (width - 1)) {                                    x1 = width - 1;                                } else {                                    x1 = x - ConstantUtil.tranlateX;                                }                                pixelColor = rbitmaps[j].getPixel(x1, y1);                            } else {                                pixelColor = rbitmaps[j].getPixel(x, y);                            }                            // pixelColor = rbitmaps.get()[j].getPixel(x, y);                            A += Color.alpha(pixelColor);//把每个像素点加起来                            R += Color.red(pixelColor);                            G += Color.green(pixelColor);                            B += Color.blue(pixelColor);                        }                        A = A < 255 ? A : 255;                        R = R < 255 ? R : 255;                        G = G < 255 ? G : 255;                        B = B < 255 ? B : 255;                        int c = Color.argb(A, R, G, B);                        //  bitmap.setPixel(x, y, c);                        rbitmap.get().setPixel(x, y, c);                        A = 0;                        R = 0;                        G = 0;                        B = 0;                    }                }                for (int indexNum = 0; indexNum < rbitmaps.length; indexNum++) {//循环                    if (rbitmaps[indexNum]!=null) {                        rbitmaps[indexNum].recycle();                    }                }                String timerstr = getDateTimeString();                timerstr = numberString(ConstantUtil.filenumber, 2) + "-" + timerstr;                FileUtils.capture(shearBitmapXY(rbitmap.get(), ConstantUtil.tranlateX, ConstantUtil.tranlateY), MediaMuxerWrapper.getCaptureFile(Environment.DIRECTORY_DCIM, timerstr + ".bmp", "big").toString(), false, false);                FileUtils.capture(shearBitmapXY(rbitmap.get(), ConstantUtil.tranlateX, ConstantUtil.tranlateY), MediaMuxerWrapper.getCaptureFile(Environment.DIRECTORY_DCIM, timerstr + ".scaled.bmp", "scale").toString(), true, false);                FileUtils.capture(shearBitmapXY(rbitmap.get(), ConstantUtil.tranlateX, ConstantUtil.tranlateY), MediaMuxerWrapper.getCaptureFile(Environment.DIRECTORY_DCIM, timerstr + ".scaled.jpg", "request").toString(), false, true);                if (rbitmap.get() != null) {                    rbitmap.get().recycle();                }                if (bitmap!= null) {                    bitmap.recycle();                }                System.gc();            }        });    }    private void compoudMethod(int size) {        Bitmap[] bitmaps = new Bitmap[size];        int isTranlates[] = new int[size];        WeakReference<Bitmap[]> rbitmaps = new WeakReference<Bitmap[]>(bitmaps);        int index = 0;        for (index = 0; index < size; index++) {            // int i = lstSelected.get(index);            int cout = allFile2.size() - 1 - index;            if (allFile2.get(cout).getName().contains(".mp4")) {                return;            }            //判断是否是荧光图片            if (allFile2.get(cout).getName().toString().substring(0, 1).equals("1")) {                isTranlates[index] = 1;            }            //复制图片数据到bitmaps            //bitmaps[index] = BitmapFactory.decodeFile(allFile2.get(cout).getPath());            rbitmaps.get()[index] = BitmapFactory.decodeFile(allFile2.get(cout).getPath());        }        int width = rbitmaps.get()[0].getWidth();        int height = rbitmaps.get()[0].getHeight();        int A = 0, R = 0, G = 0, B = 0;        int pixelColor;        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);//重新建一个bitmap，存新生成的数据        WeakReference<Bitmap> rbitmap = new WeakReference<Bitmap>(bitmap);        for (int y = 0; y < height; y++) {//循环每个像素点            for (int x = 0; x < width; x++) {                for (int j = 0; j < size; j++) {//循环每个图片                    if (isTranlates[j] == 1) {//带荧光图片                        int y1;                        int x1;                        //y坐标校准                        if ((y - ConstantUtil.tranlateY) < 0) {                            y1 = 0;                        } else if ((y - ConstantUtil.tranlateY) > (height - 1)) {                            y1 = height - 1;                        } else {                            y1 = y - ConstantUtil.tranlateY;                        }                        //x坐标校准                        if ((x - ConstantUtil.tranlateX) < 0) {                            x1 = 0;                        } else if ((x - ConstantUtil.tranlateX) > (width - 1)) {                            x1 = width - 1;                        } else {                            x1 = x - ConstantUtil.tranlateX;                        }                        pixelColor = rbitmaps.get()[j].getPixel(x1, y1);                    } else {                        pixelColor = rbitmaps.get()[j].getPixel(x, y);                    }                    // pixelColor = rbitmaps.get()[j].getPixel(x, y);                    A += Color.alpha(pixelColor);//把每个像素点加起来                    R += Color.red(pixelColor);                    G += Color.green(pixelColor);                    B += Color.blue(pixelColor);                }                A = A < 255 ? A : 255;                R = R < 255 ? R : 255;                G = G < 255 ? G : 255;                B = B < 255 ? B : 255;                int c = Color.argb(A, R, G, B);                //  bitmap.setPixel(x, y, c);                rbitmap.get().setPixel(x, y, c);                A = 0;                R = 0;                G = 0;                B = 0;            }        }        for (int indexNum = 0; indexNum < rbitmaps.get().length; indexNum++) {//循环每个像素点            if (rbitmaps.get()[indexNum]!= null) {                rbitmaps.get()[indexNum].recycle();            }        }        String timerstr = getDateTimeString();        timerstr = numberString(ConstantUtil.filenumber, 2) + "-" + timerstr;        FileUtils.capture(shearBitmapXY(rbitmap.get(), ConstantUtil.tranlateX, ConstantUtil.tranlateY), MediaMuxerWrapper.getCaptureFile(Environment.DIRECTORY_DCIM, timerstr + ".bmp", "big").toString(), false, false);        FileUtils.capture(shearBitmapXY(rbitmap.get(), ConstantUtil.tranlateX, ConstantUtil.tranlateY), MediaMuxerWrapper.getCaptureFile(Environment.DIRECTORY_DCIM, timerstr + ".scaled.bmp", "scale").toString(), true, false);        FileUtils.capture(shearBitmapXY(rbitmap.get(), ConstantUtil.tranlateX, ConstantUtil.tranlateY), MediaMuxerWrapper.getCaptureFile(Environment.DIRECTORY_DCIM, timerstr + ".scaled.jpg", "request").toString(), false, true);        for (int i = 0; i < bitmaps.length; i++) {            if (bitmaps[i]!=null) {                bitmaps[i].recycle();            }        }        if (bitmap!=null) {            bitmap.recycle();        }        if (rbitmap.get() != null) {            rbitmap.get().recycle();        }        System.gc();    }   /* public  void capture(Bitmap bitmap, String path, boolean scaled,boolean isjpg) {        if (scaled) {            Matrix matrix = new Matrix();            matrix.postScale(0.1f, 0.1f);   //图像长宽缩小为0.2倍 136*104 ;            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);        }        //20170614注释        FileOutputStream fos = null;        try {            fos = new FileOutputStream(path);            if(isjpg) {                if (bitmap.compress(Bitmap.CompressFormat.JPEG, 30, fos)) {                    fos.flush();                }            }else if(scaled){                if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)) {                    fos.flush();                }            } else {                int w = bitmap.getWidth(), h = bitmap.getHeight();                int[] pixels = new int[w * h];                bitmap.getPixels(pixels, 0, w, 0, 0, w, h);                byte[] rgb = addBMP_RGB_888(pixels, w, h);                byte[] header = addBMPImageHeader(rgb.length);                byte[] infos = addBMPImageInfosHeader(w, h);                byte[] buffer = new byte[54 + rgb.length];                System.arraycopy(header, 0, buffer, 0, header.length);                System.arraycopy(infos, 0, buffer, 14, infos.length);                System.arraycopy(rgb, 0, buffer, 54, rgb.length);                fos.write(buffer);            }            if(bitmap!=null)                bitmap.recycle();            fos.close();            System.gc();        } catch (FileNotFoundException e) {            e.printStackTrace();        } catch (IOException e) {            e.printStackTrace();        } finally {            try {               *//* //关闭资源输入输出流 :                if (fos != null) {                    fos.close();                }                if(scaled&&(!bitmap.isRecycled())){                    bitmap.recycle();                }*//*                System.gc();            } catch (Exception ex) {                ex.printStackTrace();            } finally {            }        }    }    private  byte[] addBMPImageHeader(int size) {        byte[] buffer = new byte[14];        buffer[0] = 0x42;        buffer[1] = 0x4D;        buffer[2] = (byte) (size >> 0);        buffer[3] = (byte) (size >> 8);        buffer[4] = (byte) (size >> 16);        buffer[5] = (byte) (size >> 24);        buffer[6] = 0x00;        buffer[7] = 0x00;        buffer[8] = 0x00;        buffer[9] = 0x00;        buffer[10] = 0x36;        buffer[11] = 0x00;        buffer[12] = 0x00;        buffer[13] = 0x00;        return buffer;    }    private  byte[] addBMPImageInfosHeader(int w, int h) {        byte[] buffer = new byte[40];        buffer[0] = 0x28;        buffer[1] = 0x00;        buffer[2] = 0x00;        buffer[3] = 0x00;        buffer[4] = (byte) (w >> 0);        buffer[5] = (byte) (w >> 8);        buffer[6] = (byte) (w >> 16);        buffer[7] = (byte) (w >> 24);        buffer[8] = (byte) (h >> 0);        buffer[9] = (byte) (h >> 8);        buffer[10] = (byte) (h >> 16);        buffer[11] = (byte) (h >> 24);        buffer[12] = 0x01;        buffer[13] = 0x00;  //12-13图层数        buffer[14] = 0x18;        buffer[15] = 0x00;  //bitdepth        buffer[16] = 0x00;        buffer[17] = 0x00;        buffer[18] = 0x00;        buffer[19] = 0x00;        buffer[20] = 0x00;        buffer[21] = 0x00;        buffer[22] = 0x00;        buffer[23] = 0x00;        //分辨率//        buffer[24] = (byte) 0xE0;//        buffer[25] = 0x01;//        buffer[26] = 0x00;//        buffer[27] = 0x00;//        buffer[28] = 0x02;//        buffer[29] = 0x03;//        buffer[30] = 0x00;//        buffer[31] = 0x00;        buffer[24] = (byte) (h >> 0);        buffer[25] = (byte) (h >> 8);        buffer[26] = (byte) (h >> 16);        buffer[27] = (byte) (h >> 24);        buffer[28] = (byte) (h >> 0);        buffer[29] = (byte) (h >> 8);        buffer[30] = (byte) (h >> 16);        buffer[31] = (byte) (h >> 24);        //============================        buffer[32] = 0x00;        buffer[33] = 0x00;        buffer[34] = 0x00;        buffer[35] = 0x00;        buffer[36] = 0x00;        buffer[37] = 0x00;        buffer[38] = 0x00;        buffer[39] = 0x00;        return buffer;    }    private  byte[] addBMP_RGB_888(int[] b, int w, int h) {        int len = b.length;        System.out.println(b.length);        byte[] buffer = new byte[w * h * 3];        int offset = 0;        for (int i = len - 1; i >= w; i -= w) {            int end = i, start = i - w + 1;            for (int j = start; j <= end; j++) {                buffer[offset] = (byte) (b[j] >> 0);                buffer[offset + 1] = (byte) (b[j] >> 8);                buffer[offset + 2] = (byte) (b[j] >> 16);                offset += 3;            }        }        return buffer;    }*/    //public static int[] ligths;    //自动拍摄图片的合成    public void pictureAutoCompound(Context context) {        int contractslength = 0;        int[][] contracts = DataUtil.getContracts();//获取对照组        if (contracts != null)            contractslength = contracts.length;//得到对照组的大小        Set<String> strings = new HashSet<>();        SharedPreferences preferences = context.getSharedPreferences(ConstantUtil.CHECKBOXSTATE, 0);        strings = preferences.getStringSet(ConstantUtil.CHECKBOXS, null);        if (strings == null) {            return;        }        int size = strings.size();        strings = null;        List<File> pngFiles = PictureUtils.getPictures(context);        allFile2.clear();        allFile2.addAll(pngFiles);        //Log.w("PictureUtils","DataUtil.isStartContract"+DataUtil.isStartContract);        //对照组合成        if (DataUtil.getContracts() != null) {            //  Log.w("PictureUtils","DataUtil.isStartContract="+DataUtil.isStartContract);            if (DataUtil.CONTRASTPLAN == 2) {                for (int i = 0; i < contractslength; i++) {                    Bitmap[] bitmaps = new Bitmap[size];                    int isTranlates[] = new int[size];                    for (int j = 0; j < size; j++) {                        int cout = allFile2.size() - j * contractslength - i - 1;                        if (allFile2.get(allFile2.size() - cout - 1).getName().contains(".mp4")) {                            return;                        }                        //判断是否是荧光图片                        if (allFile2.get(allFile2.size() - cout - 1).getName().toString().substring(0, 1).equals("1")) {                            isTranlates[j] = 1;                        }                        bitmaps[j] = BitmapFactory.decodeFile(allFile2.get(allFile2.size() - cout - 1).getPath());                    }                    contrastCompoudMethod(bitmaps, size, isTranlates);                }            } else {                compoudMethod(size);            }        } else {            Log.w("PictureUtils", "compoudMethod");            compoudMethod(size);        }        allFile2.clear();    }    public static List<File> getPictures(Context context) {        String currentUserName = ConstantUtil.getCurrentUserName(context);        List<File> list = new ArrayList<>();        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), currentUserName + "/big");        File[] allFiles = file.listFiles();        if (allFiles == null) {            return list;        }        for (int k = 0; k < allFiles.length; k++) {            final File fi = allFiles[k];            if (fi.isFile()) {               /* int idx = fi.getPath().lastIndexOf(".");                if (idx <= 0) {                    continue;                }                if (fi.getPath().contains("scaled")) {                    continue;                }                String suffix = fi.getPath().substring(idx);                if (suffix.toLowerCase().equals(".jpg") ||                        suffix.toLowerCase().equals(".jpeg") ||                        suffix.toLowerCase().equals(".bmp") ||                        suffix.toLowerCase().equals(".png") ||                        suffix.toLowerCase().equals(".gif")) {                    list.add(fi);                }*/                list.add(fi);            }        }        return list;    }    public static List<File> getPicturesScaled(Context context) {        String currentUserName = ConstantUtil.getCurrentUserName(context);        List<File> list = new ArrayList<>();        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), currentUserName + "/scale");        File[] allFiles = file.listFiles();        if (allFiles == null) {            return list;        }        for (int k = 0; k < allFiles.length; k++) {            final File fi = allFiles[k];            if (fi.isFile()) {                /*int idx = fi.getPath().lastIndexOf(".");                if (idx <= 0) {                    continue;                }                if (!fi.getPath().contains(".scaled")||fi.getPath().contains(".jpg")) {                    continue;                }*/                //20170815                list.add(fi);               /* String suffix = fi.getPath().substring(idx);                if (suffix.toLowerCase().equals(".jpg") ||                        suffix.toLowerCase().equals(".jpeg") ||                        suffix.toLowerCase().equals(".bmp") ||                        suffix.toLowerCase().equals(".png") ||                        suffix.toLowerCase().equals(".gif")) {                    list.add(fi);                }*/            }        }        return list;    }    /**     * @param bmp     * @param shearX     * @param shearY     * @return     */    public  Bitmap shearBitmapXY(Bitmap bmp, int shearX, int shearY) {        int width = bmp.getWidth();        int height = bmp.getHeight();        int startx = 0, picturew = 0, starty = 0, pictureh;        if (shearX > 0) {            startx = shearX;            picturew = shearX;        } else {            startx = 0;            picturew = -shearX;        }        if (shearY > 0) {            starty = shearY;            pictureh = shearY;        } else {            starty = 0;            pictureh = -shearY;        }        Bitmap bitmap = Bitmap.createBitmap(bmp, 0 + startx, 0 + starty, width - picturew, height - pictureh);        return bitmap;    }    public static List<File> getJpgPicturesScaled(Context context) {        String currentUserName = ConstantUtil.getCurrentUserName(context);        List<File> list = new ArrayList<>();        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), currentUserName + "/request");        File[] allFiles = file.listFiles();        if (allFiles == null) {            return list;        }        for (int k = 0; k < allFiles.length; k++) {            final File fi = allFiles[k];            if (fi.isFile()) {               /* int idx = fi.getPath().lastIndexOf(".");                if (idx <= 0) {                    continue;                }                if (!fi.getPath().contains(".jpg")) {                    continue;                }                String suffix = fi.getPath().substring(idx);                if (suffix.toLowerCase().equals(".jpg") ||                        suffix.toLowerCase().equals(".jpeg") ||                        suffix.toLowerCase().equals(".bmp") ||                        suffix.toLowerCase().equals(".png") ||                        suffix.toLowerCase().equals(".gif")) {                    list.add(fi);                }*/                list.add(fi);            }        }        return list;    }   /* public static List<File> getPicturesScaled(Context context,int page) {        int pagenum=15;        int allnum=0;        String currentUserName = ConstantUtil.getCurrentUserName(context);        List<File> list = new ArrayList<>();        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),currentUserName);        File[] allFiles = file.listFiles();        Collections.sort(Arrays.asList(allFiles), new Comparator<File>() {            @Override            public int compare(File lhs, File rhs) {                if (lhs.lastModified() > rhs.lastModified()) {                    return -1;                } else {                    return 1;                }            }        });        if (allFiles == null) {            return list;        }        if(allFiles.length<page*pagenum){           allnum = allFiles.length;        }else            allnum =page*pagenum;        for (int k = 0; k < allnum; k++) {            final File fi = allFiles[k];            if (fi.isFile()) {                int idx = fi.getPath().lastIndexOf(".");                if (idx <= 0) {                    continue;                }                if (!fi.getPath().contains("scaled")) {                    continue;                }                String suffix = fi.getPath().substring(idx);                if (suffix.toLowerCase().equals(".jpg") ||                        suffix.toLowerCase().equals(".jpeg") ||                        suffix.toLowerCase().equals(".bmp") ||                        suffix.toLowerCase().equals(".png") ||                        suffix.toLowerCase().equals(".gif")) {                    list.add(fi);                }            }        }        return list;    }    */    public static List<File> getMovies(Context context) {        String currentUserName = ConstantUtil.getCurrentUserName(context);        List<File> list = new ArrayList<>();        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), currentUserName);        File[] allfiles = file.listFiles();        if (allfiles == null) {            return list;        }        for (int k = 0; k < allfiles.length; k++) {            final File fi = allfiles[k];            if (fi.isFile()) {                int idx = fi.getPath().lastIndexOf(".");                if (idx <= 0) {                    continue;                }                String suffix = fi.getPath().substring(idx);                if (suffix.toLowerCase().equals(".mp4")) {                    list.add(fi);                }            }        }        return list;    }   /* public static List<File> getMovies(Context context,int page) {        int pagenum=15;        int allnum=0;        String currentUserName = ConstantUtil.getCurrentUserName(context);        List<File> list = new ArrayList<>();        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), currentUserName);        File[] allfiles = file.listFiles();        Collections.sort(Arrays.asList(allfiles), new Comparator<File>() {            @Override            public int compare(File lhs, File rhs) {                if (lhs.lastModified() > rhs.lastModified()) {                    return -1;                } else {                    return 1;                }            }        });        if (allfiles == null) {            return list;        }        if(allfiles.length<page*pagenum){            allnum = allfiles.length;        }else            allnum = page*pagenum;        for (int k = 0; k < allnum; k++) {            final File fi = allfiles[k];            if (fi.isFile()) {                int idx = fi.getPath().lastIndexOf(".");                if (idx <= 0) {                    continue;                }                String suffix = fi.getPath().substring(idx);                if (suffix.toLowerCase().equals(".mp4")) {                    list.add(fi);                }            }        }        return list;    }*/}