package com.novacloud.btprint.react;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;


/**
 * Created by Nova on 16/10/25.
 */
public class Command {

    private final static int PRINT_DELAY_OFFSET = 3;
    private final static String SHANG_MI_BT_PRINTER_NAME = "InnerPrinter";
    public static byte[] INIT = hexStringToBytes("1B 40");//初始化
    public static byte[] ALIGN_LEFT = hexStringToBytes("1B 61 00");//左对齐
    public static byte[] ALIGN_RIGHT = hexStringToBytes("1B 61 02");//居右对齐
    public static byte[] ALIGN_CENTER = hexStringToBytes("1B 61 01");//居中对齐
    public static byte[] OUT_PAPER = hexStringToBytes("0C");//页出纸
    public static byte[] UNDER_LINE = hexStringToBytes("1C 2D 01");//下划线
    public static byte[] NEW_LINE = hexStringToBytes("0A");//换行
    public static byte[] HEIGHT_LINE = hexStringToBytes("1B 33 16");//行间距
    public static byte[] HEIGHT_LINE_57MM = hexStringToBytes("1B 33 2d");//行间距
    public static byte[] SMALL_FONT = hexStringToBytes("1B 4D 01");//小号字体
    public static byte[] NORMAL_FONT = hexStringToBytes("1B 4D 00");//正常
    public static byte[] BOLD_FONT = hexStringToBytes("1B 45 01");//粗体
    public static int NORMAL_FONT_NUMBER = 48;
    public static int SMALL_FONT_NUMBER = 72;
    private static BluetoothService mService;

    public static byte[] hexStringToBytes(String hexString) {
        hexString = hexString.toLowerCase();
        String[] hexStrings = hexString.split(" ");
        byte[] bytes = new byte[hexStrings.length];
        for (int i = 0; i < hexStrings.length; i++) {
            char[] hexChars = hexStrings[i].toCharArray();
            bytes[i] = (byte) (charToByte(hexChars[0]) << 4 | charToByte(hexChars[1]));
        }
        return bytes;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789abcdef".indexOf(c);
    }

    public static boolean print(final ReadableArray readableArray) throws Exception {
        mService = BluetoothService.getInstance(null);
        if (mService.getState() != mService.STATE_CONNECTED) return false;
        if (readableArray == null || readableArray.size() == 0) return false;
        boolean ret = false;
        if (mService.getConnectedDeviceName().equals(SHANG_MI_BT_PRINTER_NAME)) {
            ret = print57MM(readableArray);
        } else {
            ret=  print80MM(readableArray);
        }
        return  ret;
    }

    private static boolean print57MM(final  ReadableArray readableArray) throws Exception {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < readableArray.size(); i++) {
                    ReadableMap map = readableArray.getMap(i);
                    mService.write(Command.INIT);

                    mService.write(Command.ALIGN_CENTER);
                    mService.write(Command.HEIGHT_LINE_57MM);
                    
                    mService.write(map.getString("user_company") + map.getString("orderType"));
                    mService.write(Command.NEW_LINE);

                    mService.write(Command.ALIGN_LEFT);
                    mService.write("No." + map.getString("no"));
                    mService.write(Command.NEW_LINE);

                    mService.write(map.getString("printDate"));
                    mService.write(Command.NEW_LINE);

                    mService.write("公司名称：" + map.getString("company"));
                    mService.write(Command.NEW_LINE);

                    mService.write("联系人：" + map.getMap("saler").getString("user"));
                    mService.write(Command.NEW_LINE);

                    mService.write("品名/品种/规格/件重");
                    mService.write(Command.NEW_LINE);

                    ReadableArray list = map.getArray("list");
                    StringBuffer info = new StringBuffer("");
                    if (null != list) {
                        for (int j = 0; j < list.size(); j++) {
                            ReadableMap listMap = list.getMap(j);
                            info.append(new StringBuffer(generateInfoVal(listMap)));
                            info.append("\n");
                            info.append(listMap.getString("num") + "/");
                            info.append(listMap.getString("price") + "元/");
                            info.append(listMap.getString("cash") + "元");
                            info.append("\n");
                        }
                    }
                    mService.write(info.toString());
                    mService.write("总价：" + map.getMap("sum").getString("sum") + "元");
                    mService.write(Command.NEW_LINE);

                    mService.write(map.getString("type"));
                    mService.write(Command.NEW_LINE);

                    mService.write(map.getString("user_saler") + " " + map.getString("user_tel"));
                    mService.write(Command.NEW_LINE);

                    mService.write("注:本单等同于辉展《销售成交单》");
                    mService.write(Command.NEW_LINE);

                    mService.write("客户签名:");
                    mService.write(Command.NEW_LINE);

                    mService.write(Command.NEW_LINE);
                    mService.write(Command.NEW_LINE);
                    mService.write(Command.NEW_LINE);
                    mService.write(Command.NEW_LINE);
                    mService.write(Command.NEW_LINE);

                    if (map.hasKey("print_delivery") && "1".equals(map.getString("print_delivery"))) {
                        printDeliveryOrder(map);
                    }

                    int delay = mService.delay == 0 ? 5 * 1000 + PRINT_DELAY_OFFSET * 1000 : mService.delay * 1000 + PRINT_DELAY_OFFSET * 1000;
                    if (delay < 0) {
                        delay = 0;
                    }
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        thread.start();
        return true;
    }

    private static boolean print80MM(final ReadableArray readableArray) throws Exception  {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < readableArray.size(); i++) {
                    ReadableMap map = readableArray.getMap(i);
                    mService.write(Command.INIT);
                    //mService.write(Command.BOLD_FONT);

                    mService.write(Command.ALIGN_CENTER);

                    mService.write(map.getString("user_company") + map.getString("orderType"));
                    mService.write(Command.NEW_LINE);
                    mService.write(Command.ALIGN_LEFT);
                    mService.write(Command.SMALL_FONT);
                    mService.write(Command.HEIGHT_LINE);

                    mService.write(Command.SMALL_FONT);
                    mService.write(addBlankCase("No." + map.getString("no"), map.getString("date"), Command.SMALL_FONT_NUMBER));
                    mService.write(Command.NEW_LINE);

                    mService.write(Command.NORMAL_FONT);
                    mService.write(map.getString("printDate"));
                    mService.write(Command.NEW_LINE);

                    mService.write(Command.NORMAL_FONT);
                    mService.write("客户公司名称:" + map.getString("company") + "\n");
                    ReadableMap saler = map.getMap("saler");
                    mService.write("客户公司联系人:" + saler.getString("user") + "\n");

                    mService.write(addBlankCase("品名/品种/规格/件重", "退损 数量 单价   金额", Command.NORMAL_FONT_NUMBER));

                    ReadableArray list = map.getArray("list");
                    String info = "\n";
                    if (list != null) {
                        for (int j = 0; j < list.size(); j++) {
                            String lineStart = "";
                            String lineEnd = "";
                            ReadableMap listMap = list.getMap(j);
                            lineStart = generateInfoVal(listMap);
                            lineEnd = addBlankCase(listMap.getString("returnnum"), "", 4) +
                                    addBlankCase(listMap.getString("num"), "", 4) + " " +
                                    addBlankCase(listMap.getString("price"), "", 5) +
                                    addBlankCase("", listMap.getString("cash"), 6);
                            info += addBlankCase(lineStart, lineEnd, Command.NORMAL_FONT_NUMBER);
                            info += "\n";
                        }
                    }
                    mService.write(info);
                    ReadableMap sum = map.getMap("sum");
                    mService.write(addBlankCase("", addBlankCase("合计", sum.getString("sum"), 16), Command.NORMAL_FONT_NUMBER));
                    mService.write("\n");
                    mService.write(addBlankCase("销售员:" + map.getString("user_saler") + " " + map.getString("user_tel"), map.getString("type"), Command.NORMAL_FONT_NUMBER));
                    mService.write("\n************************************************");
                    mService.write("\n注：本销售单等同于辉展市场巜销售成交单》");
                    mService.write("\n客户签名:");

                    mService.write(Command.NEW_LINE);
                    mService.write(Command.NEW_LINE);
                    mService.write(Command.NEW_LINE);


                    if (map.hasKey("print_delivery") && "1".equals(map.getString("print_delivery"))) {
                        printDeliveryOrder(map);
                    } else {
                        mService.write(Command.NEW_LINE);
                        mService.write(Command.NEW_LINE);
                        mService.write(Command.NEW_LINE);
                    }

                    int delay = mService.delay == 0 ? 5 * 1000 + PRINT_DELAY_OFFSET * 1000 : mService.delay * 1000 + PRINT_DELAY_OFFSET * 1000;
                    if (delay < 0) {
                        delay = 0;
                    }
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        thread.start();

        return true;
    }

    private static void printDeliveryOrder(ReadableMap map) {

        mService.write(Command.ALIGN_CENTER);
        mService.write(map.getString("user_company") + "-送货单");
        mService.write(Command.NEW_LINE);

        mService.write(Command.ALIGN_LEFT);
        mService.write("订单号：" + map.getString("no"));
        mService.write(Command.NEW_LINE);

        mService.write("客户：" + map.getString("company"));
        mService.write(Command.NEW_LINE);

        ReadableArray list = map.getArray("list");
        if (list != null) {
            for (int j = 0; j < list.size(); j++) {
                ReadableMap listMap = list.getMap(j);
                String line = "商品：" + generateInfoVal(listMap);
                mService.write(line);
                mService.write(Command.NEW_LINE);
                mService.write("数量：" + listMap.getString("num"));
                mService.write(Command.NEW_LINE);
            }
        }

        mService.write("销售员：" + map.getString("user_saler") + " " + map.getString("user_tel"));
        mService.write(Command.NEW_LINE);

        ReadableArray deliveryArea =  map.getArray("delivery_area");
        String joinedArea = "";
        for (int i = 0; i < deliveryArea.size(); i++) {
            joinedArea = joinedArea + deliveryArea.getString(i) + " ";
        }
        mService.write("送货区域：" + joinedArea);
        mService.write(Command.NEW_LINE);

        mService.write("车牌号：" + map.getString("plate_num"));


        mService.write(Command.NEW_LINE);
        mService.write(Command.NEW_LINE);
        mService.write(Command.NEW_LINE);
        mService.write(Command.NEW_LINE);
        mService.write(Command.NEW_LINE);

    }


    public static String addBlankCase(String first, String end, int number) {
        String str = null;
        int length = 0;
        for (int i = 0; i < first.length(); i++) {
            if (first.charAt(i) >= '\u4e00' && first.charAt(i) <= '\u9fa5') {
                length += 2;
            } else {
                length += 1;
            }
        }
        for (int i = 0; i < end.length(); i++) {
            if (end.charAt(i) >= '\u4e00' && end.charAt(i) <= '\u9fa5') {
                length += 2;
            } else {
                length += 1;
            }
        }
        if (length < number) {
            for (int i = 0; i < number - length; i++) {
                first = first + " ";
            }
        }
        return first + end;
    }

    private static boolean isValidVal(Object obj) {
        if (obj != null && !obj.toString().equals("null") && !obj.toString().isEmpty()) {
            return true;
        }
        return false;
    }

    public static String generateInfoVal(ReadableMap map) {
        String ret = "";
        try {
            if (isValidVal(map.getString("product"))) {
                ret += map.getString("product");
                ret += "/";
            }
            if (isValidVal(map.getString("note"))) {
                ret += "(";
                ret += map.getString("note");
                ret += ")/";
            }
            if (isValidVal(map.getString("variety"))) {
                ret += map.getString("variety");
                ret += "/";
            }
            if (isValidVal(map.getString("spec"))) {
                ret += map.getString("spec");
                ret += "/";
            }
            if (isValidVal(map.getString("weight"))) {
                ret += map.getString("weight");
                ret += "/";
            }

            if (ret.endsWith("/")) {
                ret = ret.substring(0, ret.length() - 1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }
}
