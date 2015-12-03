package com.android.utils.packapks;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by pig on 2015-10-19.
 */
public class MultiManifestPackApksServiceImpl implements PackApksService {
    private final String configName = "AndroidManifest.xml";
    private String keystoreName;
    private String keystorePwd;

    public MultiManifestPackApksServiceImpl(String keystoreName, String keystorePwd) {
        this.keystoreName = keystoreName;
        this.keystorePwd = keystorePwd;
    }

    @Override
    public void pack(String apkName) {
        ArrayList<HashMap<String, String>> channels = readChannels();

        if (channels != null && channels.size() != 0) {
            packApks(channels, apkName);
        } else {
            System.out.println("Channels.txt 文件错误！");
        }
    }

    private void packApks(ArrayList<HashMap<String, String>> channels, String apkName) {
        String curFilePath = new File("").getAbsolutePath();
        String cmd = "cmd.exe /C java -jar apktool.jar d -f -s " + apkName;

        runCmd(cmd);

        String apkDirName = apkName.split(".apk")[0];

        File apkDir = new File(apkDirName);

        String configPath = apkDir.getAbsolutePath() + File.separator + configName;
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

        String line = null;
        for (HashMap<String, String> channel : channels) {
            FileWriter fw = null;
            FileReader fr = null;
            BufferedReader br = null;
            try {
                fr = new FileReader(newConfigFile);
                br = new BufferedReader(fr);
                StringBuffer sb = new StringBuffer();
                while ((line = br.readLine()) != null) {
                    if (line.contains("\"UMENGCHANNEL\"")) {
                        line = line.replace("UMENGCHANNEL", channel.get("UMENGCHANNEL"));

                    }

                    if (line.contains("\"LLTTCPCHANNELID\"")) {
                        line = line.replace("LLTTCPCHANNELID", channel.get("UMENGCHANNEL"));

                    }

                    if (line.contains("\"YLSOURCEID\"")) {
                        line = line.replace("YLSOURCEID", channel.get("YLSOURCEID"));

                    }

                    if (line.contains("\"ZHANGQD\"")) {
                        line = line.replace("ZHANGQD", channel.get("ZHANGQD"));

                    }

                    if (line.contains("\"TIANZHCHANNEL\"")) {
                        line = line.replace("TIANZHCHANNEL", channel.get("UMENGCHANNEL"));

                    }


                    sb.append(line + "\n");
                }

                fw = new FileWriter(configFile);

                fw.write(sb.toString());


            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fr != null) {
                        fr.close();
                    }
                    if (br != null) {
                        br.close();
                    }
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

            String signedApkName = "./packedApks/" + apkDirName + "/" + apkDirName + "_" + channel.get("UMENGCHANNEL") + ".apk";

            String signCmd = String.format("cmd.exe /C jarsigner -digestalg SHA1 -sigalg MD5withRSA -verbose -keystore %s -signedjar %s %s %s -storepass  %s", keystoreName, signedApkName, unsignApkName, keystoreName, keystorePwd);
            runCmd(signCmd);

            // 删除未签名的包
            File unApk = new File(unsignApkName);
            unApk.delete();
        }
        newConfigFile.delete();
    }

    private ArrayList<HashMap<String, String>> readChannels() {
        File txt = new File("channels_multi.txt");

        if (!txt.exists()) {
            System.out.println("channels_multi.txt 不存在！");
            return null;
        }
        ArrayList<HashMap<String, String>> channels = new ArrayList<>();

        FileReader fr = null;
        BufferedReader br = null;

        try {
            fr = new FileReader(txt);
            br = new BufferedReader(fr);

            String line;

            while ((line = br.readLine()) != null) {
                if (line.equals("")) {
                    System.out.print("channels_multi.txt have empty String ");
                    return null;
                }
                String[] pairs = line.split(",");

                if (pairs.length > 0) {
                    HashMap<String, String> result = new HashMap<>();
                    for (String pair : pairs) {
                        result.put(pair.split("=")[0], pair.split("=")[1]);
                    }

                    channels.add(result);
                }
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
