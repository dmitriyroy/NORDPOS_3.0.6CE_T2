package com.openbravo.pos.ticket;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.DataRead;
import com.openbravo.data.loader.SerializerRead;

/**
 *
 * @author dmitriyroy
 */
public class ProductMini {
    
//    private static final long serialVersionUID = 7587696854696L;
    
    private String id;
    private String ingredientName;
    private Boolean isComlex;

    public ProductMini(String id, String ingredientName, Boolean isComlex) {
        this.id = id;
        this.ingredientName = ingredientName;
        this.isComlex = isComlex;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    public void setIngredientName(String ingredientName) {
        this.ingredientName = ingredientName;
    }

    public Boolean getIsComlex() {
        return isComlex;
    }

    public void setIsComlex(Boolean isComlex) {
        this.isComlex = isComlex;
    }

    
    public static SerializerRead getSerializerRead() {
        return new SerializerRead() {
            @Override
            public Object readValues(DataRead dr) throws BasicException {
                return new ProductMini(
                        dr.getString(1),
                        dr.getString(2),
                        dr.getBoolean(3));
            }
        };
    }

    @Override
    public String toString() {
        return "ProductMini{" + "id=" + id + ", ingredientName=" + ingredientName + ", isComlex=" + isComlex + '}';
    }
    
}
