package gr.kgdev.fileuploader;

import gr.kgdev.fileuploader.model.User;

public class AppCache {

    private static User APP_USER;
    private static String DOWNLOAD_LOACTION = "/storage/emulated/0/Download/";

    public static User getAppUser() {
        return APP_USER;
    }

    public static void setAppUser(User appUser) {
        APP_USER = appUser;
    }

    public static String getDownloadLocation() {
        return DOWNLOAD_LOACTION;
    }

    public static void setDownloadLocation(String downloadLocation) {
        DOWNLOAD_LOACTION = downloadLocation;
    }
}
