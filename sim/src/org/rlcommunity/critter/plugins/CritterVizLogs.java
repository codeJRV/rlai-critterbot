/*
 * CritterVizLogs.java
 *
 * Created on March 25, 2009, 11:54 AM
 */

package org.rlcommunity.critter.plugins;

import java.awt.event.WindowListener;
import org.rlcommunity.critter.Drops.CritterLogTagDrop;
import org.rlcommunity.critter.Drops.DropInterface;

/**
 *
 * @author  critterbot
 */
public class CritterVizLogs extends javax.swing.JFrame {
    
    DropInterface di;
    WindowListener mainViz;
    /** Creates new form CritterVizLogs */
    public CritterVizLogs(DropInterface _di,WindowListener _mainViz) {
        di = _di;
        mainViz = _mainViz;
        initComponents();
        this.addWindowListener(mainViz);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLogComment = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLogCommentSend = new javax.swing.JButton();
        jLogName = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Log Interface");
        setLocationByPlatform(true);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Log Comments"));

        jLogComment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendLogTag(evt);
            }
        });

        jLabel1.setLabelFor(jLogComment);
        jLabel1.setText("Comment:");

        jLogCommentSend.setText("Send");
        jLogCommentSend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendLogTag(evt);
            }
        });

        jLogName.setEditable(true);
        jLogName.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "sokolsky", "anna", "degris", "sutton", "wiewiora", "awhite" }));
        jLogName.setSelectedItem(System.getProperty("user.name"));
        jLogName.setVerifyInputWhenFocusTarget(false);

        jLabel2.setLabelFor(jLogName);
        jLabel2.setText("Name:");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLogName, 0, 175, Short.MAX_VALUE)
                    .addComponent(jLogComment, javax.swing.GroupLayout.DEFAULT_SIZE, 175, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLogCommentSend)
                .addGap(12, 12, 12))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(30, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLogName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLogComment, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLogCommentSend))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void sendLogTag(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendLogTag
        di.sendDrop(new CritterLogTagDrop(jLogName.getSelectedItem().toString(),
                jLogComment.getText()));
        jLogComment.setText("");
    }//GEN-LAST:event_sendLogTag
 
 
    private static String getUserName() {
        return System.getProperty("user.name");
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JTextField jLogComment;
    private javax.swing.JButton jLogCommentSend;
    private javax.swing.JComboBox jLogName;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
    
}