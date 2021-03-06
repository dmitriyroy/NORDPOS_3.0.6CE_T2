//    Openbravo POS is a point of sales application designed for touch screens.
//    Copyright (C) 2007-2009 Openbravo, S.L.
//    http://www.openbravo.com/product/pos
//
//    This file is part of Openbravo POS.
//
//    Openbravo POS is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    Openbravo POS is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with Openbravo POS.  If not, see <http://www.gnu.org/licenses/>.
package com.openbravo.pos.inventory;

import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.ComboBoxValModel;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.data.loader.SentenceFind;
import com.openbravo.data.loader.SentenceList;
import com.openbravo.data.user.DirtyManager;
import com.openbravo.data.user.EditorRecord;
import com.openbravo.format.Formats;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.DataLogicSales;
import com.nordpos.device.ticket.TicketParser;
import com.nordpos.device.ticket.TicketPrinterException;
import com.openbravo.pos.sales.TaxesLogic;
import com.openbravo.pos.scripting.ScriptEngine;
import com.openbravo.pos.scripting.ScriptException;
import com.openbravo.pos.scripting.ScriptFactory;
import com.openbravo.pos.ticket.CategoryInfo;
import com.openbravo.pos.ticket.IngredientInfo;
import com.openbravo.pos.ticket.ProductInfoEdit;
import com.openbravo.pos.ticket.ProductInfoExt;
import static com.openbravo.pos.util.T2FileLogger.writeLog;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.krysalis.barcode4j.impl.upcean.*;

/**
 *
 * @author adrianromero
 * @author Andrey Svininykh <svininykh@gmail.com>
 * @version NORD POS 3
 */
public class ProductsEditor extends JPanel implements EditorRecord {

    private static final String PRINTER_SHEMA = "/com/nordpos/templates/Schema.Printer.xsd";
    private static final String PRINT_PRODUCT_LABEL = "/com/nordpos/templates/Printer.ProductLabel.xml";

    private String s_GenRef;
    private String s_GenBarcode;
    private String s_DefBarcode;
    private String s_DefTaxCat;
    private String s_DefProdCat;

    private final SentenceList product;

    private final SentenceList m_sentcat;
    private ComboBoxValModel m_CategoryModel;

    private final SentenceList taxcatsent;
    private ComboBoxValModel taxcatmodel;

    private final SentenceList attsent;
    private ComboBoxValModel attmodel;

    private final SentenceList taxsent;
    private TaxesLogic taxeslogic;

    private final SentenceFind loadimage;

    private final ComboBoxValModel m_CodetypeModel;

    private Object m_id;
    private Object pricesell;
    private boolean priceselllock = false;

    private boolean reportlock = false;

    private final DataLogicSales m_dSales;
    private TicketParser m_TTP;

    private ProductInfoEdit m_oCurrentProductEdit;

    private final AppView m_App;

    public ProductsEditor(AppView app, DataLogicSales dlSales, DirtyManager dirty) {
        initComponents();

        jTable_ComplexData = new MJTable();
        jTable_ComplexData.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {}, new String [] {}
        ));
//        jTable_ComplexData.setEnabled(false);
        jScrollPane3.setViewportView(jTable_ComplexData);

        m_App = app;
        m_dSales = dlSales;

        loadimage = dlSales.getProductImage();

        product = dlSales.getProductList();

        // The taxes sentence
        taxsent = dlSales.getTaxList();

        // The categories model
        m_sentcat = dlSales.getCategoriesList();
        m_CategoryModel = new ComboBoxValModel();

        // The taxes model
        taxcatsent = dlSales.getTaxCategoriesList();
        taxcatmodel = new ComboBoxValModel();

        // The attributes model
        attsent = dlSales.getAttributeSetList();
        attmodel = new ComboBoxValModel();

        m_CodetypeModel = new ComboBoxValModel();
        m_CodetypeModel.add(null);
        m_CodetypeModel.add(CodeType.EAN13);
        m_CodetypeModel.add(CodeType.CODE128);
        m_jCodetype.setModel(m_CodetypeModel);
        m_jCodetype.setVisible(false);

        m_jRef.getDocument().addDocumentListener(dirty);
        m_jCode.getDocument().addDocumentListener(dirty);
        m_jName.getDocument().addDocumentListener(dirty);
        m_jComment.addActionListener(dirty);
        m_jScale.addActionListener(dirty);
        m_jCategory.addActionListener(dirty);
        m_jTax.addActionListener(dirty);
        m_jAtt.addActionListener(dirty);
        m_jPriceBuy.getDocument().addDocumentListener(dirty);
        m_jPriceSell.getDocument().addDocumentListener(dirty);
        m_jImage.addPropertyChangeListener("image", dirty);
        m_jstockcost.getDocument().addDocumentListener(dirty);
        m_jstockvolume.getDocument().addDocumentListener(dirty);
        m_jInCatalog.addActionListener(dirty);
        m_jCatalogOrder.getDocument().addDocumentListener(dirty);
        txtAttributes.getDocument().addDocumentListener(dirty);
        jCheckBox_ComplexProduct.addActionListener(dirty);

        FieldsManager fm = new FieldsManager();
        m_jPriceBuy.getDocument().addDocumentListener(fm);
        m_jPriceSell.getDocument().addDocumentListener(new PriceSellManager());
        m_jTax.addActionListener(fm);

        m_jPriceSellTax.getDocument().addDocumentListener(new PriceTaxManager());
        m_jmargin.getDocument().addDocumentListener(new MarginManager());

        txtAttributes.setAntiAliasingEnabled(true);
        txtAttributes.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);

        m_jCategory.addActionListener(fm);

        writeValueEOF();
    }

    public void activate() throws BasicException {

        s_GenRef = m_App.getGenerateProductReference();
        s_GenBarcode = m_App.getGenerateProductBarcode();
        s_DefBarcode = m_App.getUserBarcode();
        s_DefTaxCat = m_App.getDefaultTaxCategory();
        s_DefProdCat = m_App.getDefaultProductCategory();

        // Load the taxes logic
        taxeslogic = new TaxesLogic(taxsent.list());

        m_CategoryModel = new ComboBoxValModel(m_sentcat.list());
        m_jCategory.setModel(m_CategoryModel);

        taxcatmodel = new ComboBoxValModel(taxcatsent.list());
        m_jTax.setModel(taxcatmodel);

        attmodel = new ComboBoxValModel(attsent.list());
        attmodel.add(0, null);
        m_jAtt.setModel(attmodel);

        m_oCurrentProductEdit = new ProductInfoEdit();
    }

    @Override
    public void refresh() {

    }

    @Override
    public void writeValueEOF() {

        reportlock = true;
        pricesell = null;
        // Los valores
        m_jTitle.setText(AppLocal.getIntString("label.recordeof"));
        m_id = null;
        m_jRef.setText(null);
        m_jCode.setText(null);
        m_jName.setText(null);
        m_jComment.setSelected(false);
        m_jScale.setSelected(false);
        m_CategoryModel.setSelectedKey(null);
        taxcatmodel.setSelectedKey(null);
        attmodel.setSelectedKey(null);
        m_jPriceBuy.setText(null);
        setPriceSell(null);
        m_jImage.setImage(null);
        m_jstockcost.setText(null);
        m_jstockvolume.setText(null);
        m_jInCatalog.setSelected(false);
        m_jCatalogOrder.setText(null);
        txtAttributes.setText(null);
        reportlock = false;
        jCheckBox_ComplexProduct.setSelected(false);

        // Los habilitados
        m_jRef.setEnabled(false);
        m_jCode.setEnabled(false);
        m_jName.setEnabled(false);
        m_jComment.setEnabled(false);
        m_jScale.setEnabled(false);
        m_jCategory.setEnabled(false);
        m_jTax.setEnabled(false);
        m_jAtt.setEnabled(false);
        m_jPriceBuy.setEnabled(false);
        m_jPriceSell.setEnabled(false);
        m_jPriceSellTax.setEnabled(false);
        m_jmargin.setEnabled(false);
        m_jImage.setEnabled(false);
        m_jstockcost.setEnabled(false);
        m_jstockvolume.setEnabled(false);
        m_jInCatalog.setEnabled(false);
        m_jCatalogOrder.setEnabled(false);
        txtAttributes.setEnabled(false);
        jCheckBox_ComplexProduct.setEnabled(false);

        calculateMargin();
        calculatePriceSellTax();
    }

    @Override
    public void writeValueInsert() {

        reportlock = false;
        pricesell = null;
        // Los valores
        m_jTitle.setText(AppLocal.getIntString("label.recordnew"));
        m_id = UUID.randomUUID().toString();

        if (s_GenRef.equals("true")) {
            String sReferense = "1";
            try {
                if (product.list() != null) {
                    sReferense = Integer.toString(product.list().size() + 1);
                }
            } catch (BasicException ex) {
                Logger.getLogger(ProductsEditor.class.getName()).log(Level.SEVERE, null, ex);
            }

            for (int i = sReferense.length(); i < 4; i++) {
                sReferense = "0".concat(sReferense);
            }
            m_jRef.setText(sReferense);
        } else {
            m_jRef.setText(null);
        }

        m_jName.setText(null);
        m_jComment.setSelected(false);
        m_jScale.setSelected(false);
        m_CategoryModel.setSelectedKey(s_DefProdCat);
        taxcatmodel.setSelectedKey(s_DefTaxCat);
        attmodel.setSelectedKey(null);
        m_jPriceBuy.setText(null);
        setPriceSell(null);
        m_jImage.setImage(null);
        m_jstockcost.setText(null);
        m_jstockvolume.setText(null);
        m_jInCatalog.setSelected(true);
        m_jCatalogOrder.setText(null);
        txtAttributes.setText(null);
        reportlock = false;
        jCheckBox_ComplexProduct.setSelected(false);

        // Los habilitados
        m_jRef.setEnabled(true);
        m_jCode.setEnabled(true);
        m_jName.setEnabled(true);
        m_jComment.setEnabled(true);
        m_jScale.setEnabled(true);
        m_jCategory.setEnabled(true);
        m_jTax.setEnabled(true);
        m_jAtt.setEnabled(true);
        m_jPriceBuy.setEnabled(true);
        m_jPriceSell.setEnabled(true);
        m_jPriceSellTax.setEnabled(true);
        m_jmargin.setEnabled(true);
        m_jImage.setEnabled(true);
        m_jstockcost.setEnabled(true);
        m_jstockvolume.setEnabled(true);
        m_jInCatalog.setEnabled(true);
        m_jCatalogOrder.setEnabled(false);
        txtAttributes.setEnabled(true);
        jCheckBox_ComplexProduct.setEnabled(true);
        m_jPriceBuy.setEditable(true);

        if (s_GenBarcode.equals("true")) {
            try {
                CategoryInfo mCurrentCategory = (CategoryInfo) m_CategoryModel.getSelectedItem();
                if (mCurrentCategory != null) {
                    m_jCode.setText(generateBarCode(m_dSales.countPonductsByCategory(mCurrentCategory.getID()), mCurrentCategory.getCode()));
                } else {
                    m_jCode.setText(null);
                }
            } catch (BasicException ex) {
                m_jCode.setText(null);
            }
        } else {
            m_jCode.setText(null);
        }


//        jScrollPane3.setViewportView(jTable_ComplexData);
        calculateMargin();
        calculatePriceSellTax();
        try {
            setComplexData((String)m_id);
        } catch (BasicException ex) {
            Logger.getLogger(ProductsEditor.class.getName()).log(Level.SEVERE, null, ex);
        }    
        jTable_ComplexData.setEnabled(false);
        jButton3.setEnabled(false);
    }

    @Override
    public void writeValueDelete(Object value) {

        reportlock = true;
        Object[] myprod = (Object[]) value;
        m_jTitle.setText(Formats.STRING.formatValue(myprod[1]) + " - " + Formats.STRING.formatValue(myprod[3]) + " " + AppLocal.getIntString("label.recorddeleted"));
        m_id = myprod[0];
        m_jRef.setText(Formats.STRING.formatValue(myprod[1]));
        m_jCode.setText(Formats.STRING.formatValue(myprod[2]));
        m_jName.setText(Formats.STRING.formatValue(myprod[3]));
        m_jComment.setSelected(((Boolean) myprod[4]));
        m_jScale.setSelected(((Boolean) myprod[5]));
        if((Boolean) myprod[17]){
            m_jPriceBuy.setText(Formats.CURRENCY.formatValue(m_dSales.getProductPriceBy((String)m_id)));
        }else{
            m_jPriceBuy.setText(Formats.CURRENCY.formatValue(myprod[6]));
        }
        setPriceSell(myprod[7]);
        m_CategoryModel.setSelectedKey(myprod[8]);
        taxcatmodel.setSelectedKey(myprod[9]);
        attmodel.setSelectedKey(myprod[10]);
        m_jImage.setImage(findImage(m_id));
        m_jstockcost.setText(Formats.CURRENCY.formatValue(myprod[12]));
        m_jstockvolume.setText(Formats.DOUBLE.formatValue(myprod[13]));
        m_jInCatalog.setSelected(((Boolean) myprod[14]));
        m_jCatalogOrder.setText(Formats.INT.formatValue(myprod[15]));
        txtAttributes.setText(Formats.BYTEA.formatValue(myprod[16]));
        txtAttributes.setCaretPosition(0);
        reportlock = false;
        jCheckBox_ComplexProduct.setSelected(((Boolean) myprod[17]));

        // Los habilitados
        m_jRef.setEnabled(false);
        m_jCode.setEnabled(false);
        m_jName.setEnabled(false);
        m_jComment.setEnabled(false);
        m_jScale.setEnabled(false);
        m_jCategory.setEnabled(false);
        m_jTax.setEnabled(false);
        m_jAtt.setEnabled(false);
        m_jPriceBuy.setEnabled(false);
        m_jPriceSell.setEnabled(false);
        m_jPriceSellTax.setEnabled(false);
        m_jmargin.setEnabled(false);
        m_jImage.setEnabled(false);
        m_jstockcost.setEnabled(false);
        m_jstockvolume.setEnabled(false);
        m_jInCatalog.setEnabled(false);
        m_jCatalogOrder.setEnabled(false);
        txtAttributes.setEnabled(false);
        jCheckBox_ComplexProduct.setEnabled(false);

        calculateMargin();
        calculatePriceSellTax();
    }

    @Override
    public void writeValueEdit(Object value) {

        reportlock = true;
        Object[] myprod = (Object[]) value;
        m_jTitle.setText(Formats.STRING.formatValue(myprod[1]) + " - " + Formats.STRING.formatValue(myprod[3]));
        m_id = myprod[0];
        m_jRef.setText(Formats.STRING.formatValue(myprod[1]));
        m_jCode.setText(Formats.STRING.formatValue(myprod[2]));
        m_jName.setText(Formats.STRING.formatValue(myprod[3]));
        m_jComment.setSelected(((Boolean) myprod[4]));
        m_jScale.setSelected(((Boolean) myprod[5]));
        if((Boolean) myprod[17]){
            m_jPriceBuy.setText(Formats.CURRENCY.formatValue(m_dSales.getProductPriceBy((String)m_id)));
         }else{
            m_jPriceBuy.setText(Formats.CURRENCY.formatValue(myprod[6]));
        }
        setPriceSell(myprod[7]);
        m_CategoryModel.setSelectedKey(myprod[8]);
        taxcatmodel.setSelectedKey(myprod[9]);
        attmodel.setSelectedKey(myprod[10]);
        m_jImage.setImage(findImage(m_id));
        m_jstockcost.setText(Formats.CURRENCY.formatValue(myprod[12]));
        m_jstockvolume.setText(Formats.DOUBLE.formatValue(myprod[13]));
        m_jInCatalog.setSelected(((Boolean) myprod[14]));
        m_jCatalogOrder.setText(Formats.INT.formatValue(myprod[15]));
        txtAttributes.setText(Formats.BYTEA.formatValue(myprod[16]));
        txtAttributes.setCaretPosition(0);
        reportlock = false;
        jCheckBox_ComplexProduct.setSelected(((Boolean) myprod[17]));
        m_jPriceBuy.setEditable(!(Boolean) myprod[17]);
        jButton3.setEnabled((Boolean) myprod[17]);
        jTable_ComplexData.setEnabled((Boolean) myprod[17]);

        // Los habilitados
        m_jRef.setEnabled(true);
        m_jCode.setEnabled(true);
        m_jName.setEnabled(true);
        m_jComment.setEnabled(true);
        m_jScale.setEnabled(true);
        m_jCategory.setEnabled(true);
        m_jTax.setEnabled(true);
        m_jAtt.setEnabled(true);
        m_jPriceBuy.setEnabled(true);
        m_jPriceSell.setEnabled(true);
        m_jPriceSellTax.setEnabled(true);
        m_jmargin.setEnabled(true);
        m_jImage.setEnabled(true);
        m_jstockcost.setEnabled(true);
        m_jstockvolume.setEnabled(true);
        m_jInCatalog.setEnabled(true);
        m_jCatalogOrder.setEnabled(m_jInCatalog.isSelected());
        txtAttributes.setEnabled(true);
        jCheckBox_ComplexProduct.setEnabled(true);

        calculateMargin();
        calculatePriceSellTax();
        try {
            setComplexData((String)m_id);
        } catch (BasicException ex) {
            Logger.getLogger(ProductsEditor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public Object createValue() throws BasicException {

        Object[] myprod = new Object[18];
        myprod[0] = m_id;
        myprod[1] = m_jRef.getText();
        myprod[2] = m_jCode.getText();
        myprod[3] = m_jName.getText();
        myprod[4] = m_jComment.isSelected();
        myprod[5] = m_jScale.isSelected();
//        myprod[6] = Formats.CURRENCY.parseValue(m_jPriceBuy.getText());
        myprod[6] = Formats.CURRENCY.parseValue(calculateComplexPriceBy((String)m_id));
        myprod[7] = pricesell;
        myprod[8] = m_CategoryModel.getSelectedKey();
        myprod[9] = taxcatmodel.getSelectedKey();
        myprod[10] = attmodel.getSelectedKey();
        myprod[11] = m_jImage.getImage();
        myprod[12] = Formats.CURRENCY.parseValue(m_jstockcost.getText());
        myprod[13] = Formats.DOUBLE.parseValue(m_jstockvolume.getText());
        myprod[14] = m_jInCatalog.isSelected();
        myprod[15] = Formats.INT.parseValue(m_jCatalogOrder.getText());
        myprod[16] = Formats.BYTEA.parseValue(txtAttributes.getText());
        myprod[17] = jCheckBox_ComplexProduct.isSelected();

        return myprod;
    }

    public void setComplexData(String id) throws BasicException{
        List<IngredientInfo> ingredients = m_dSales.getIngredients(id);
        jTable_ComplexData.setModel(new DefaultTableModel(new String [] {
                    "ID",
                    AppLocal.getIntString("column.product"),
                    AppLocal.getIntString("column.coefficient")
                }, ingredients.size()));
        jTable_ComplexData.getColumnModel().getColumn(0).setResizable(false);
        jTable_ComplexData.getColumnModel().getColumn(0).setPreferredWidth(0);
        jTable_ComplexData.getColumnModel().getColumn(0).setMinWidth(0);
        jTable_ComplexData.getColumnModel().getColumn(0).setMaxWidth(0);
        jTable_ComplexData.getColumnModel().getColumn(1).setResizable(false);
        jTable_ComplexData.getColumnModel().getColumn(2).setResizable(false);
        int row = 0;
        for (IngredientInfo ingredient : ingredients) {
            jTable_ComplexData.setValueAt(ingredient.getId(), row, 0);
            jTable_ComplexData.setValueAt(ingredient.getIngredientName(), row, 1);
            jTable_ComplexData.setValueAt(ingredient.getIngredientWeight(), row, 2);
            row++;
        }
    }

    class MJTable extends JTable {
        public MJTable() {
            super();
        }
        @Override
        public boolean isCellEditable(int row, int column)
        {
          return false;
        }
    }
    public String calculateComplexPriceBy(String id) throws BasicException{
        if(jCheckBox_ComplexProduct.isSelected()){
            m_jPriceBuy.setEditable(false);
            jButton3.setEnabled(true);
            jTable_ComplexData.setEnabled(true);
            String outValue = m_dSales.getComplexPriceBy(id).toString();
            if(outValue != null){
                outValue = outValue.replace(".",",");
            }else{
                outValue = "0";
            }
            return outValue;
        }
        m_jPriceBuy.setEditable(true);
        jButton3.setEnabled(false);
        jTable_ComplexData.setEnabled(false);
        return m_jPriceBuy.getText();
    }

    @Override
    public Component getComponent() {
        return this;
    }

    private BufferedImage findImage(Object id) {
        try {
            return (BufferedImage) loadimage.find(id);
        } catch (BasicException e) {
            return null;
        }
    }

    private void calculateMargin() {

        if (!reportlock) {
            reportlock = true;

            Double dPriceBuy = readCurrency(m_jPriceBuy.getText());
            Double dPriceSell = (Double) pricesell;

            if (dPriceBuy == null || dPriceSell == null) {
                m_jmargin.setText(null);
            } else {
                m_jmargin.setText(Formats.PERCENT.formatValue(dPriceSell / dPriceBuy - 1.0));
            }
            reportlock = false;
        }
    }

    private void calculatePriceSellTax() {

        if (!reportlock) {
            reportlock = true;

            Double dPriceSell = (Double) pricesell;

            if (dPriceSell == null) {
                m_jPriceSellTax.setText(null);
            } else {
                double dTaxRate = taxeslogic.getTaxRate((TaxCategoryInfo) taxcatmodel.getSelectedItem(), new Date());
                m_jPriceSellTax.setText(Formats.CURRENCY.formatValue(dPriceSell * (1.0 + dTaxRate)));
            }
            reportlock = false;
        }
    }

    private String generateBarCode(Integer iCountProducts, String sCategoryPrefix) {
        String sCode = Integer.toString(iCountProducts + 1);

        for (int i = sCode.length(); i < 5; i++) {
            sCode = "0".concat(sCode);
        }

        sCode = s_DefBarcode.concat(sCategoryPrefix == null ? "0000" : sCategoryPrefix).concat(sCode);

        return sCode.concat(Character.toString(EAN13LogicImpl.calcChecksum(sCode)));
    }

    private void calculatePriceSellfromMargin() {

        if (!reportlock) {
            reportlock = true;

            Double dPriceBuy = readCurrency(m_jPriceBuy.getText());
            Double dMargin = readPercent(m_jmargin.getText());

            if (dMargin == null || dPriceBuy == null) {
                setPriceSell(null);
            } else {
                setPriceSell(dPriceBuy * (1.0 + dMargin));
            }

            reportlock = false;
        }

    }

    private void calculatePriceSellfromPST() {

        if (!reportlock) {
            reportlock = true;

            Double dPriceSellTax = readCurrency(m_jPriceSellTax.getText());

            if (dPriceSellTax == null) {
                setPriceSell(null);
            } else {
                double dTaxRate = taxeslogic.getTaxRate((TaxCategoryInfo) taxcatmodel.getSelectedItem(), new Date());
                setPriceSell(dPriceSellTax / (1.0 + dTaxRate));
            }

            reportlock = false;
        }
    }

    private void setPriceSell(Object value) {

        if (!priceselllock) {
            priceselllock = true;
            pricesell = value;
            m_jPriceSell.setText(Formats.CURRENCY.formatValue(pricesell));
            priceselllock = false;
        }
    }

    private class PriceSellManager implements DocumentListener {

        @Override
        public void changedUpdate(DocumentEvent e) {
            if (!priceselllock) {
                priceselllock = true;
                pricesell = readCurrency(m_jPriceSell.getText());
                priceselllock = false;
            }
            calculateMargin();
            calculatePriceSellTax();
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            if (!priceselllock) {
                priceselllock = true;
                pricesell = readCurrency(m_jPriceSell.getText());
                priceselllock = false;
            }
            calculateMargin();
            calculatePriceSellTax();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            if (!priceselllock) {
                priceselllock = true;
                pricesell = readCurrency(m_jPriceSell.getText());
                priceselllock = false;
            }
            calculateMargin();
            calculatePriceSellTax();
        }
    }

    private class FieldsManager implements DocumentListener, ActionListener {

        @Override
        public void changedUpdate(DocumentEvent e) {
            calculateMargin();
            calculatePriceSellTax();
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            calculateMargin();
            calculatePriceSellTax();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            calculateMargin();
            calculatePriceSellTax();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            calculateMargin();
            calculatePriceSellTax();
        }
    }

    private class PriceTaxManager implements DocumentListener {

        @Override
        public void changedUpdate(DocumentEvent e) {
            calculatePriceSellfromPST();
            calculateMargin();
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            calculatePriceSellfromPST();
            calculateMargin();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            calculatePriceSellfromPST();
            calculateMargin();
        }
    }

    private class MarginManager implements DocumentListener {

        @Override
        public void changedUpdate(DocumentEvent e) {
            calculatePriceSellfromMargin();
            calculatePriceSellTax();
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            calculatePriceSellfromMargin();
            calculatePriceSellTax();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            calculatePriceSellfromMargin();
            calculatePriceSellTax();
        }
    }

    private static Double readCurrency(String sValue) {
        try {
            return (Double) Formats.CURRENCY.parseValue(sValue);
        } catch (BasicException e) {
            return null;
        }
    }

    private static Double readPercent(String sValue) {
        try {
            return (Double) Formats.PERCENT.parseValue(sValue);
        } catch (BasicException e) {
            return null;
        }
    }

    private void printProductLabel(ProductInfoEdit productinfo) throws TicketPrinterException, ScriptException {
        InputStream schema = getClass().getResourceAsStream(PRINTER_SHEMA);
        InputStream template = getClass().getResourceAsStream(PRINT_PRODUCT_LABEL);
        if (schema == null || template == null) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotprintticket"));
            msg.show(this);
        } else {
            m_TTP = new TicketParser(schema, m_App.getDeviceTicket());
            try {
                ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.VELOCITY);
                script.put("product", productinfo);
                script.put("local", new AppLocal());
                m_TTP.printTicket(template, script);
            } catch (ScriptException e) {
                MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotprintticket"), e);
                msg.show(this);
            } catch (TicketPrinterException e) {
                MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotprintticket"), e);
                msg.show(this);
                throw e;
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        m_jRef = new javax.swing.JTextField();
        m_jName = new javax.swing.JTextField();
        m_jTitle = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        m_jCode = new javax.swing.JTextField();
        m_jImage = new com.openbravo.data.gui.JImageEditor();
        jLabel3 = new javax.swing.JLabel();
        m_jPriceBuy = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        m_jPriceSell = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        m_jCategory = new javax.swing.JComboBox();
        jLabel7 = new javax.swing.JLabel();
        m_jTax = new javax.swing.JComboBox();
        m_jmargin = new javax.swing.JTextField();
        m_jPriceSellTax = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        m_jAtt = new javax.swing.JComboBox();
        jButtonGenBarcode = new javax.swing.JButton();
        m_jCodetype = new javax.swing.JComboBox();
        jPanel2 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        m_jstockcost = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        m_jstockvolume = new javax.swing.JTextField();
        m_jScale = new javax.swing.JCheckBox();
        m_jComment = new javax.swing.JCheckBox();
        jLabel18 = new javax.swing.JLabel();
        m_jCatalogOrder = new javax.swing.JTextField();
        m_jInCatalog = new javax.swing.JCheckBox();
        jLabel8 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jCheckBox_ComplexProduct = new javax.swing.JCheckBox();
        jLabel_ComplexProduct = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTable_ComplexData = new javax.swing.JTable();
        jButton3 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new org.fife.ui.rtextarea.RTextScrollPane();
        txtAttributes = new org.fife.ui.rsyntaxtextarea.RSyntaxTextArea();
        m_jPrintLabel = new javax.swing.JButton();
        m_jVirtualKeyboard = new javax.swing.JButton();

        jLabel1.setText(AppLocal.getIntString("label.prodref")); // NOI18N

        jLabel2.setText(AppLocal.getIntString("label.prodname")); // NOI18N

        m_jTitle.setFont(m_jTitle.getFont().deriveFont((m_jTitle.getFont().getStyle() | java.awt.Font.ITALIC) | java.awt.Font.BOLD, m_jTitle.getFont().getSize()+2));

        jLabel6.setText(AppLocal.getIntString("label.prodbarcode")); // NOI18N

        m_jImage.setMaxDimensions(new java.awt.Dimension(256, 256));

        jLabel3.setText(AppLocal.getIntString("label.prodpricebuy")); // NOI18N

        m_jPriceBuy.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        m_jPriceBuy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jPriceBuyActionPerformed(evt);
            }
        });

        jLabel4.setText(AppLocal.getIntString("label.prodpricesell")); // NOI18N

        m_jPriceSell.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        jLabel5.setText(AppLocal.getIntString("label.prodcategory")); // NOI18N

        jLabel7.setText(AppLocal.getIntString("label.taxcategory")); // NOI18N

        m_jmargin.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        m_jPriceSellTax.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        jLabel16.setText(AppLocal.getIntString("label.prodpriceselltax")); // NOI18N

        jLabel13.setText(AppLocal.getIntString("label.attributes")); // NOI18N

        jButtonGenBarcode.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/gnome-tali.png"))); // NOI18N
        jButtonGenBarcode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonGenBarcodeActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(m_jCode)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonGenBarcode))
                    .addComponent(m_jTax, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jAtt, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(m_jPriceSellTax, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE)
                            .addComponent(m_jPriceBuy, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(m_jPriceSell, javax.swing.GroupLayout.Alignment.LEADING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addComponent(m_jCodetype, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(4, 4, 4))
                            .addComponent(m_jmargin, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(m_jImage, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(jLabel6)
                            .addComponent(m_jCode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButtonGenBarcode))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(jLabel3)
                            .addComponent(m_jPriceBuy, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(m_jCodetype, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(jLabel4)
                            .addComponent(m_jPriceSell, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(m_jmargin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(jLabel16)
                            .addComponent(m_jPriceSellTax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(jLabel7)
                            .addComponent(m_jTax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(jLabel5)
                            .addComponent(m_jCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(jLabel13)
                            .addComponent(m_jAtt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(m_jImage, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        jTabbedPane1.addTab(AppLocal.getIntString("label.prodgeneral"), jPanel1); // NOI18N

        jPanel2.setLayout(null);

        jLabel9.setText(AppLocal.getIntString("label.prodstockcost")); // NOI18N
        jPanel2.add(jLabel9);
        jLabel9.setBounds(10, 20, 150, 14);

        m_jstockcost.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jPanel2.add(m_jstockcost);
        m_jstockcost.setBounds(160, 20, 80, 20);

        jLabel10.setText(AppLocal.getIntString("label.prodstockvol")); // NOI18N
        jPanel2.add(jLabel10);
        jLabel10.setBounds(10, 50, 150, 14);

        m_jstockvolume.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jPanel2.add(m_jstockvolume);
        m_jstockvolume.setBounds(160, 50, 80, 20);
        jPanel2.add(m_jScale);
        m_jScale.setBounds(160, 150, 80, 20);
        jPanel2.add(m_jComment);
        m_jComment.setBounds(160, 130, 80, 20);

        jLabel18.setText(AppLocal.getIntString("label.prodorder")); // NOI18N
        jPanel2.add(jLabel18);
        jLabel18.setBounds(10, 80, 150, 14);

        m_jCatalogOrder.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jPanel2.add(m_jCatalogOrder);
        m_jCatalogOrder.setBounds(160, 80, 80, 20);

        m_jInCatalog.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jInCatalogActionPerformed(evt);
            }
        });
        jPanel2.add(m_jInCatalog);
        m_jInCatalog.setBounds(160, 110, 50, 20);

        jLabel8.setText(AppLocal.getIntString("label.prodincatalog")); // NOI18N
        jPanel2.add(jLabel8);
        jLabel8.setBounds(10, 110, 150, 20);

        jLabel11.setText(AppLocal.getIntString("label.prodaux")); // NOI18N
        jLabel11.setMaximumSize(new java.awt.Dimension(107, 14));
        jLabel11.setMinimumSize(new java.awt.Dimension(107, 14));
        jLabel11.setPreferredSize(new java.awt.Dimension(107, 14));
        jPanel2.add(jLabel11);
        jLabel11.setBounds(10, 130, 150, 20);

        jLabel12.setText(AppLocal.getIntString("label.prodscale")); // NOI18N
        jLabel12.setMaximumSize(new java.awt.Dimension(107, 14));
        jLabel12.setMinimumSize(new java.awt.Dimension(107, 14));
        jLabel12.setPreferredSize(new java.awt.Dimension(107, 14));
        jPanel2.add(jLabel12);
        jLabel12.setBounds(10, 150, 150, 20);

        jCheckBox_ComplexProduct.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox_ComplexProductActionPerformed(evt);
            }
        });
        jPanel2.add(jCheckBox_ComplexProduct);
        jCheckBox_ComplexProduct.setBounds(160, 170, 81, 20);

        jLabel_ComplexProduct.setText(AppLocal.getIntString("label.complex"));
        jLabel_ComplexProduct.setMaximumSize(new java.awt.Dimension(107, 14));
        jLabel_ComplexProduct.setMinimumSize(new java.awt.Dimension(107, 14));
        jLabel_ComplexProduct.setPreferredSize(new java.awt.Dimension(107, 14));
        //jLabel14.setText(AppLocal.getIntString("label.prodscale"));
        jPanel2.add(jLabel_ComplexProduct);
        jLabel_ComplexProduct.setBounds(10, 170, 150, 20);

        jTable_ComplexData.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] { }
            ,
            new String [] {}
        ));
        jTable_ComplexData.setEnabled(false);
        jScrollPane3.setViewportView(jTable_ComplexData);

        jPanel2.add(jScrollPane3);
        jScrollPane3.setBounds(250, 10, 280, 160);

        jButton3.setText(AppLocal.getIntString("button.editRecipe"));
        jButton3.setEnabled(false);
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton3);
        jButton3.setBounds(320, 180, 210, 23);

        jTabbedPane1.addTab(AppLocal.getIntString("label.prodstock"), jPanel2); // NOI18N

        jPanel3.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel3.setLayout(new java.awt.BorderLayout());

        txtAttributes.setFont(new java.awt.Font("DialogInput", 0, 12)); // NOI18N
        jScrollPane1.setViewportView(txtAttributes);

        jPanel3.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jTabbedPane1.addTab(AppLocal.getIntString("label.properties"), jPanel3); // NOI18N

        m_jPrintLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/printer.png"))); // NOI18N
        m_jPrintLabel.setFocusPainted(false);
        m_jPrintLabel.setFocusable(false);
        m_jPrintLabel.setMargin(new java.awt.Insets(8, 14, 8, 14));
        m_jPrintLabel.setRequestFocusEnabled(false);
        m_jPrintLabel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jPrintLabelActionPerformed(evt);
            }
        });

        m_jVirtualKeyboard.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/input-keyboard.png"))); // NOI18N
        m_jVirtualKeyboard.setFocusPainted(false);
        m_jVirtualKeyboard.setFocusable(false);
        m_jVirtualKeyboard.setMargin(new java.awt.Insets(8, 14, 8, 14));
        m_jVirtualKeyboard.setRequestFocusEnabled(false);
        m_jVirtualKeyboard.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jVirtualKeyboardActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 607, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(m_jRef, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(m_jName))
                            .addComponent(m_jTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(m_jPrintLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(m_jVirtualKeyboard))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(m_jTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(m_jName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(m_jRef, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(m_jPrintLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_jVirtualKeyboard, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane1))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void m_jVirtualKeyboardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jVirtualKeyboardActionPerformed
        m_oCurrentProductEdit.setID(m_id.toString());
        m_oCurrentProductEdit.setReference(m_jRef.getText());
        m_oCurrentProductEdit.setName(m_jName.getText());
        m_oCurrentProductEdit.setCode(m_jCode.getText());

        Double dPriceBuy = readCurrency(m_jPriceBuy.getText());
        if (dPriceBuy != null) {
            m_oCurrentProductEdit.setPriceBuy(dPriceBuy);
        }
        Double dPriceSell = readCurrency(m_jPriceSell.getText());
        if (dPriceSell != null) {
            m_oCurrentProductEdit.setPriceSell(dPriceSell);
        }

        m_oCurrentProductEdit.setCategoryID(m_CategoryModel.getSelectedKey().toString());
        m_oCurrentProductEdit.setTaxID(taxcatmodel.getSelectedKey().toString());
        m_oCurrentProductEdit.setAttributeUseID((attmodel.getSelectedKey() != null) ? attmodel.getSelectedKey().toString() : null);

        try {
            JProductEditDialog editor = JProductEditDialog.getProductEditor(this, m_dSales);
            editor.editProductFields(m_oCurrentProductEdit);
            editor.setVisible(true);

            m_oCurrentProductEdit = editor.getEditProduct();
            m_jRef.setText(m_oCurrentProductEdit.getReference());
            m_jName.setText(m_oCurrentProductEdit.getName());
            m_jCode.setText(m_oCurrentProductEdit.getCode());
            m_jPriceBuy.setText(Formats.CURRENCY.formatValue(m_oCurrentProductEdit.getPriceBuy()));
            m_jPriceSell.setText(Formats.CURRENCY.formatValue(m_oCurrentProductEdit.getPriceSell()));
            m_CategoryModel.setSelectedKey(m_oCurrentProductEdit.getCategoryID());
            taxcatmodel.setSelectedKey(m_oCurrentProductEdit.getTaxID());
            attmodel.setSelectedKey(m_oCurrentProductEdit.getAttributeUseID());
        } catch (BasicException ex) {
            Logger.getLogger(ProductsEditor.class.getName()).log(Level.SEVERE, null, ex);
        }
        calculateMargin();

    }//GEN-LAST:event_m_jVirtualKeyboardActionPerformed

    private void m_jPrintLabelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jPrintLabelActionPerformed
        m_oCurrentProductEdit.setID(m_id.toString());
        m_oCurrentProductEdit.setReference(m_jRef.getText());
        m_oCurrentProductEdit.setName(m_jName.getText());
        m_oCurrentProductEdit.setCode(m_jCode.getText());

        Double dPriceBuy = readCurrency(m_jPriceBuy.getText());
        if (dPriceBuy != null) {
            m_oCurrentProductEdit.setPriceBuy(dPriceBuy);
        }
        Double dPriceSell = readCurrency(m_jPriceSell.getText());
        if (dPriceSell != null) {
            m_oCurrentProductEdit.setPriceSell(dPriceSell);
        }

        m_oCurrentProductEdit.setCategoryID(m_CategoryModel.getSelectedKey().toString());
        m_oCurrentProductEdit.setTaxID(taxcatmodel.getSelectedKey().toString());
        m_oCurrentProductEdit.setAttributeUseID((attmodel.getSelectedKey() != null) ? attmodel.getSelectedKey().toString() : null);

        try {
            printProductLabel(m_oCurrentProductEdit);
        } catch (TicketPrinterException | ScriptException ex) {
            Logger.getLogger(ProductsEditor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_m_jPrintLabelActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        ComplexProductsEditor complexProductsEditor = new ComplexProductsEditor(m_dSales, m_id, jTable_ComplexData, m_jPriceBuy);
        complexProductsEditor.setVisible(true);
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jCheckBox_ComplexProductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox_ComplexProductActionPerformed
        if (jCheckBox_ComplexProduct.isSelected()) {
            // TODO new ComplexProdust
            // проверить, есть ли такой продукт в базе
            if(m_id == null){
                m_id = UUID.randomUUID().toString();
            }
            try {
                ProductInfoExt productInfoExt = m_dSales.getProductInfo((String) m_id);
                if(productInfoExt == null){
                    Object[] newComplexProductData = new Object[]{
    //                    setString(1, (String)newComplexProductData[0]);         //ID,
                        m_id,
    //                    setString(2, (String)newComplexProductData[1]);         //REFERENCE,
                        m_jRef.getText(),
    //                    setString(3, (String)newComplexProductData[2]);         //CODE,
                        m_jCode.getText(),
    //                    setString(4, (String)newComplexProductData[3]);         //NAME,
                        m_jName.getText() == null ? "" : m_jName.getText(),
    //                    setBoolean(5, (Boolean)newComplexProductData[4]);       //ISCOM,
                        m_jComment.isSelected(),
    //                    setBoolean(6, (Boolean)newComplexProductData[5]);       //ISSCALE,
                        m_jScale.isSelected(),
    //                    setDouble(7, (Double)newComplexProductData[6]);         //PRICEBUY,
                        0.0,
    //                    setDouble(8, (Double)newComplexProductData[7]);         //PRICESELL,
                        m_jPriceSell.getText() != null && !m_jPriceSell.getText().trim().equals("") ? Double.parseDouble(m_jPriceSell.getText()) : 0.0,
    //                    setString(9, (String)newComplexProductData[8]);         //CATEGORY,
                        ((CategoryInfo)m_jCategory.getModel().getSelectedItem()).getID(),
    //                    setString(10, (String)newComplexProductData[9]);        //TAXCAT
                        ((TaxCategoryInfo)m_jTax.getModel().getSelectedItem()).getID(),
    //                    setString(11, (String)newComplexProductData[10]);       //ATTRIBUTESET_ID,  // null
                        null,
    //                    setObject(12,  newComplexProductData[11]);   //IMAGE,            // null
                        null,
    //                    setDouble(13, (Double)newComplexProductData[12]);       //STOCKCOST,        // null
                        null,
    //                    setDouble(14, (Double)newComplexProductData[13]);       //STOCKVOLUME,      // null
                        null,
    //                    setObject(15,  newComplexProductData[14]);   //ATTRIBUTES,       // null
                        null,
    //                    setBoolean(16, (Boolean)newComplexProductData[15]);     //ISCOMPLEX
                        jCheckBox_ComplexProduct.isSelected(),
                        m_jInCatalog.isSelected(),
                        null
                    };
                    m_dSales.insertNewComplexProduct(newComplexProductData).exec();
                }
                // если нет, то установить цену = 0 и занести в базу
            } catch (Exception ex) {
                writeLog(this, "jCheckBox_ComplexProductActionPerformed ex : " + ex.getMessage());
                Logger.getLogger(ProductsEditor.class.getName()).log(Level.SEVERE, null, ex);
            }

            // если есть, то продолжать работать в штатном режиме
            jButton3.setEnabled(true);
            jTable_ComplexData.setEnabled(true);
            m_jPriceBuy.setEditable(false);
        } else {
            jButton3.setEnabled(false);
            jTable_ComplexData.setEnabled(false);
            m_jPriceBuy.setEditable(true);
        }
    }//GEN-LAST:event_jCheckBox_ComplexProductActionPerformed

    private void m_jInCatalogActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jInCatalogActionPerformed

        if (m_jInCatalog.isSelected()) {
            m_jCatalogOrder.setEnabled(true);
        } else {
            m_jCatalogOrder.setEnabled(false);
            m_jCatalogOrder.setText(null);
        }
    }//GEN-LAST:event_m_jInCatalogActionPerformed

    private void jButtonGenBarcodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonGenBarcodeActionPerformed

        String sCode = m_jCode.getText();
        if (sCode.length() == 12) {
            m_jCode.setText(sCode.concat(Character.toString(EAN13LogicImpl.calcChecksum(sCode))));
        } else {

            if (JOptionPane.showConfirmDialog(this, AppLocal.getIntString("message.generatebarcode"), AppLocal.getIntString("title.editor"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                try {
                    CategoryInfo mCurrentCategory = (CategoryInfo) m_CategoryModel.getSelectedItem();
                    m_jCode.setText(generateBarCode(m_dSales.countPonductsByCategory(mCurrentCategory.getID()), mCurrentCategory.getCode()));
                } catch (BasicException ex) {
                    m_jCode.setText(null);
                }
            }
        }
    }//GEN-LAST:event_jButtonGenBarcodeActionPerformed

    private void m_jPriceBuyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jPriceBuyActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_m_jPriceBuyActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButtonGenBarcode;
    private javax.swing.JCheckBox jCheckBox_ComplexProduct;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabel_ComplexProduct;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private org.fife.ui.rtextarea.RTextScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTable_ComplexData;
    private javax.swing.JComboBox m_jAtt;
    private javax.swing.JTextField m_jCatalogOrder;
    private javax.swing.JComboBox m_jCategory;
    private javax.swing.JTextField m_jCode;
    private javax.swing.JComboBox m_jCodetype;
    private javax.swing.JCheckBox m_jComment;
    private com.openbravo.data.gui.JImageEditor m_jImage;
    private javax.swing.JCheckBox m_jInCatalog;
    private javax.swing.JTextField m_jName;
    private javax.swing.JTextField m_jPriceBuy;
    private javax.swing.JTextField m_jPriceSell;
    private javax.swing.JTextField m_jPriceSellTax;
    private javax.swing.JButton m_jPrintLabel;
    private javax.swing.JTextField m_jRef;
    private javax.swing.JCheckBox m_jScale;
    private javax.swing.JComboBox m_jTax;
    private javax.swing.JLabel m_jTitle;
    private javax.swing.JButton m_jVirtualKeyboard;
    private javax.swing.JTextField m_jmargin;
    private javax.swing.JTextField m_jstockcost;
    private javax.swing.JTextField m_jstockvolume;
    private org.fife.ui.rsyntaxtextarea.RSyntaxTextArea txtAttributes;
    // End of variables declaration//GEN-END:variables

}
