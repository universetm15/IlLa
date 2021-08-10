package com.example.calendardiary;


public class ItemData {
    private String itemTxtDate;
    private String itemTxtList;

    public ItemData(String itemTxtDate, String itemTxtList) {
        this.itemTxtDate=itemTxtDate;
        this.itemTxtList=itemTxtList;
    }

    public String getItemTxtDate() {
        return itemTxtDate;
    }

    public String getItemTxtList() {
        return itemTxtList;
    }


    public void setItemTxtDate(String itemTxtDate) {
        this.itemTxtDate = itemTxtDate;
    }

    public void setItemTxtList(String itemTxtList) {
        this.itemTxtList = itemTxtList;
    }


}
