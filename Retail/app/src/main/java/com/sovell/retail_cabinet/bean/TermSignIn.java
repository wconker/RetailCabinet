package com.sovell.retail_cabinet.bean;

public class TermSignIn {


    /**
     * code : 1
     * sub_code : 0
     * msg : success
     * process_time : 0.156
     * term : 114
     * name : 1232
     * shop : 100
     * shop_no : 1001
     * shop_name : 新浪1号餐厅
     * stime : 2019-02-25 15:05:27
     * interval : 10
     * authkey : KVZNV27ZHCJYC8NPH52F
     * type : 41
     * temp_json : {"coldmax":"15","coldmin":"8"}
     * oper_json : {"name":"lx","mob":"18767122542"}
     * code_all : 1000
     */

    private int code;
    private int sub_code;
    private String msg;
    private double process_time;
    private String term;
    private String name;
    private String shop;
    private String shop_no;
    private String shop_name;
    private String stime;
    private int interval;
    private String authkey;
    private int type;
    private TempJsonBean temp_json;
    private OperJsonBean oper_json;
    private int code_all;
    //pass支付需要的参数
    private Properties properties;


    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }


    //取餐放餐对象
    private TimeJsonBean time_json;

    public TimeJsonBean getTime_json() {
        return time_json;
    }

    public void setTime_json(TimeJsonBean time_json) {
        this.time_json = time_json;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getSub_code() {
        return sub_code;
    }

    public void setSub_code(int sub_code) {
        this.sub_code = sub_code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public double getProcess_time() {
        return process_time;
    }

    public void setProcess_time(double process_time) {
        this.process_time = process_time;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShop() {
        return shop;
    }

    public void setShop(String shop) {
        this.shop = shop;
    }

    public String getShop_no() {
        return shop_no;
    }

    public void setShop_no(String shop_no) {
        this.shop_no = shop_no;
    }

    public String getShop_name() {
        return shop_name;
    }

    public void setShop_name(String shop_name) {
        this.shop_name = shop_name;
    }

    public String getStime() {
        return stime;
    }

    public void setStime(String stime) {
        this.stime = stime;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public String getAuthkey() {
        return authkey;
    }

    public void setAuthkey(String authkey) {
        this.authkey = authkey;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public TempJsonBean getTemp_json() {
        return temp_json;
    }

    public void setTemp_json(TempJsonBean temp_json) {
        this.temp_json = temp_json;
    }

    public OperJsonBean getOper_json() {
        return oper_json;
    }

    public void setOper_json(OperJsonBean oper_json) {
        this.oper_json = oper_json;
    }

    public int getCode_all() {
        return code_all;
    }

    public void setCode_all(int code_all) {
        this.code_all = code_all;
    }

    public static class TempJsonBean {

        /**
         * coldmax : 15
         * coldmin : 8
         * state:0
         */

        //温度状态 0关闭,1开启
        private int state;
        private int cold_max;
        private int cold_min;

        public int getState() {
            return state;
        }

        public void setState(int state) {
            this.state = state;
        }

        public int getColdmax() {
            return cold_max;
        }

        public void setColdmax(int coldmax) {
            this.cold_max = coldmax;
        }

        public int getColdmin() {
            return cold_min;
        }

        public void setColdmin(int coldmin) {
            this.cold_min = coldmin;
        }
    }

    public static class OperJsonBean {
        /**
         * name : lx
         * mob : 18767122542
         */

        private String name;
        private String mob;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getMob() {
            return mob;
        }

        public void setMob(String mob) {
            this.mob = mob;
        }
    }

    public static class TimeJsonBean {
        private String book_time_st;//预订开始时间
        private String book_time_et;//预订截止时间
        private String take_time_st;//取餐开始时间
        private String take_time_et;//取餐截止时间
        private int book_state = 1;//取餐状态

        public int getBookstate() {
            return book_state;
        }

        public void setBookstate(int bookstate) {
            this.book_state = bookstate;
        }


        public String getBook_time_st() {
            return book_time_st;
        }

        public void setBook_time_st(String book_time_st) {
            this.book_time_st = book_time_st;
        }

        public String getBook_time_et() {
            return book_time_et;
        }

        public void setBook_time_et(String book_time_et) {
            this.book_time_et = book_time_et;
        }

        public String getTake_time_st() {
            return take_time_st;
        }

        public void setTake_time_st(String take_time_st) {
            this.take_time_st = take_time_st;
        }

        public String getTake_time_et() {
            return take_time_et;
        }

        public void setTake_time_et(String take_time_et) {
            this.take_time_et = take_time_et;
        }


    }
    public static class Properties {
        public String getQrpay() {
            return qrpay;
        }

        public void setQrpay(String qrpay) {
            this.qrpay = qrpay;
        }

        private String qrpay;
    }
}
