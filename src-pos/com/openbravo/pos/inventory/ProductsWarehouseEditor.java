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
//    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-130

package com.openbravo.pos.inventory;

import java.awt.Component;
import com.openbravo.basic.BasicException;
import com.openbravo.data.user.DirtyManager;
import com.openbravo.data.user.EditorRecord;
import com.openbravo.format.Formats;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.util.T2FileLogger;

/**
 *
 * @author adrianromero
 */
public class ProductsWarehouseEditor extends javax.swing.JPanel implements EditorRecord {

    public Object id;
    public Object prodid;
    public Object prodref;
    public Object prodname;
    public Object location;
    
    /** Creates new form ProductsWarehouseEditor */
    public ProductsWarehouseEditor(DirtyManager dirty) {
        initComponents();
        
        m_jMinimum.getDocument().addDocumentListener(dirty);
        m_jMaximum.getDocument().addDocumentListener(dirty);
    }
    
    public void writeValueEOF() {
        try{
            m_jTitle.setText(AppLocal.getIntString("label.recordeof"));
            id = null;
            prodid = null;
            prodref = null;
            prodname = null;
            location = null;
            m_jQuantity.setText(null);
            m_jMinimum.setText(null);
            m_jMaximum.setText(null);
            m_jMinimum.setEnabled(false);
            m_jMaximum.setEnabled(false);
        }catch(Exception e){
            T2FileLogger.writeLog(this.getClass().getName() + ".writeValueEOF", e.getMessage());
        }
    }
    public void writeValueInsert() {
        try{
            m_jTitle.setText(AppLocal.getIntString("label.recordnew"));
            id = null;
            prodid = null;
            prodref = null;
            prodname = null;
            location = null;
            m_jQuantity.setText(null);
            m_jMinimum.setText(null);
            m_jMaximum.setText(null);
            m_jMinimum.setEnabled(true);
            m_jMaximum.setEnabled(true);
        }catch(Exception e){
            T2FileLogger.writeLog(this.getClass().getName() + ".writeValueInsert", e.getMessage());
        }
    }
    public void writeValueEdit(Object value) {
        try{
            Object[] myprod = (Object[]) value;
            id = myprod[0];
            prodid = myprod[1];
            prodref = myprod[2];
            prodname = myprod[3];
            location = myprod[4];
            m_jTitle.setText(Formats.STRING.formatValue(myprod[2]) + " - " + Formats.STRING.formatValue(myprod[3]));
            // t2studio
            // if Complex Product
            // then quantity their ingredients
            if(myprod.length > 8){
                if(myprod[8] != null && (boolean)myprod[8]){
                    m_jQuantity.setText(Formats.INT.formatValue(myprod[9]));
                }else{
                    m_jQuantity.setText(Formats.DOUBLE.formatValue(myprod[7]));             
                }
            }else{
                m_jQuantity.setText(Formats.DOUBLE.formatValue(myprod[7]));                             
            }
            m_jMinimum.setText(Formats.DOUBLE.formatValue(myprod[5]));
            m_jMaximum.setText(Formats.DOUBLE.formatValue(myprod[6]));
            m_jMinimum.setEnabled(true);
            m_jMaximum.setEnabled(true);
        }catch(Exception e){
            T2FileLogger.writeLog(this.getClass().getName() + ".writeValueEdit", e.getMessage());
        }
     }
    public void writeValueDelete(Object value) {
        try{
            Object[] myprod = (Object[]) value;
            id = myprod[0];
            prodid = myprod[1];
            prodref = myprod[2];
            prodname = myprod[3];
            location = myprod[4];
            m_jTitle.setText(Formats.STRING.formatValue(myprod[2]) + " - " + Formats.STRING.formatValue(myprod[3]));
            // t2studio
            // if Complex Product
            // then quantity their ingredients
            if(myprod.length > 8){
                if(myprod[8] != null && (boolean)myprod[8]){
                    m_jQuantity.setText(Formats.INT.formatValue(myprod[9]));
                }else{
                    m_jQuantity.setText(Formats.DOUBLE.formatValue(myprod[7]));             
                }
            }else{
                m_jQuantity.setText(Formats.DOUBLE.formatValue(myprod[7]));                             
            }
            m_jMinimum.setText(Formats.DOUBLE.formatValue(myprod[5]));
            m_jMaximum.setText(Formats.DOUBLE.formatValue(myprod[6]));
            m_jMinimum.setEnabled(false);
            m_jMaximum.setEnabled(false);
        }catch(Exception e){
            T2FileLogger.writeLog(this.getClass().getName() + ".writeValueDelete", e.getMessage());
        }
    }
    public Object createValue() throws BasicException {
        try{
            return new Object[] {
                id,
                prodid,
                prodref,
                prodname,
                location,
                Formats.DOUBLE.parseValue(m_jMinimum.getText()),
                Formats.DOUBLE.parseValue(m_jMaximum.getText()),
                Formats.DOUBLE.parseValue(m_jQuantity.getText())
            };
        }catch(Exception e){
            T2FileLogger.writeLog(this.getClass().getName() + ".createValue", e.getMessage());
        }
        return null;
    }
    
    public Component getComponent() {
        return this;
    }
    
    public void refresh() {
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        m_jTitle = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        m_jQuantity = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        m_jMinimum = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        m_jMaximum = new javax.swing.JTextField();

        m_jTitle.setFont(m_jTitle.getFont().deriveFont((m_jTitle.getFont().getStyle() | java.awt.Font.ITALIC) | java.awt.Font.BOLD, m_jTitle.getFont().getSize()+2));

        jLabel3.setText(AppLocal.getIntString("label.units")); // NOI18N

        m_jQuantity.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        m_jQuantity.setEnabled(false);

        jLabel4.setText(AppLocal.getIntString("label.minimum")); // NOI18N

        m_jMinimum.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        jLabel5.setText(AppLocal.getIntString("label.maximum")); // NOI18N

        m_jMaximum.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(m_jTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(m_jQuantity, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(m_jMinimum, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(m_jMaximum, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(68, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(m_jTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel3)
                    .addComponent(m_jQuantity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel4)
                    .addComponent(m_jMinimum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel5)
                    .addComponent(m_jMaximum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JTextField m_jMaximum;
    private javax.swing.JTextField m_jMinimum;
    private javax.swing.JTextField m_jQuantity;
    private javax.swing.JLabel m_jTitle;
    // End of variables declaration//GEN-END:variables
    
}
