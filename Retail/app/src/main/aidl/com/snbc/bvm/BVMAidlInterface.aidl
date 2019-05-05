// BVMAidlInterface.aidl
package com.snbc.bvm;

// Declare any non-default types here with import statements

interface BVMAidlInterface {
     //获取整机状态
     int BVMGetRunningState(int boxid);
     //获取门状态
     int[] BVMGetDoorState(int boxid);
     //获取故障码
     String[] BVMGetFGFault(int boxid);
     //故障确认
     int BVMCleanSysFault(int boxid);
     //货道扫描
     int BVMInitXYRoad(int boxID, int xyselect, int workmode, int timeout);
     //查询扫描结果
     int[] BVMQueryInitResult(int boxID);
     //货道扫描完整流程
     int[] BVMInitXYRoadProc(int boxid);
     //货道状态取得
     int BVMQueryGoodsState(int boxID,int xSelect,int ySelect);
     //备货
     String BVMMoveSaleGoodsPro(String injson);
     //备货后取货
     String BVMCtrlSaleGoodsStepPro(String injson);
     //出货
     String BVMStartShip(String injson);
     //一次出多个货物
     String BVMStartShipEx1(String injson);
     //查询最后一次出货的出货报告
     String BVMQueryLastSaleReport(String injson);
     //客户未拿走货物时的再次开门
     int BVMReSaleGoods(int boxid);
     //进入/退出维护模式
     int BVMSetWorkMode(int boxid,int state);
     //读取资产编码
     String BVMReadAssetCode(int boxid);
     //烧录客户资产编码
     int BVMWriteGodAssetCode(int boxid,String code);
     //读取客户资产编码
     String BVMReadGodAssetCOde(int boxid);
     //设置灯光状态
     int BVMSetLightState(int boxID,int type ,int state);
     //电控锁
     int BVMElecDoorCtrl(int boxID);
     //判断灯的开关
     boolean[] BVMQueryLightState(int boxid);
     //升级固件
     int BVMUpdateHardWare(int boxID,String filepath);
     //查询供应商代码，固件版本号等信息
     String  BVMQueryBoxInfo(int boxID,int type);
     //开始扫描
     String BVMOpenScanDev();
     //关闭设备扫描
     int BVMCloseScanDev();
     //货道禁用/启用
     int BVMSetCtrlFGLayRow(int boxId, int nLay, int nRow, int state);
     //货道禁用/启用查询
     int[] BVMSGetFGLayRowState(int boxId, int nLay, int nRow);
     //是否支持激光测距
     int BVMGetLaserPermission(int boxid);
     //设置密钥
     int BVMSetKey(String key);
     //制冷制热模式设置
     int BVMSetColdHeatModel(int boxid,int mode);
     //制冷制热模式查询
     int BVMGetColdHeatModel(int boxid);
     //制冷模式设置
     int BVMSetColdModel(int boxid,int mode);
     //制冷模式查询
     int BVMGetColdMode(int boxid);
     //制冷温度设置
     int BVMSetColdTemp(int boxid,int onTemp,int offTemp);
     //制冷温度查询
     int[] BVMGetColdTemp(int boxid);
     //制热温度设置
     int BVMSetHeatTemp(int boxid,int onTemp,int offTemp);
     //制热温度查询
     int[] BVMGetHeatTemp(int boxid);
     //当前温度查询
     int[] BVMGetColdHeatTemp(int boxid);
     //身份证读卡器相关接口 对SAM_A复位
     int BVMResetSAM();
     //对SAM_A进行状态监测
     int BVMGetSAMStatus();
     //读取SAM_A的编号，输出为十六进制
     int BVMGetSAMID(inout byte[] SamID);
     //读取SAM_A的编号
     int BVMGetSAMIDToStr (inout char[] SamID);
     //寻找居民身份证
     int BVMStartFindIDCard();
     //选取居民身份证
     int BVMSelectIDCard();
     //读取居民身份证信息（文字、照片）
     int BVMReadBaseMsg(inout byte[] pucCHMsg,inout int[] pucCHMsgLen,inout byte[] pucPHMsg,inout int[] pucPHMsgLen);
     //读取身份证信息（文字、照片、指纹）
     int BVMReadBaseFPMsg(inout byte[] pucCHMsg,inout int[] pucCHMsgLen,inout byte[] pucPHMsg,inout int[] pucPHMsgLen,inout byte[] pucFPMsg,inout int[] pucFPMsgLen);
     //初始化打印机
     int BVMInitPrinter (String port,int baudRate);
     //关闭打印机
     int BVMClosePrinter();
     //打印机执行命令
     int BVMPrintExec(String cmdStr);
     //获取打印机的数据
     byte[] BVMGetPrinterData();
     //升级中间层
     int BVMInstallApk(String apkPath, boolean silent, String pkgName, String clsName);

}
