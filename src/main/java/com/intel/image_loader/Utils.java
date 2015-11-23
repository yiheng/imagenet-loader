package com.intel.image_loader;

public class Utils {
    public static void requires(boolean verify, String msg) {
        if(!verify) {
            throw new IllegalArgumentException(msg);
        }
    }

    public static void requires(boolean verify) {
        requires(verify, "");
    }
}
