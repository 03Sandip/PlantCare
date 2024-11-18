package com.example.plantcare.Domain;

public class CategoryDomain {
    private String imgPath;
   private String Title;

    public CategoryDomain(String imgPath, String title){
        this.imgPath= imgPath;
        this.Title=title;
    }

    public String getImgPath(){
        return imgPath;
    }

    public void setImgPath(String imgPath){
        this.imgPath = imgPath;
    }

    public String getTitle(){
        return Title;
    }

    public void setTitle(String Title){
        this.Title = Title;
    }

}







