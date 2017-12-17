/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openbravo.pos.inventory;
import com.openbravo.basic.BasicException;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.ticket.IngredientInfo;
import com.openbravo.pos.ticket.ProductMini;
import static com.openbravo.pos.util.T2FileLogger.writeLog;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import static java.lang.Double.valueOf;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;



/**
 *
 * @author AVM
 */
public class ComplexProductsEditor extends javax.swing.JDialog {

    /**
     * Creates new form ComplexProductsEditor
     */
    private final int FRAME_WIDTH = 500;
    private final int FRAME_HEIGHT = 380;
    final String[] list1ModelData;
    List<IngredientInfo> ingredients;
    List<ProductMini> productMiniList;
    DataLogicSales m_dSales;
    String productId;
    
    public ComplexProductsEditor(final DataLogicSales m_dSales, Object id) {
        this.m_dSales = m_dSales;
        this.productId = (String) id;
        initComponents();
        setModalityType(ModalityType.APPLICATION_MODAL);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int locationX = (screenSize.width - FRAME_WIDTH) / 2;
        int locationY = (screenSize.height - FRAME_HEIGHT) / 2;
        setBounds(new Rectangle(locationX, locationY, FRAME_WIDTH, FRAME_HEIGHT));        
        //setDefaultCloseOperation(HIDE_ON_CLOSE);
        setSize(FRAME_WIDTH,FRAME_HEIGHT);
        setTitle(AppLocal.getIntString("title.editRecept"));
        setResizable(false);
        
        ingredients = new ArrayList<>();
        try {
            ingredients = m_dSales.getIngredients((String)id);
        } catch (BasicException ex) {
            Logger.getLogger(ComplexProductsEditor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        jTable_ProductList.setModel(new DefaultTableModel(new String [] {
                    "ID",
                    AppLocal.getIntString("column.product"), 
                    AppLocal.getIntString("column.coefficient")
                }, ingredients.size()));
                //}, 1));
        jTable_ProductList.getColumnModel().getColumn(0).setResizable(false);
        jTable_ProductList.getColumnModel().getColumn(0).setPreferredWidth(0);
        jTable_ProductList.getColumnModel().getColumn(0).setMinWidth(0);
        jTable_ProductList.getColumnModel().getColumn(0).setMaxWidth(0);
        jTable_ProductList.getColumnModel().getColumn(1).setResizable(false);
        jTable_ProductList.getColumnModel().getColumn(2).setResizable(false);
        int row = 0;
        for (IngredientInfo ingredientInfo : ingredients) {
            jTable_ProductList.setValueAt(ingredientInfo.getIngredientId(), row, 0);
            jTable_ProductList.setValueAt(ingredientInfo.getIngredientName(), row, 1);
            jTable_ProductList.setValueAt(ingredientInfo.getIngredientWeight(), row, 2);
            row++;
        }
        
        jTable_ProductList.getModel().addTableModelListener(new TableModelListener(){
            @Override
            public void tableChanged(TableModelEvent e) {
                jTable_ProductListActionPerformed(e);
            }
        });
        
        productMiniList = new ArrayList<>();
        
        try {
            productMiniList = m_dSales.getAllProductNameNonComplex();
        } catch (BasicException ex) {
            Logger.getLogger(ComplexProductsEditor.class.getName()).log(Level.SEVERE, null, ex);
        }

        list1ModelData = new String[productMiniList.size()];
        for (int i = 0; i < productMiniList.size(); i++) {
            list1ModelData[i] = productMiniList.get(i).getIngredientName();
        }
        
        jList_Ingredient.setModel(new javax.swing.AbstractListModel<String>() {
            @Override
            public int getSize() { return list1ModelData.length; }
            @Override
            public String getElementAt(int i) { return list1ModelData[i]; }
        });
    }
       
    private void jTable_ProductListActionPerformed(TableModelEvent e) {
        int selectedRow = jTable_ProductList.getSelectedRow();
        String ingredientId = (String) jTable_ProductList.getValueAt(selectedRow, 0);
        String ingredientWeight = (String) jTable_ProductList.getValueAt(jTable_ProductList.getSelectedRow(), 2);
        Double weight = stringToDouble(ingredientWeight);
        try {
            int countUpdatedRows = m_dSales.updateIngredientIntoRecipe(
                    this.productId,
                    ingredientId,
                    weight);
        } catch (BasicException ex) {
            Logger.getLogger(ComplexProductsEditor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
   
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        jList_Ingredient = new javax.swing.JList<>();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTable_ProductList = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();
        jButtonDelete = new javax.swing.JButton();
        jButtonAdd = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jList_Ingredient.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = {
                //"Продукт1", "Продукт2", "Продукт3", "Продукт4", "Продукт5"
            };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        jScrollPane2.setViewportView(jList_Ingredient);

        jLabel1.setText(AppLocal.getIntString("editRecept.toRecept"));

        jLabel2.setText(AppLocal.getIntString("editRecept.available"));

        jTable_ProductList.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                //        {"Кофе", "0,25"},
                //        {"Сахар", "0,02"},
                //        {"Молоко", "0,03"},
                //        {"Молоко1", "0,03"},
                //        {"Молоко2", "0,03"},
                //        {"Молоко3", "0,03"},
                //        {"Молоко4", "0,03"},
                //        {"Молоко5", "0,03"},
                //        {"Молоко6", "0,03"},
                //        {"Молоко7", "0,03"},
                //        {"Молоко8", "0,03"}
            },
            /*new String [] {
                "Продукт", "Коэфициент"
            }*/
            new String [] {
                AppLocal.getIntString("column.product"),
                AppLocal.getIntString("column.coefficient")
            }
        ));
        jTable_ProductList.setEnabled(true);
        jScrollPane3.setViewportView(jTable_ProductList);

        jButton1.setText(AppLocal.getIntString("button.doneEdit"));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButtonDelete.setBackground(new java.awt.Color(255, 204, 204));
        jButtonDelete.setText(">>>");
        jButtonDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeleteActionPerformed(evt);
            }
        });

        jButtonAdd.setBackground(new java.awt.Color(204, 255, 204));
        jButtonAdd.setText("<<<");
        jButtonAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 193, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jButtonDelete, javax.swing.GroupLayout.DEFAULT_SIZE, 81, Short.MAX_VALUE)
                                    .addComponent(jButtonAdd, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButton1)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(15, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 262, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(43, 43, 43)
                        .addComponent(jButtonAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jButtonDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        dispose();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButtonDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeleteActionPerformed
        
        try {
            int rowId = jTable_ProductList.getSelectedRow();
            String ingredientId = (String) jTable_ProductList.getModel().getValueAt(rowId, 0);
            //TODO @DimaRoy remove record from base by idProduct - DONE
            int countDeletedRow = m_dSales.deleteIngredientFromRecipe(this.productId,ingredientId);
            ((DefaultTableModel)jTable_ProductList.getModel()).removeRow(rowId);
            jTable_ProductList.revalidate();
        } catch (BasicException ex) {
            Logger.getLogger(ComplexProductsEditor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }//GEN-LAST:event_jButtonDeleteActionPerformed

    private void jButtonAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddActionPerformed
        try {
            // TODO add your handling code here:
            int index = jList_Ingredient.getSelectedIndex();
            String ingredientId = productMiniList.get(index).getId();
            String ingredientName = jList_Ingredient.getSelectedValue();
            //TODO Add to base recep - DONE
            DefaultTableModel model = (DefaultTableModel) jTable_ProductList.getModel();
            if (!existsInTable(ingredientId)) {
                m_dSales.addIngredientIntoRecipe(this.productId,ingredientId ,0.0);
                jTable_ProductList.revalidate();
                model.addRow(new Object[]{ingredientId,ingredientName ,"0.0"});
//                jTable_ProductList.revalidate();
                jTable_ProductList.repaint();
                
            }
        } catch (BasicException ex) {
            Logger.getLogger(ComplexProductsEditor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButtonAddActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButtonAdd;
    private javax.swing.JButton jButtonDelete;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JList<String> jList_Ingredient;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTable jTable_ProductList;
    // End of variables declaration//GEN-END:variables

    private boolean existsInTable(String ingredientId) {
        for(int i = 0; i < jTable_ProductList.getRowCount(); i++) {
            String productIdInList = (String) jTable_ProductList.getValueAt(i, 0);
            if (ingredientId.trim().equals(productIdInList)) {
                return true;
            }
        }
        return false;
    }
    
    private double stringToDouble(String doubleString){
        double cakeWeight;
        String tmpStr = "";
        int pointCount = 0;
        for (int i=0 ; i < doubleString.length(); i++){
            if ( doubleString.charAt(i) == ',' || doubleString.charAt(i) == '.'){
                if ( pointCount == 0){
                    tmpStr += '.'; 
                    pointCount = 1;                    
                } 
            } else if (doubleString.charAt(i) == '1' || doubleString.charAt(i) == '2' 
                    || doubleString.charAt(i) == '3' || doubleString.charAt(i) == '4' 
                    || doubleString.charAt(i) == '5' || doubleString.charAt(i) == '6'
                    || doubleString.charAt(i) == '7' || doubleString.charAt(i) == '8' 
                    || doubleString.charAt(i) == '9' || doubleString.charAt(i) == '0'){                 
                tmpStr += doubleString.charAt(i);
            }
        }
        cakeWeight = valueOf(tmpStr);
        return cakeWeight;
    }    
    
}
