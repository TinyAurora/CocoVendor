package cn.edu.stu.max.cocovendor.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edu.stu.max.cocovendor.javaClass.FileService;
import cn.edu.stu.max.cocovendor.adapters.MyInternalListAdapter;
import cn.edu.stu.max.cocovendor.adapters.MyInternalSettingListAdapter;
import cn.edu.stu.max.cocovendor.adapters.MyUSBSettingListAdapter;
import cn.edu.stu.max.cocovendor.adapters.MyUSBListAdapter;
import cn.edu.stu.max.cocovendor.R;


public class AdSettingActivity extends AppCompatActivity {

    private static final String adSettingDataFileName = "sharedfile";     // 定义保存的文件的名称

    private final static String FROMPATH = "/mnt/usb_storage/USB_DISK2/udisk0/advertisement/";   // U盘广告存储路径
    private final static String TOPATH = "/storage/sdcard0/tencent/QQfile_recv/b/";               // 本机广告存储路径

    private static boolean isInternalPathChanged = false;

    private TextView textViewAdSettingHint;          // 广告设置文字提醒
    private VideoView videoViewAd;    // 播放广告视频
    private ImageView imageViewAd;    // 播放图片
    private Button buttonAdLoadAd;     // 加载U盘广告按钮

    private ListView listViewAdUSB;         // U盘广告列表
    private ListView listViewAdInternal;   // 本机广告列表
    private ListView listViewAdUSBSetting;    // U盘广告设置列表
    private ListView listViewAdInternalSetting;    // 本机广告设置列表

    private MyUSBListAdapter myUSBListAdapter;    // U盘广告内容适配器
    private MyInternalListAdapter myInternalListAdapter;    // 本机广告内容适配器
    private MyUSBSettingListAdapter myUSBSettingListAdapter;      // U盘广告内容设置适配器
    private MyInternalSettingListAdapter myInternalSettingListAdapter;  // 本机广告内容设置适配器

    private boolean hasAddedFlag = false;             // 用来判断有无文件添加
    private boolean hasFetchedFlag = false;           // 用来判断有无文件取回
    private boolean adSettingFlag = false;
    private boolean getOnceFlag = false;

    private String[] usbFileNameList = new String[100];
    private int usbFileNameListIndex = 0;

    private String[] internalFileNameList;
    private String[] tempInternalFileNameList;
    private int[] internalFileNameListIndex;

    private File[] fileToGetName;

    private boolean[] internalPositionFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().hide();

        setContentView(R.layout.activity_ad_setting);

        SharedPreferences share = super.getSharedPreferences(adSettingDataFileName, MODE_PRIVATE);  // 实例化
        final SharedPreferences.Editor editor = share.edit();   // 使处于可编辑状态

        final Button buttonAdReturn = (Button) findViewById(R.id.btn_ad_return);
        final Button buttonAdAdd = (Button) findViewById(R.id.btn_ad_add);
        final Button buttonAdDelete = (Button) findViewById(R.id.btn_ad_delete);
        final Button buttonAdFetch = (Button) findViewById(R.id.btn_ad_fetch);
        final Button buttonAdSetting = (Button) findViewById(R.id.btn_ad_setting);

        buttonAdLoadAd = (Button) findViewById(R.id.btn_ad_load_ad);

        textViewAdSettingHint = (TextView) findViewById(R.id.tv_ad_setting_hint);
        textViewAdSettingHint.setVisibility(View.INVISIBLE);

        imageViewAd = (ImageView) findViewById(R.id.iv_ad);

        videoViewAd = (VideoView) findViewById(R.id.vv_ad);

        listViewAdUSB = (ListView) findViewById(R.id.lv_ad_usb);
        listViewAdInternal = (ListView) findViewById(R.id.lv_ad_internal);
        listViewAdUSBSetting = (ListView) findViewById(R.id.lv_ad_usb_setting);
        listViewAdInternalSetting = (ListView) findViewById(R.id.lv_ad_internal_setting);

        imageViewAd.setVisibility(View.VISIBLE);
        videoViewAd.setVisibility(View.INVISIBLE);

        buttonAdReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editor.putBoolean("isAdSettingChanged", true);
                for (int i = 0; i < usbFileNameListIndex; i++) {
                    editor.putString("Ad_" + i, usbFileNameList[i]);
                    editor.putString("Frequency_" + i, MyUSBSettingListAdapter.saveMap.get(i));
                }
                editor.apply();
                finish();
            }
        });

        myInternalSettingListAdapter = new MyInternalSettingListAdapter(this, getList(TOPATH));
        listViewAdInternalSetting.setAdapter(myInternalSettingListAdapter);
        listViewAdInternalSetting.setVisibility(View.INVISIBLE);
        listViewAdInternalSetting.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!adSettingFlag) {

                } else {

                    if (!internalFileNameList[position].equals("")) {
                        usbFileNameList[usbFileNameListIndex] = internalFileNameList[position];
                        usbFileNameListIndex ++;
                        internalFileNameListIndex[usbFileNameListIndex] = position;
                        internalFileNameList[position] = "";

                        MyInternalSettingListAdapter.setList(getNameList(internalFileNameList));
                        myInternalSettingListAdapter.notifyDataSetChanged();
                        MyUSBSettingListAdapter.setIsSetting(true);
                        MyUSBSettingListAdapter.saveMap.put(usbFileNameListIndex, "1");

                        MyUSBSettingListAdapter.setList(getSettingList(usbFileNameList, usbFileNameListIndex));
                        myUSBSettingListAdapter.notifyDataSetChanged();
                    }
                }
            }
        });

        myUSBSettingListAdapter = new MyUSBSettingListAdapter(this, getVoidList());
        listViewAdUSBSetting.setAdapter(myUSBSettingListAdapter);
        listViewAdUSBSetting.setVisibility(View.INVISIBLE);
        listViewAdUSBSetting.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!adSettingFlag) {

                } else {

                    for (int i = 0; i < tempInternalFileNameList.length; i++) {
                        if (tempInternalFileNameList[i].equals(usbFileNameList[position])) {
                            internalFileNameList[i] = usbFileNameList[position];
                        }
                    }
                    for (int i = position; i < usbFileNameListIndex - 1; i++) {
                        usbFileNameList[i] = usbFileNameList[i + 1];
                        MyUSBSettingListAdapter.saveMap.put(i, MyUSBSettingListAdapter.saveMap.get(i + 1));
                    }
                    usbFileNameList[usbFileNameListIndex - 1] = "";
                    usbFileNameListIndex = usbFileNameListIndex - 1;
                    MyInternalSettingListAdapter.setList(getNameList(internalFileNameList));
                    myInternalSettingListAdapter.notifyDataSetChanged();
                    MyUSBSettingListAdapter.setIsSetting(true);
                    MyUSBSettingListAdapter.setList(getSettingList(usbFileNameList, usbFileNameListIndex));
                    myUSBSettingListAdapter.notifyDataSetChanged();
                }
            }
        });

        myUSBListAdapter = new MyUSBListAdapter(this, getList(FROMPATH));
        listViewAdUSB.setAdapter(myUSBListAdapter);
        listViewAdUSB.setVisibility(View.VISIBLE);
        listViewAdUSB.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (USBExists()) {
                    File[] files = FileService.getFiles(FROMPATH);
                    playVideo(files[position].getPath());
                    videoViewAd.setVisibility(View.VISIBLE);
                    imageViewAd.setVisibility(View.INVISIBLE);
                }
            }
        });


        myInternalListAdapter = new MyInternalListAdapter(this, getList(TOPATH));
        listViewAdInternal.setAdapter(myInternalListAdapter);
        listViewAdInternal.setVisibility(View.VISIBLE);
        listViewAdInternal.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                File[] currentFiles = FileService.getFiles(TOPATH);
                playVideo(currentFiles[position].getPath());
                videoViewAd.setVisibility(View.VISIBLE);
                imageViewAd.setVisibility(View.INVISIBLE);
            }
        });

        videoViewAd.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                videoViewAd.start();
            }
        });

        buttonAdAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (USBExists()) {
                    File[] currentFiles = FileService.getFiles(FROMPATH);
                    for (int i = 0; i < MyUSBListAdapter.isFileAdded.length; i++) {
                        if (MyUSBListAdapter.isFileAdded[i]) {
                            isInternalPathChanged = true;
                            FileService.copyFile(currentFiles[i].getPath(), TOPATH + currentFiles[i].getName());
                            hasAddedFlag = true;
                        }
                    }
                    if (hasAddedFlag) {
                        MyInternalListAdapter.setList(getList(TOPATH));
                        MyInternalListAdapter.setIsSelectedAndIsFileAdded(getList(TOPATH));
                        myInternalListAdapter.notifyDataSetChanged();
                        MyUSBListAdapter.setIsSelectedAndIsFileAdded(getList(FROMPATH));
                        myUSBListAdapter.notifyDataSetChanged();
                        hasAddedFlag = false;
                    }
                }
            }
        });

        buttonAdFetch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (USBExists()) {
                    File[] currentFiles = FileService.getFiles(TOPATH);
                    for (int i = 0; i < MyInternalListAdapter.isFileAdded.length; i++) {
                        if (MyInternalListAdapter.isFileAdded[i]) {
                            FileService.copyFile(currentFiles[i].getPath(), FROMPATH + currentFiles[i].getName());
                            hasFetchedFlag = true;
                        }
                    }
                    if (hasFetchedFlag) {
                        MyUSBListAdapter.setList(getList(FROMPATH));
                        MyUSBListAdapter.setIsSelectedAndIsFileAdded(getList(FROMPATH));
                        myUSBListAdapter.notifyDataSetChanged();
                        MyInternalListAdapter.setIsSelectedAndIsFileAdded(getList(TOPATH));
                        myInternalListAdapter.notifyDataSetChanged();
                        hasFetchedFlag = false;
                    }
                }
            }
        });

        buttonAdDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (USBExists()) {
                    if (FileService.isFileAdded(TOPATH, MyInternalListAdapter.isFileAdded)) {
                        isInternalPathChanged = true;
                    }

                    if (FileService.isFileAdded(FROMPATH, MyUSBListAdapter.isFileAdded)
                            || FileService.isFileAdded(TOPATH, MyInternalListAdapter.isFileAdded)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(AdSettingActivity.this);
                        builder.setMessage("确认删除吗？");
                        builder.setTitle("提示");
                        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                FileService.deleteFile(FROMPATH, MyUSBListAdapter.isFileAdded);
                                FileService.deleteFile(TOPATH, MyInternalListAdapter.isFileAdded);

                                MyInternalListAdapter.setList(getList(TOPATH));
                                MyInternalListAdapter.setIsSelectedAndIsFileAdded(getList(TOPATH));
                                myInternalListAdapter.notifyDataSetChanged();
                                MyUSBListAdapter.setList(getList(FROMPATH));
                                MyUSBListAdapter.setIsSelectedAndIsFileAdded(getList(FROMPATH));
                                myUSBListAdapter.notifyDataSetChanged();
                            }
                        });
                        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        builder.setCancelable(false);
                        builder.create().show();
                    }
                } else {
                    if (FileService.isFileAdded(TOPATH, MyInternalListAdapter.isFileAdded)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(AdSettingActivity.this);
                        builder.setMessage("确认删除吗？");
                        builder.setTitle("提示");
                        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                FileService.deleteFile(TOPATH, MyInternalListAdapter.isFileAdded);
                                MyInternalListAdapter.setList(getList(TOPATH));
                                MyInternalListAdapter.setIsSelectedAndIsFileAdded(getList(TOPATH));
                                myInternalListAdapter.notifyDataSetChanged();
                            }
                        });
                        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        builder.setCancelable(false);
                        builder.create().show();
                    }
                }
            }
        });

        buttonAdSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (adSettingFlag) {

                    buttonAdAdd.setVisibility(View.VISIBLE);
                    buttonAdDelete.setVisibility(View.VISIBLE);
                    buttonAdFetch.setVisibility(View.VISIBLE);
                    buttonAdSetting.setText("广告播放顺序、次数设置");

                    textViewAdSettingHint.setVisibility(View.INVISIBLE);
                    buttonAdLoadAd.setVisibility(View.VISIBLE);

                    listViewAdUSBSetting.setVisibility(View.INVISIBLE);
                    listViewAdUSB.setVisibility(View.VISIBLE);
                    listViewAdInternalSetting.setVisibility(View.INVISIBLE);
                    listViewAdInternal.setVisibility(View.VISIBLE);

                    videoViewAd.start();

                    adSettingFlag = false;
                    isInternalPathChanged = false;
                } else {

                    if (!getOnceFlag) {

                        usbFileNameListIndex = 0;

                        fileToGetName = FileService.getFiles(TOPATH);
                        internalFileNameListIndex = new int[fileToGetName.length + 1];
                        internalFileNameList = new String[fileToGetName.length];
                        tempInternalFileNameList = new String[fileToGetName.length];
                        internalPositionFlag = new boolean[fileToGetName.length];
                        for (int i = 0; i < fileToGetName.length; i++) {
                            internalFileNameList[i] = fileToGetName[i].getName();
                            tempInternalFileNameList[i] = fileToGetName[i].getName();
                            internalPositionFlag[i] = true;
                            internalFileNameListIndex[i] = 0;
                        }
                        getOnceFlag = true;
                    }

                    if (isInternalPathChanged) {
                        MyUSBSettingListAdapter.setIsSetting(true);
                        MyUSBSettingListAdapter.setList(getSettingList(usbFileNameList, 0));
                        myUSBSettingListAdapter.notifyDataSetChanged();

                        usbFileNameListIndex = 0;

                        fileToGetName = FileService.getFiles(TOPATH);
                        internalFileNameListIndex = new int[fileToGetName.length + 1];
                        internalFileNameList = new String[fileToGetName.length];
                        tempInternalFileNameList = new String[fileToGetName.length];
                        internalPositionFlag = new boolean[fileToGetName.length];
                        for (int i = 0; i < fileToGetName.length; i++) {
                            internalFileNameList[i] = fileToGetName[i].getName();
                            tempInternalFileNameList[i] = fileToGetName[i].getName();
                            internalPositionFlag[i] = true;
                            internalFileNameListIndex[i] = 0;
                        }
                    }
                    buttonAdAdd.setVisibility(View.INVISIBLE);
                    buttonAdDelete.setVisibility(View.INVISIBLE);
                    buttonAdFetch.setVisibility(View.INVISIBLE);
                    buttonAdSetting.setText("返回上一步");

                    MyInternalSettingListAdapter.setList(getNameList(internalFileNameList));
                    myInternalSettingListAdapter.notifyDataSetChanged();

                    textViewAdSettingHint.setVisibility(View.VISIBLE);
                    buttonAdLoadAd.setVisibility(View.INVISIBLE);
                    listViewAdUSB.setVisibility(View.INVISIBLE);
                    listViewAdUSBSetting.setVisibility(View.VISIBLE);
                    listViewAdInternal.setVisibility(View.INVISIBLE);
                    listViewAdInternalSetting.setVisibility(View.VISIBLE);

                    videoViewAd.pause();

                    adSettingFlag = true;
                }
            }
        });

        buttonAdLoadAd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (USBExists()) {
                    MyUSBListAdapter.setList(getList(FROMPATH));
                    MyUSBListAdapter.setIsSelectedAndIsFileAdded(getList(FROMPATH));
                    myUSBListAdapter.notifyDataSetChanged();
                    listViewAdUSB.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(AdSettingActivity.this, "请插入U盘", Toast.LENGTH_LONG).show();
                    MyUSBListAdapter.setList(getList(FROMPATH));
                    MyUSBListAdapter.setIsSelectedAndIsFileAdded(getList(FROMPATH));
                    myUSBListAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    public void playVideo(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            imageViewAd.setVisibility(View.INVISIBLE);
            videoViewAd.setVisibility(View.VISIBLE);
            videoViewAd.setVideoPath(file.getPath());
            videoViewAd.start();
        } else {
            imageViewAd.setVisibility(View.VISIBLE);
            videoViewAd.setVisibility(View.INVISIBLE);
        }
    }

    private List<Map<String, Object>> getList(String filePath)
    {
        File file = new File(filePath);

        if (!file.exists()) {
            List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
            Map<String, Object> map;
            for(int i = 0; i < 10; i++)
            {
                map = new HashMap<String, Object>();
                map.put("img", R.mipmap.ic_launcher);
                map.put("title", "请插入U盘");
                list.add(map);
            }
            return list;
        } else {
            File[] files = FileService.getFiles(filePath);
            List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
            Map<String, Object> map;
            for(int i = 0; i < files.length; i++)
            {
                map = new HashMap<String, Object>();
                map.put("img", R.mipmap.ic_launcher);
                map.put("title", files[i].getName());
                list.add(map);
            }
            return list;
        }
    }

    private List<Map<String, Object>> getVoidList()
    {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map;
        for(int i = 0; i < 0; i++)
        {
            map = new HashMap<String, Object>();
            map.put("img", R.mipmap.ic_launcher);
            map.put("title", "");
            list.add(map);
        }
        return list;
    }

    private List<Map<String, Object>> getSettingList(String[] fileNameList, int fileNameListIndex)
    {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map;
        for(int i = 0; i < fileNameListIndex; i++)
        {
            map = new HashMap<String, Object>();
            map.put("img", R.mipmap.ic_launcher);
            map.put("title", fileNameList[i]);
            map.put("tv_order", i + 1 + ".");
            map.put("tv_frequency", "1");
            list.add(map);
        }
        return list;
    }

    private List<Map<String, Object>> getNameList(String[] fileNameList)
    {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map;
        for(int i = 0; i < fileNameList.length; i++)
        {
            map = new HashMap<String, Object>();
            map.put("img", R.mipmap.ic_launcher);
            map.put("title", fileNameList[i]);
            list.add(map);
        }
        return list;
    }

    private boolean USBExists() {
        File usbFile = new File(FROMPATH);
        if (usbFile.exists()) {
            return true;
        } else {
            return false;
        }
    }
}
