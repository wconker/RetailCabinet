package com.sovell.retail_cabinet.bean;

import java.util.List;

/**
 * 作者: 胡龙 on 2019/3/8.
 */

public class CardInfoBean {

    /**
     * code : 1
     * msg : success
     * card : {"id":"191013515","no":"191013515","level":{"id":1,"name":"金卡"},"balance":"214.00","balance_usable":"209.00","credited":"0.00","freeze":"5.00","state":1,"expire_date":"2101-06-23 10:58:00","locked":0,"locked_msg":"","group":{"name":"临时"},"total_consum":"5000","total_charge":"22600"}
     * accounts : [{"i":0,"id":"191013515","no":"191013515","name":"主账户","balance":"214.00","freeze":"5.00","balance_usable":"209.00","credited":"0.00","locked":0,"locked_msg":"","total_consum":"50.00","total_charge":"226.00"},{"i":1,"id":"191013515.1","no":"191013515.1","name":"补贴账户","balance":"182.00","freeze":"0.00","balance_usable":"182.00","credited":"0.00","locked":0,"locked_msg":"","total_consum":"20.00","total_charge":"200.00"}]
     * profile : {"mob":"13819171490","name":"刘冲","gender":1,"birthday":"1900-01-01","tel":"13819171490","addr":"滨江路1181号","city":"杭州市","area":"滨江区","road":"","id":8663,"id_no":"123123","district":"","remark":""}
     */

    private int code;
    private int sub_code;
    private String msg;
    private CardBean card;
    private ProfileBean profile;
    private List<AccountsBean> accounts;

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

    public CardBean getCard() {
        return card;
    }

    public void setCard(CardBean card) {
        this.card = card;
    }

    public ProfileBean getProfile() {
        return profile;
    }

    public void setProfile(ProfileBean profile) {
        this.profile = profile;
    }

    public List<AccountsBean> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<AccountsBean> accounts) {
        this.accounts = accounts;
    }

    public static class CardBean {
        /**
         * id : 191013515
         * no : 191013515
         * level : {"id":1,"name":"金卡"}
         * balance : 214.00
         * balance_usable : 209.00
         * credited : 0.00
         * freeze : 5.00
         * state : 1
         * expire_date : 2101-06-23 10:58:00
         * locked : 0
         * locked_msg :
         * group : {"name":"临时"}
         * total_consum : 5000
         * total_charge : 22600
         */

        private String id;
        private String no;
        private LevelBean level;
        private String balance;
        private String balance_usable;
        private String credited;
        private String freeze;
        private int state;
        private String expire_date;
        private int locked;
        private String locked_msg;
        private GroupBean group;
        private String total_consum;
        private String total_charge;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getNo() {
            return no;
        }

        public void setNo(String no) {
            this.no = no;
        }

        public LevelBean getLevel() {
            return level;
        }

        public void setLevel(LevelBean level) {
            this.level = level;
        }

        public String getBalance() {
            return balance;
        }

        public void setBalance(String balance) {
            this.balance = balance;
        }

        public String getBalance_usable() {
            return balance_usable;
        }

        public void setBalance_usable(String balance_usable) {
            this.balance_usable = balance_usable;
        }

        public String getCredited() {
            return credited;
        }

        public void setCredited(String credited) {
            this.credited = credited;
        }

        public String getFreeze() {
            return freeze;
        }

        public void setFreeze(String freeze) {
            this.freeze = freeze;
        }

        public int getState() {
            return state;
        }

        public void setState(int state) {
            this.state = state;
        }

        public String getExpire_date() {
            return expire_date;
        }

        public void setExpire_date(String expire_date) {
            this.expire_date = expire_date;
        }

        public int getLocked() {
            return locked;
        }

        public void setLocked(int locked) {
            this.locked = locked;
        }

        public String getLocked_msg() {
            return locked_msg;
        }

        public void setLocked_msg(String locked_msg) {
            this.locked_msg = locked_msg;
        }

        public GroupBean getGroup() {
            return group;
        }

        public void setGroup(GroupBean group) {
            this.group = group;
        }

        public String getTotal_consum() {
            return total_consum;
        }

        public void setTotal_consum(String total_consum) {
            this.total_consum = total_consum;
        }

        public String getTotal_charge() {
            return total_charge;
        }

        public void setTotal_charge(String total_charge) {
            this.total_charge = total_charge;
        }

        public static class LevelBean {
            /**
             * id : 1
             * name : 金卡
             */

            private int id;
            private String name;

            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }
        }

        public static class GroupBean {
            /**
             * name : 临时
             */

            private String name;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }
        }
    }

    public static class ProfileBean {
        /**
         * mob : 13819171490
         * name : 刘冲
         * gender : 1
         * birthday : 1900-01-01
         * tel : 13819171490
         * addr : 滨江路1181号
         * city : 杭州市
         * area : 滨江区
         * road :
         * id : 8663
         * id_no : 123123
         * district :
         * remark :
         */

        private String mob;
        private String name;
        private int gender;
        private String birthday;
        private String tel;
        private String addr;
        private String city;
        private String area;
        private String road;
        private int id;
        private String id_no;
        private String district;
        private String remark;

        public String getMob() {
            return mob;
        }

        public void setMob(String mob) {
            this.mob = mob;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getGender() {
            return gender;
        }

        public void setGender(int gender) {
            this.gender = gender;
        }

        public String getBirthday() {
            return birthday;
        }

        public void setBirthday(String birthday) {
            this.birthday = birthday;
        }

        public String getTel() {
            return tel;
        }

        public void setTel(String tel) {
            this.tel = tel;
        }

        public String getAddr() {
            return addr;
        }

        public void setAddr(String addr) {
            this.addr = addr;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getArea() {
            return area;
        }

        public void setArea(String area) {
            this.area = area;
        }

        public String getRoad() {
            return road;
        }

        public void setRoad(String road) {
            this.road = road;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getId_no() {
            return id_no;
        }

        public void setId_no(String id_no) {
            this.id_no = id_no;
        }

        public String getDistrict() {
            return district;
        }

        public void setDistrict(String district) {
            this.district = district;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }
    }

    public static class AccountsBean {
        /**
         * i : 0
         * id : 191013515
         * no : 191013515
         * name : 主账户
         * balance : 214.00
         * freeze : 5.00
         * balance_usable : 209.00
         * credited : 0.00
         * locked : 0
         * locked_msg :
         * total_consum : 50.00
         * total_charge : 226.00
         */

        private int i;
        private String id;
        private String no;
        private String name;
        private String balance;
        private String freeze;
        private String balance_usable;
        private String credited;
        private int locked;
        private String locked_msg;
        private String total_consum;
        private String total_charge;

        public int getI() {
            return i;
        }

        public void setI(int i) {
            this.i = i;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getNo() {
            return no;
        }

        public void setNo(String no) {
            this.no = no;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getBalance() {
            return balance;
        }

        public void setBalance(String balance) {
            this.balance = balance;
        }

        public String getFreeze() {
            return freeze;
        }

        public void setFreeze(String freeze) {
            this.freeze = freeze;
        }

        public String getBalance_usable() {
            return balance_usable;
        }

        public void setBalance_usable(String balance_usable) {
            this.balance_usable = balance_usable;
        }

        public String getCredited() {
            return credited;
        }

        public void setCredited(String credited) {
            this.credited = credited;
        }

        public int getLocked() {
            return locked;
        }

        public void setLocked(int locked) {
            this.locked = locked;
        }

        public String getLocked_msg() {
            return locked_msg;
        }

        public void setLocked_msg(String locked_msg) {
            this.locked_msg = locked_msg;
        }

        public String getTotal_consum() {
            return total_consum;
        }

        public void setTotal_consum(String total_consum) {
            this.total_consum = total_consum;
        }

        public String getTotal_charge() {
            return total_charge;
        }

        public void setTotal_charge(String total_charge) {
            this.total_charge = total_charge;
        }
    }
}
