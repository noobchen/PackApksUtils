package com.android.utils.packapks;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by pig on 2015-10-19.
 */
public class AssetsPackApksServiceImpl implements PackApksService {
    private final String configName = "channel_string.txt";
    private String keystoreName;
    private String keystorePwd;

    public AssetsPackApksServiceImpl(String keystoreName, String keystorePwd) {
        this.keystoreName = keystoreName;
        this.keystorePwd = keystorePwd;
    }

    @Override
    public void pack(String apkName) {
        ArrayList<String> channels = readChannels();

        if (channels != null && channels.size() != 0) {
            packApks(channels, apkName);
        } else {
            System.out.println("channels_dtyhf.txt 文件错误！");
        }
    }

    private void packApks(ArrayList<String> channels, String apkName) {
        String curFilePath = new File("").getAbsolutePath();
        String cmd = "cmd.exe /C java -jar apktool.jar d -f -s " + apkName;

        runCmd(cmd);

        String apkDirName = apkName.split(".apk")[0];

        File apkDir = new File(apkDirName);

        String configPath = apkDir.getAbsolutePath() + "\\assets\\" + configName;
        String newConfigPath = curFilePath + File.separator + configName;

        File configFile = new File(configPath);
        File newConfigFile = new File(newConfigPath);

        configFile.renameTo(newConfigFile);

        for (int i = 0; i < 10; i++) {
            if (newConfigFile.exists()) {
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        for (String channel : channels) {
            FileWriter fw = null;
            try {
                fw = new FileWriter(configPath);
                fw.write("channel=" + channel);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fw != null) {
                        fw.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            String unsignApkName = "unsigned.apk";
            String packCmd = String.format("cmd.exe /C java -jar apktool.jar b %s -o %s", apkDirName, unsignApkName);

            runCmd(packCmd);

            File signedApksDir = new File("./packedApks/" + apkDirName);

            if (!signedApksDir.exists()) {
                signedApksDir.mkdirs();
            }

            String signedApkName = "./packedApks/" + apkDirName + "/" + channel.split("\\|")[2] + ".apk";

            String signCmd = String.format("cmd.exe /C jarsigner -digestalg SHA1 -sigalg MD5withRSA -verbose -keystore %s -signedjar %s %s %s -storepass  %s", keystoreName, signedApkName, unsignApkName, keystoreName, keystorePwd);
            runCmd(signCmd);

            // 删除未签名的包
            File unApk = new File(unsignApkName);
            unApk.delete();
        }
    }

    private ArrayList<String> readChannels() {
        File txt = new File("channels_dtyhf.txt");

        if (!txt.exists()) {
            System.out.println("channels_dtyhf.txt 不存在！");
            return null;
        }
        ArrayList<String> channels = new ArrayList<>();

        FileReader fr = null;
        BufferedReader br = null;

        try {
            fr = new FileReader(txt);
            br = new BufferedReader(fr);

            String line;

            while ((line = br.readLine()) != null) {
                if (line.equals("")) {
                    System.out.print("Config.txt have empty String ");
                    return null;
                }
                channels.add(line);
            }

            return channels;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (fr != null) {
                    fr.close();
                }
                if (br != null) {
                    br.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void runCmd(String cmd) {
        Runtime rt = Runtime.getRuntime();
        BufferedReader br = null;
        InputStreamReader isr = null;
        try {
            Process p = rt.exec(cmd);
            // p.waitFor();
            isr = new InputStreamReader(p.getInputStream());
            br = new BufferedReader(isr);
            String msg = null;
            while ((msg = br.readLine()) != null) {
                System.out.println(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (isr != null) {
                    isr.close();
                }
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
