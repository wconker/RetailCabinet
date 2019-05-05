package com.sovell.retail_cabinet.manager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.snbc.bvm.BVMAidlInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

/**
 * 货柜管理
 */
public class BVMManager {

    private static final int NULL_AIDL = -1;
    private static final int BOX_ID = 1;
    private static final String KEY = "ee9vvMJ=1BZaDFHk";

    private static Context mContext;
    private static BVMAidlInterface aidlInterface;
    private static ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            aidlInterface = BVMAidlInterface.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    /**
     * 绑定服务
     */
    public static void bindService(Context context) {
        mContext = context;
        Intent intent = new Intent();
        intent.setAction("android.intent.action.SnbcBvmService");
        intent.setPackage("com.snbc.bvm");
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    /**
     * 解绑服务
     */
    public static void unbind() {
        mContext.unbindService(connection);
    }

    /**
     * 1.设置进入维护的密码
     * 2.进入维护
     */
    public static int initSetKey() throws RemoteException {
        synchronized (BVMManager.class) {
            if (null == aidlInterface) {
                return NULL_AIDL;
            }
            return aidlInterface.BVMSetKey(KEY);
        }
    }

    /**
     * 进入、退出维护
     *
     * @param isEnter 是否进入
     */
    public static int maintainState(boolean isEnter) throws RemoteException {
        synchronized (BVMManager.class) {
            if (null == aidlInterface) {
                return NULL_AIDL;
            }
            return aidlInterface.BVMSetWorkMode(BOX_ID, isEnter ? 1 : 0);
        }
    }

    /**
     * 展仓灯带开关
     *
     * @param type   灯光类型：0：顶部灯 1：展仓1 2：展仓 3：取货灯
     * @param isOpen 是否开启灯带
     */
    public static int lightOnOff(int type, boolean isOpen) throws RemoteException {
        synchronized (BVMManager.class) {
            if (null == aidlInterface) {
                return NULL_AIDL;
            }
            //0：开启 1：关闭
            return aidlInterface.BVMSetLightState(BOX_ID, type, isOpen ? 1 : 0);
        }
    }

    /**
     * 灯光状态
     *
     * @return boolean[0] true:展仓灯1开 false : 展仓灯1关
     * boolean[1] true:展仓灯2开 false：展仓灯2关
     * boolean[2] true:取货指示灯开 false：取货指示灯关
     * boolean[3] true:顶部灯箱开 false：顶部灯箱关
     */
    public static boolean[] lightState() throws RemoteException {
        synchronized (BVMManager.class) {
            if (null == aidlInterface) {
                return new boolean[]{false, false, false, false};
            }
            return aidlInterface.BVMQueryLightState(BOX_ID);
        }
    }

    /**
     * 扫描货道，重新上电或货道改变时调用
     */
    public static int initXYRoad() throws RemoteException {
        synchronized (BVMManager.class) {
            if (null == aidlInterface) {
                return NULL_AIDL;
            }
            return aidlInterface.BVMInitXYRoad(BOX_ID, 0, 0, 0);
        }
    }

    /**
     * 获取箱格详情
     */
    public static int[] goodsDetail() throws RemoteException {
        synchronized (BVMManager.class) {
            if (null == aidlInterface) {
                return new int[]{NULL_AIDL};
            }
            return aidlInterface.BVMQueryInitResult(BOX_ID);
        }
    }

    /**
     * 获取设备整体状态
     *
     * @return 0x00  启动完成，未自检
     * 0x01	启动自检过程中（上电时自检，大概40s左右）
     * 0x02	启动完成（待机），等待售货
     * 0x03	售货中（出货指示—出货完成—出货后硬件复位动作完成）
     * 0x04	维护中（补货、货道扫描、升级、参数设定、制冷制热准备中等。进入条件：开门进入或指令进入。退出条件：门关闭并且维护标志清除后退出）
     * 0x05	BOOT中（升级过程中）
     * 0x06	停止中（所有的商品都不可以售卖，原因可能是发生停机故障，销售停止时间带，超温停售等）
     */
    public static int deviceState() throws RemoteException {
        synchronized (BVMManager.class) {
            if (null == aidlInterface) {
                return NULL_AIDL;
            }
            return aidlInterface.BVMGetRunningState(BOX_ID);
        }
    }

    /**
     * 获取指定货道状态
     */
    public static int boxState(int ySelect, int xSelect) throws RemoteException {
        synchronized (BVMManager.class) {
            if (null == aidlInterface) {
                return NULL_AIDL;
            }
            return aidlInterface.BVMQueryGoodsState(BOX_ID, ySelect, xSelect);
        }
    }

    /**
     * 门状态查询
     *
     * @return 索引0: 2：柜门开 1：柜门关（主副门任意门开认定为门开）
     * 索引1: 2：闸门开 1：闸门关
     */
    public static int[] doorState() throws RemoteException {
        synchronized (BVMManager.class) {
            if (null == aidlInterface) {
                return new int[]{NULL_AIDL, NULL_AIDL};
            }
            return aidlInterface.BVMGetDoorState(BOX_ID);
        }
    }

    /**
     * 再次开门
     */
    public static int openDoorAgain() throws RemoteException {
        synchronized (BVMManager.class) {
            if (null == aidlInterface) {
                return NULL_AIDL;
            }
            return aidlInterface.BVMReSaleGoods(BOX_ID);
        }
    }

    /**
     * 故障查询
     */
    public static String[] faultQuery() throws RemoteException {
        synchronized (BVMManager.class) {
            if (null == aidlInterface) {
                return new String[]{"-1 AidlInterfaceNull"};
            }
            String[] error = aidlInterface.BVMGetFGFault(BOX_ID);
            List<String> listError = new ArrayList<>();
            for (String str : error) {
                if (!str.contains("6050") && !str.contains("63BD")) {
                    listError.add(str);
                }
            }
            if (listError.size() <= 0) {
                return new String[]{""};
            }
            String[] result = new String[listError.size()];
            for (int i = 0; i < listError.size(); i++) {
                result[i] = listError.get(i);
            }
            return result;
        }
    }

    /**
     * 故障清楚
     */
    public static int faultClean() throws RemoteException {
        synchronized (BVMManager.class) {
            if (null == aidlInterface) {
                return NULL_AIDL;
            }
            return aidlInterface.BVMCleanSysFault(BOX_ID);
        }
    }

    /**
     * 获取固件信息
     */
    public static String deviceInfo() throws RemoteException {
        synchronized (BVMManager.class) {
            if (null == aidlInterface) {
                return "aidlInterface null";
            }
            String info_0 = aidlInterface.BVMQueryBoxInfo(BOX_ID, 0);
            String info_1 = aidlInterface.BVMQueryBoxInfo(BOX_ID, 1);
            return info_1 + "\n" + info_0;
        }
    }

    /**
     * 取货
     *
     * @param json 取货参数
     */
    public static String takeGoods(String json) throws RemoteException {
        synchronized (BVMManager.class) {
            if (null == aidlInterface) {
                return "aidlInterface null";
            }
            return aidlInterface.BVMStartShip(json);
        }
    }

    /**
     * 制冷制热模式查询
     *
     * @return 0：常温模式 1：制冷模式 2：制热模式
     */
    public static int currentTempModel() throws RemoteException {
        synchronized (BVMManager.class) {
            if (null == aidlInterface) {
                return NULL_AIDL;
            }
            return aidlInterface.BVMGetColdHeatModel(BOX_ID);
        }
    }

    /**
     * 查询当前温湿度
     *
     * @return int[0]:箱体温度;int[1]:外界环境温度;int[2]:箱体湿度
     */
    public static int[] currentTemp() throws RemoteException {
        synchronized (BVMManager.class) {
            if (null == aidlInterface) {
                return new int[]{NULL_AIDL, NULL_AIDL, NULL_AIDL};
            }
            return aidlInterface.BVMGetColdHeatTemp(BOX_ID);
        }
    }

    /**
     * 设置制冷温度
     *
     * @param onTemp  制冷ON温度 范围4-25度
     * @param offTemp 制冷OFF温度 范围4-25度
     */
    public static int setColdTemp(int onTemp, int offTemp) throws RemoteException {
        synchronized (BVMManager.class) {
            if (null == aidlInterface) {
                return NULL_AIDL;
            }
            return aidlInterface.BVMSetColdTemp(BOX_ID, onTemp, offTemp);
        }
    }

    /**
     * 查询制冷温度
     *
     * @return int[0]: 制冷ON温度 int[1] 制冷OFF温度
     */
    public static int[] getColdTemp() throws RemoteException {
        synchronized (BVMManager.class) {
            if (null == aidlInterface) {
                return new int[]{NULL_AIDL};
            }
            return aidlInterface.BVMGetColdTemp(BOX_ID);
        }
    }

    /**
     * 制冷模式设置
     *
     * @param mode 0：弱冷模式，1：强冷模式
     */
    public static int setColdModel(int mode) throws RemoteException {
        synchronized (BVMManager.class) {
            if (null == aidlInterface) {
                return NULL_AIDL;
            }
            return aidlInterface.BVMSetColdModel(BOX_ID, mode);
        }
    }

    /**
     * 制冷制热模式设置
     *
     * @param mode 0:常温模式 1：制冷模式 2：制热模式
     */
    public static int setHeatColdModel(int mode) throws RemoteException {
        synchronized (BVMManager.class) {
            if (null == aidlInterface) {
                return NULL_AIDL;
            }
            return aidlInterface.BVMSetColdHeatModel(BOX_ID, mode);
        }
    }

    /**
     * 设备取货参数
     *
     * 不兜圈子了，我对你有好感，希望可以了解你；
     * 如果你觉得不合适，那我也就不打扰你了
     *
     * @param row    行
     * @param column 列
     * @param price  价格
     * @param number 订单号
     */
    public static String takeGoodsParam(int row, int column, int price, String number) {
        Map<String, Object> params = new HashMap<>();
        params.put("boxid", 1);
        params.put("positionX", row);
        params.put("positionY", column);
        params.put("elcspeed", 2);
        params.put("chspeed", 2);
        params.put("ordernumber", TextUtils.isEmpty(number) ? createRandom() : number);
        params.put("price", price);
        params.put("goodsnum", 1);

        return new Gson().toJson(params);
    }

    private static String createRandom() {
        StringBuilder builder = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 8; i++) {
            builder.append(random.nextInt(10));
        }
        return builder.toString();
    }

    private final static Map<Integer, String> ERROR_CODE = new HashMap<Integer, String>() {
        {
            put(-1, "aidl异常");
            put(99, "命令成功");
            put(-99, "不支持该功能（设备类型配置错误）");
            put(-98, "未找到识别设备");
            put(-97, "密钥错误");
            put(-96, "自适应设备失败");
            put(-95, "通讯失败");

            put(-1000, "命令失败");
            put(-1001, "打开端口失败");
            put(-1002, "发送命令失败");
            put(-1003, "接收命令失败");
            put(-1004, "参数错误-普通参数");
            put(-1005, "参数不能为空-指针");
            put(-1006, "判断帧头帧尾CMD及校验错误");
            put(-1007, "初始化Log失败");
            put(-1008, "通讯配置文件错误");
            put(-1009, "指令执行错误");
            put(-1010, "指令长度错误");
            put(-1011, "指令校验错误");
            put(-1012, "不支持指令");
            put(-1013, "指令参数错误");
            put(-1014, "其他错误");

            put(-1100, "进入维护模式失败/不在维护模式");
            put(-1101, "动作执行超时未完成");
            put(-1102, "货道自动识别失败");
            put(-1103, "货道自动识别成功，进入工作模式失败");
            put(-1104, "通信失败");
            put(-1105, "货道自动识别动作未执行");

            put(-1200, "停机故障");
            put(-1201, "整机忙");
            put(-1202, "货斗有货");
            put(-1203, "空货道");
            put(-1204, "坏货道");
            put(-1205, "已售空");
            put(-1206, "已售空");
            put(-1207, "准备中");
            put(-1208, "可选购");
            put(-1209, "选购中");
            put(-1210, "出货中");
            put(-1211, "货道禁用");
            put(-1212, "货道层列数错");
            put(-1213, "取货动作未执行");
            put(-1214, "获取出货报告超时失败");
            put(-1215, "通信失败");
            put(-1216, "取货失败，获取故障失败");
            put(-1217, "取货失败，其他故障");

            put(-1500, "停机故障");
            put(-1501, "警告故障");
            put(-1502, "一般故障");
            put(-1503, "其他故障");

        }
    };

    private final static Map<Integer, String> DEVICE_STATUS = new HashMap<Integer, String>() {
        {
            put(0, "启动完成，未自检");
            put(1, "启动自检过程中");
            put(2, "启动完成，等待售货");
            put(3, "售货中");
            put(4, "维护中");
            put(5, "BOOT中（升级过程中）");
            put(6, "停止中");
        }
    };

    public static boolean isSuccess(int code) {
        return code == 99 || code == 0;
    }

    public static String errorMsg(int code) {
        return String.format(Locale.CHINA, "%s(code=%d)", ERROR_CODE.get(code), code);
    }

    public static String stateMsg(int state) {
        return String.format(Locale.CHINA, "%s(code=%d)", DEVICE_STATUS.get(state), state);
    }
}
