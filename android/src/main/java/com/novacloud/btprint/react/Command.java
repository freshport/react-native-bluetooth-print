package com.novacloud.btprint.react;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;


/**
 * Created by Nova on 16/10/25.
 */
public class Command {

    public static byte[] INIT = hexStringToBytes("1B 40");//初始化
    public static byte[] ALIGN_LEFT = hexStringToBytes("1B 61 00");//左对齐
    public static byte[] ALIGN_RIGHT = hexStringToBytes("1B 61 02");//居右对齐
    public static byte[] ALIGN_CENTER = hexStringToBytes("1B 61 01");//居中对齐

    public static byte[] OUT_PAPER = hexStringToBytes("0C");//页出纸
    public static byte[] UNDER_LINE = hexStringToBytes("1C 2D 01");//下划线
    public static byte[] NEW_LINE = hexStringToBytes("0A");//换行
    public static byte[] HEIGHT_LINE = hexStringToBytes("1B 33 16");//行间距

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

    public static boolean print(ReadableArray readableArray) throws Exception {
        if (readableArray == null || readableArray.size() == 0) return false;
        mService = BluetoothService.getInstance(null);
        if (mService.getState() != mService.STATE_CONNECTED) return false;

        mService = BluetoothService.getInstance(null);

        for (int i = 0; i < readableArray.size(); i++) {
            ReadableMap map = readableArray.getMap(i);
            mService.write(Command.INIT);
            //mService.write(Command.BOLD_FONT);

            String company = map.getString("user_company");
            mService.write(Command.ALIGN_CENTER);

            mService.write(company);
            mService.write(Command.NEW_LINE);
            mService.write(Command.ALIGN_LEFT);
            mService.write(Command.SMALL_FONT);
            mService.write(Command.HEIGHT_LINE);

            mService.write(addBlankCase("No." + map.getString("no"), map.getString("date"), Command.SMALL_FONT_NUMBER));

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
            mService.write(Command.NEW_LINE);
            mService.write(Command.NEW_LINE);
            mService.write(Command.NEW_LINE);
        }

        return true;
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

    public static String generateInfoVal(ReadableMap map) throws Exception {
        String ret = "";
        if (isValidVal(map.getString("product"))) {
            ret += map.getString("product");
            ret += "/";
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

        return ret;
    }
}
