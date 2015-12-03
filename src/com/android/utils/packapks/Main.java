package com.android.utils.packapks;

public class Main {

    /**
     * @param args
     * args[0] 打包方式，0：修改assets/channel_string.txt
     *                  1：修改AndroidManifest.xml
     * args[1] 产品名
     * args[2] 签名文件名
     * args[3] 签名文件密码
     *
     */
    public static void main(String[] args) {
        PackApksService packApksService = null;

        int packType = Integer.parseInt(args[0]);

        switch (packType){
            case 0:
                packApksService = new AssetsPackApksServiceImpl(args[2],args[3]);
                break;
            case 1:
                packApksService = new ManifestPackApksServiceImpl(args[2],args[3]);
                break;
            case 2:
                packApksService = new MultiManifestPackApksServiceImpl(args[2],args[3]);
                break;
        }

        packApksService.pack(args[1]);
    }



}
