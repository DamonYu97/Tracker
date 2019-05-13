package com.example.damon.tracker;


import java.util.HashMap;
import java.util.Map;

public class Permission {
    public static Map<String,Boolean> isPermittedMap = new HashMap<String,Boolean>();
    public static Map<String,Integer> permMenuIDMap = new HashMap<String,Integer>();
    private static final int LENGTH = 6;
    private static final String[] PERMISSION_NOS = {"01000","02000","03000","04000","05000","06000"};
    private static final Boolean[] IS_PERMITTED = {false,false,false,false,false,false};
    private static final int[] MENU_ID = {R.id.nav_accounts,R.id.nav_location,R.id.nav_infoManage,R.id.nav_configure,R.id.nav_statistic,0};

    public static void init(){
        //initialize the permission information
        for (int i = 0;i < LENGTH;i++) {
            isPermittedMap.put(PERMISSION_NOS[i],IS_PERMITTED[i]);
            permMenuIDMap.put(PERMISSION_NOS[i],MENU_ID[i]);
        }
    }
}
