package com.mossman.darren.elves_v_goblins;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Utils {

    public static ArrayList<String> readFile(InputStream inputStream) {
        ArrayList<String> res = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                res.add(line);
            }
        } catch (IOException e) {
            System.out.printf(e.getMessage());
        }
        return res;
    }

    public static ArrayList<String> readFile(Context context, String fileName) {
        AssetManager am = context.getAssets();
        try {
            InputStream inputStream = am.open(fileName);
            return readFile(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
