/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openbravo.pos.ticket;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.DataRead;
import com.openbravo.data.loader.ImageUtils;
import com.openbravo.data.loader.SerializerRead;
import java.io.Serializable;

/**
 *
 * @author dmitriyroy
 */
public class IngredientInfo implements Serializable {
    
//    private static final long serialVersionUID = 7573758137839L;
    
    private String id;
    private String productId;
    private String ingredientId;
    private String ingredientName;
    private Double ingredientWeight;

    public IngredientInfo(String id, String productId, String ingredientId, String ingredientName, Double ingredientWeight) {
        this.id = id;
        this.productId = productId;
        this.ingredientId = ingredientId;
        this.ingredientName = ingredientName;
        this.ingredientWeight = ingredientWeight;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getIngredientId() {
        return ingredientId;
    }

    public void setIngredientId(String ingredientId) {
        this.ingredientId = ingredientId;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    public void setIngredientName(String ingredientName) {
        this.ingredientName = ingredientName;
    }

    public Double getIngredientWeight() {
        return ingredientWeight;
    }

    public void setIngredientWeight(Double ingredientWeight) {
        this.ingredientWeight = ingredientWeight;
    }
    
    public static SerializerRead getSerializerRead() {
        return new SerializerRead() {
            @Override
            public Object readValues(DataRead dr) throws BasicException {
                return new IngredientInfo(dr.getString(1),
                        dr.getString(2),
                        dr.getString(3),
                        dr.getString(4),
                        dr.getDouble(5));
            }
        };
    }

    @Override
    public String toString() {
        return "IngredientInfo{" + "id=" + id + ", productId=" + productId + ", ingredientId=" + ingredientId + ", ingredientName=" + ingredientName + ", ingredientWeight=" + ingredientWeight + '}';
    }

}
