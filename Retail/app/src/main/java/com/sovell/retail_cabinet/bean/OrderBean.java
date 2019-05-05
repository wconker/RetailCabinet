package com.sovell.retail_cabinet.bean;

import java.util.List;

public class OrderBean {


    /**
     * code : 1
     * sub_code : 0
     * msg : success
     * process_time : 0.28
     * list : [{"seq":636825358854084500,"take_date":"2019-04-09 10:45:12","state":1,"qty":2,"prods":[{"detailid":1,"prodid":"2","prodno":"1001","prodname":"可口可乐","state":1,"take_date":"2000-01-01 00:00:00"},{"prodid":"2","prodno":"1001","prodname":"阿萨姆奶茶","state":2,"take_date":"2019-04-09 18:49"}]},{"seq":636825358854094500,"take_date":"2019-04-09 10:45:12","state":2,"qty":1,"prods":[{"prodid":"2","prodno":"1001","prodname":"阿萨姆奶茶","state":2,"take_date":"2019-04-09 18:49"}]}]
     * code_all : 1000
     */

    private int code;
    private int sub_code;
    private String msg;
    private double process_time;
    private int code_all;
    private List<ListBean> list;

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

    public int getCode_all() {
        return code_all;
    }

    public void setCode_all(int code_all) {
        this.code_all = code_all;
    }

    public List<ListBean> getList() {
        return list;
    }

    public void setList(List<ListBean> list) {
        this.list = list;
    }

    public static class ListBean {
        /**
         * seq : 636825358854084500
         * take_date : 2019-04-09 10:45:12
         * state : 1
         * qty : 2
         * prods : [{"detailid":1,"prodid":"2","prodno":"1001","prodname":"可口可乐","state":1,"take_date":"2000-01-01 00:00:00"},{"prodid":"2","prodno":"1001","prodname":"阿萨姆奶茶","state":2,"take_date":"2019-04-09 18:49"}]
         */

        private long seq;
        private String create_date;
        private int state;
        private int qty;


        private int amt;
        private List<ProdsBean> prods;

        public long getSeq() {
            return seq;
        }
        public int getAmt() {
            return amt;
        }

        public void setAmt(int amt) {
            this.amt = amt;
        }

        public void setSeq(long seq) {
            this.seq = seq;
        }

        public String getTake_date() {
            return create_date;
        }

        public void setTake_date(String take_date) {
            this.create_date = take_date;
        }

        public int getState() {
            return state;
        }

        public void setState(int state) {
            this.state = state;
        }

        public int getQty() {
            return qty;
        }

        public void setQty(int qty) {
            this.qty = qty;
        }

        public List<ProdsBean> getProds() {
            return prods;
        }

        public void setProds(List<ProdsBean> prods) {
            this.prods = prods;
        }

        public static class ProdsBean {
            /**
             * detailid : 1
             * prodid : 2
             * prodno : 1001
             * prodname : 可口可乐
             * state : 1
             * take_date : 2000-01-01 00:00:00
             */

            private int detailid;
            private String prodid;
            private String prodno;
            private String prodname;
            private int state;
            private String take_date;

            public int getDetailid() {
                return detailid;
            }

            public void setDetailid(int detailid) {
                this.detailid = detailid;
            }

            public String getProdid() {
                return prodid;
            }

            public void setProdid(String prodid) {
                this.prodid = prodid;
            }

            public String getProdno() {
                return prodno;
            }

            public void setProdno(String prodno) {
                this.prodno = prodno;
            }

            public String getProdname() {
                return prodname;
            }

            public void setProdname(String prodname) {
                this.prodname = prodname;
            }

            public int getState() {
                return state;
            }

            public void setState(int state) {
                this.state = state;
            }

            public String getTake_date() {
                return take_date;
            }

            public void setTake_date(String take_date) {
                this.take_date = take_date;
            }
        }
    }
}
