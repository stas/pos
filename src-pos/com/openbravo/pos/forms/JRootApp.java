//    Openbravo POS is a point of sales application designed for touch screens.
//    Copyright (C) 2007 Openbravo, S.L.
//    http://sourceforge.net/projects/
//
//    This program is free software; you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation; either version 2 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program; if not, write to the Free Software
//    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

package com.openbravo.pos.forms;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.*;
import java.lang.reflect.Constructor;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import javax.swing.*;
import javax.imageio.ImageIO;

import com.openbravo.pos.printer.*;

import com.openbravo.beans.*;
import com.openbravo.pos.util.ThumbNailBuilder;

import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.data.gui.JMessageDialog;
import com.openbravo.data.loader.BatchSentence;
import com.openbravo.data.loader.BatchSentenceResource;
import com.openbravo.data.loader.Session;
import com.openbravo.pos.scale.DeviceScale;
import com.openbravo.pos.scanpal2.DeviceScanner;
import com.openbravo.pos.scanpal2.DeviceScannerFactory;
import java.util.regex.Matcher;

/**
 *
 * @author adrianromero
 */
public class JRootApp extends JPanel implements AppView {
 
    private AppProperties m_props;
    private AppViewConnection m_appcnt;     
    private DataLogicSystem m_dlSystem;
    
    private Properties m_propsdb = null;
    private String m_sActiveCashIndex;
    private Date m_dActiveCashDateStart;
    private Date m_dActiveCashDateEnd;
    
    private String m_sInventoryLocation;
   
    private DeviceScale m_Scale;
    private DeviceScanner m_Scanner;
    private DeviceTicket m_TP;   
    private TicketParser m_TTP;
    
    private Map<String, BeanFactory> m_aBeanFactories;
    
    private JPrincipalApp m_principalapp = null;
   
    /** Creates new form JRootApp */
    public JRootApp() {    

        m_aBeanFactories = new HashMap<String, BeanFactory>();
        
        // Inicializo los componentes visuales
        initComponents ();     
        jScrollPane1.getVerticalScrollBar().setPreferredSize(new Dimension(35, 35));   
    }
    
    public boolean initApp(AppProperties props) {
        
        m_props = props;
        
        // Database start
        try {
            m_appcnt = new AppViewConnection(m_props);
        } catch (BasicException e) {
            JMessageDialog.showMessage(this, new MessageInf(MessageInf.SGN_DANGER, e.getMessage(), e));
            return false;
        }

        try {
            m_dlSystem = (DataLogicSystem) getBean("com.openbravo.pos.forms.DataLogicSystemCreate");
        } catch (BeanFactoryException e) {
            JMessageDialog.showMessage(this, new MessageInf(MessageInf.SGN_DANGER, e.getMessage(), e));
            return false;
        }
        
        // Comprobamos si existe la base de datos        
        String sDBVersion = getDataBaseVersion();
        String sScript = m_dlSystem.getInitScript() + "_" + sDBVersion + ".sql";
        
        if (JRootApp.class.getResource(sScript) != null) {
            // hay un script para actualizar o crear la base de datos.
            if (JOptionPane.showConfirmDialog(this
                    , AppLocal.getIntString("create".equals(sDBVersion) ? "message.createdatabase" : "message.updatedatabase")
                    , AppLocal.getIntString("message.title")
                    , JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {  
                
                do { // ejecutamos todos los scripts de upgrade disponibles...
                    try {
                        BatchSentence bsentence = new BatchSentenceResource(m_appcnt.getSession(), sScript);
                        bsentence.putParameter("APP_ID", Matcher.quoteReplacement(AppLocal.APP_ID));
                        bsentence.putParameter("APP_NAME", Matcher.quoteReplacement(AppLocal.APP_NAME));
                        
                        java.util.List l = bsentence.list();
                        if (l.size() > 0) {
                            JMessageDialog.showMessage(this, new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("Database.ScriptWarning"), l.toArray(new Throwable[l.size()])));
                        }
                   } catch (BasicException e) {
                        JMessageDialog.showMessage(this, new MessageInf(MessageInf.SGN_DANGER, AppLocal.getIntString("Database.ScriptError"), e));
                        m_appcnt.disconnect();
                        return false;
                    }     
                    sScript = m_dlSystem.getInitScript() + "_" + getDataBaseVersion() + ".sql";                   
                } while (JRootApp.class.getResource(sScript) != null);
            } else {
                // sin base de datos no hay registradora.
                m_appcnt.disconnect();
                return false;
            }
        }          

        // Cargamos las propiedades de base de datos
        m_propsdb = m_dlSystem.getResourceAsProperties(m_props.getHost() + "/properties");
        if (m_propsdb == null) {
            m_propsdb = new Properties();            
            // Compatibilidad hacia atras
            String soldvalue = m_props.getProperty("machine.activecash");
            if (soldvalue != null) {
                m_propsdb.setProperty("activecash", soldvalue);
            }
        }
        
        // creamos la caja activa si esta no existe      
        try {
            String sActiveCashIndex = m_propsdb.getProperty("activecash");
            Object[] valcash = m_dlSystem.findActiveCash(sActiveCashIndex);
            if (valcash == null || !m_props.getHost().equals(valcash[0])) {
                // no la encuentro o no es de mi host por tanto creo una...
                setActiveCash(UUID.randomUUID().toString(), new Date(), null);

                // creamos la caja activa      
                m_dlSystem.execInsertCash(
                        new Object[] {getActiveCashIndex(), m_props.getHost(), getActiveCashDateStart(), getActiveCashDateEnd()});                  
            } else {
                setActiveCash(sActiveCashIndex, (Date) valcash[1], (Date) valcash[2]);
            }
        } catch (BasicException e) {
            // Casco. Sin caja no hay pos
            MessageInf msg = new MessageInf(MessageInf.SGN_NOTICE, AppLocal.getIntString("message.cannotclosecash"), e);
            msg.show(this);
            m_appcnt.disconnect();
            return false;
        }  
        
        // Leo la localizacion de la caja (Almacen).
        m_sInventoryLocation = m_propsdb.getProperty("location");
        if (m_sInventoryLocation == null) {
            m_sInventoryLocation = "0";
            m_propsdb.setProperty("location", m_sInventoryLocation);
            m_dlSystem.setResourceAsProperties(m_props.getHost() + "/properties", m_propsdb);
        }
        
        // Inicializo la impresora...
        m_TP = new DeviceTicket(m_props);   
        
        // Inicializamos 
        m_TTP = new TicketParser(getDeviceTicket(), m_dlSystem);
        printerStart();
        
        // Inicializamos la bascula
        m_Scale = new DeviceScale(m_props);
        
        // Inicializamos la scanpal
        m_Scanner = DeviceScannerFactory.createInstance(m_props);
        
        // Leemos los recursos basicos
        BufferedImage imgicon = m_dlSystem.getResourceAsImage("Window.Logo");
        m_jLblTitle.setIcon(imgicon == null ? null : new ImageIcon(imgicon));
        m_jLblTitle.setText(m_dlSystem.getResourceAsText("Window.Title"));  
        
        String sWareHouse;
        try {
            sWareHouse = m_dlSystem.findLocationName(m_sInventoryLocation);
        } catch (BasicException e) {
            sWareHouse = null; // no he encontrado el almacen principal
        }        
        m_jHost.setText(sWareHouse + " (" + m_props.getHost() + ")");
        
        // Show Login
        listPeople();
        showView("login");

        return true;
    }
    
    public String getDataBaseVersion() {
        try {
            return m_dlSystem.findVersion();
        } catch (BasicException ed) {
            return "create";
        }
    }
    
    public void tryToClose() {   
        
        if (closeAppView()) {

            // success. continue with the shut down

            // apago el visor
            m_TP.clearVisor();
            // me desconecto de la base de datos.
            m_appcnt.disconnect();

            // Download Root form
            SwingUtilities.getWindowAncestor(this).dispose();
        }
    }
    
    // Interfaz de aplicacion
    public DeviceTicket getDeviceTicket(){
        return m_TP;
    }
    
    public DeviceScale getDeviceScale() {
        return m_Scale;
    }
    public DeviceScanner getDeviceScanner() {
        return m_Scanner;
    }
    
    public Session getSession() {
        return m_appcnt.getSession();
    } 

    public String getInventoryLocation() {
        return m_sInventoryLocation;
    }   
    public String getActiveCashIndex() {
        return m_sActiveCashIndex;
    }
    public Date getActiveCashDateStart() {
        return m_dActiveCashDateStart;
    }
    public Date getActiveCashDateEnd(){
        return m_dActiveCashDateEnd;
    }
    public void setActiveCash(String sIndex, Date dStart, Date dEnd) {
        m_sActiveCashIndex = sIndex;
        m_dActiveCashDateStart = dStart;
        m_dActiveCashDateEnd = dEnd;
        
        m_propsdb.setProperty("activecash", m_sActiveCashIndex);
        m_dlSystem.setResourceAsProperties(m_props.getHost() + "/properties", m_propsdb);
    }   
       
    public AppProperties getProperties() {
        return m_props;
    }
    
    public Object getBean(String beanfactory) throws BeanFactoryException {
        
        BeanFactory bf = m_aBeanFactories.get(beanfactory);
        if (bf == null) {   
            try {
                Class bfclass = Class.forName(beanfactory);

                if (BeanFactoryApp.class.isAssignableFrom(bfclass)) {
                    // the new construction of beans.
                    BeanFactoryApp bfapp = (BeanFactoryApp) bfclass.newInstance();
                    bfapp.init(this);
                    bf = bfapp;
                } else if (BeanFactory.class.isAssignableFrom(bfclass)) {
                    bf = (BeanFactory) bfclass.newInstance();             
                } else {
                    // the old construction for beans...
                    Constructor constMyView = bfclass.getConstructor(new Class[] {AppView.class});
                    Object bean = constMyView.newInstance(new Object[] {this});

                    bf = new BeanFactoryObj(bean);
                }
                m_aBeanFactories.put(beanfactory, bf);
            } catch (Exception e) {
                // ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException
                throw new BeanFactoryException(e);
            }
        }
        return bf.getBean();
    }
    
    public void waitCursorBegin() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }
    
    public void waitCursorEnd(){
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
    
    public AppUserView getAppUserView() {
        return m_principalapp;
    }

    
    private void printerStart() {
        
        String sresource = m_dlSystem.getResourceAsXML("Printer.Start");
        if (sresource == null) {
            m_TP.writeTimeVisor(AppLocal.getIntString("Visor.Title"));
        } else {
            try {
                m_TTP.printTicket(sresource);
            } catch (TicketPrinterException eTP) {
                m_TP.writeTimeVisor(AppLocal.getIntString("Visor.Title"));
            }
        }        
    }
    
    private void listPeople() {
        
        try {
           
            jScrollPane1.getViewport().setView(null);

            JFlowPanel jPeople = new JFlowPanel();
            jPeople.setOpaque(false);
           
            java.util.List people = m_dlSystem.listPeopleVisible();
            
            Image defimg;
            try {
                defimg = ImageIO.read(getClass().getClassLoader().getResourceAsStream("com/openbravo/images/yast_sysadmin.png"));               
            } catch (Exception fnfe) {
                defimg = null;
            }            
            ThumbNailBuilder tnb = new ThumbNailBuilder(32, 32, defimg);
            
            
            for (int i = 0; i < people.size(); i++) {
                Object[] value = (Object[]) people.get(i);
                 
                AppUser user = new AppUser(                       
                        (String) value[0],
                        (String) value[1],
                        (String) value[2],
                        (String) value[3],
                        new ImageIcon(tnb.getThumbNail((Image) value[4])));

                JButton btn = new JButton(new AppUserAction(user));
                btn.setFocusPainted(false);
                btn.setFocusable(false);
                btn.setRequestFocusEnabled(false);
                btn.setHorizontalAlignment(SwingConstants.LEADING);
                // btn.setMargin(new Insets(2, 2, 2, 2));
                btn.setMaximumSize(new Dimension(150, 50));
                btn.setPreferredSize(new Dimension(150, 50));
                btn.setMinimumSize(new Dimension(150, 50));
        
                jPeople.add(btn);    
                
            }
            jScrollPane1.getViewport().setView(jPeople);
            
        } catch (BasicException ee) {
            ee.printStackTrace();
        }
    }
    // La accion del selector
    private class AppUserAction extends AbstractAction {
        
        private AppUser m_actionuser;
        
        public AppUserAction(AppUser user) {
            m_actionuser = user;
            putValue(Action.SMALL_ICON, m_actionuser.getIcon());
            putValue(Action.NAME, m_actionuser.getName());
        }
        
        public AppUser getUser() {
            return m_actionuser;
        }
        
        public void actionPerformed(ActionEvent evt) {
            // String sPassword = m_actionuser.getPassword();
            if (m_actionuser.authenticate()) {
                // p'adentro directo, no tiene password        
                openAppView(m_actionuser);         
            } else {
                // comprobemos la clave antes de entrar...
                String sPassword = JPasswordDialog.showEditPassword(JRootApp.this, 
                        AppLocal.getIntString("Label.Password"),
                        m_actionuser.getName(),
                        m_actionuser.getIcon());
                if (sPassword != null) {
                    if (m_actionuser.authenticate(sPassword)) {
                        openAppView(m_actionuser);                
                    } else {
                        JOptionPane.showMessageDialog(JRootApp.this,
                                AppLocal.getIntString("message.BadPassword"),
                                m_actionuser.getName(),
                                JOptionPane.WARNING_MESSAGE);
                    }
                }   
            }
        }
    }
    
    private void showView(String view) {
        CardLayout cl = (CardLayout)(m_jPanelContainer.getLayout());
        cl.show(m_jPanelContainer, view);  
    }
    
    private void openAppView(AppUser user) {
        
        if (closeAppView()) {

            // creo el app quiza no deberia crearla si a existe 
            m_principalapp = new JPrincipalApp(this, user);

            // el indicador
            m_jPanelDown.add(m_principalapp.getNotificator());
            m_jPanelDown.revalidate();
            // el panel principal
            m_jPanelContainer.add(m_principalapp, "_" + m_principalapp.getUser().getId());
            showView("_" + m_principalapp.getUser().getId());

            m_principalapp.activate();
        }
    }
       
    public boolean closeAppView() {
        
        if (m_principalapp == null) {
            return true;
        } else if (!m_principalapp.deactivate()) {
            return false;
        } else {
            // the status label
            m_jPanelDown.remove(m_principalapp.getNotificator());
            m_jPanelDown.revalidate();
            m_jPanelDown.repaint();

            // remove the card
            m_jPanelContainer.remove(m_principalapp);
            m_principalapp = null;

            // Show Login
            listPeople();
            showView("login");     

            // show welcome message
            printerStart();        
            return true;
        }
    }

        
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        m_jPanelTitle = new javax.swing.JPanel();
        m_jLblTitle = new javax.swing.JLabel();
        m_jPanelDown = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        m_jHost = new javax.swing.JLabel();
        m_jPanelContainer = new javax.swing.JPanel();
        m_jPanelLogin = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        m_jLogonName = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel2 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        m_jClose = new javax.swing.JButton();

        setPreferredSize(new java.awt.Dimension(1024, 768));
        setLayout(new java.awt.BorderLayout());

        m_jPanelTitle.setBackground(javax.swing.UIManager.getDefaults().getColor("InternalFrame.activeTitleBackground"));

        m_jLblTitle.setForeground(javax.swing.UIManager.getDefaults().getColor("InternalFrame.activeTitleForeground"));
        m_jLblTitle.setText("Window.Title");
        m_jPanelTitle.add(m_jLblTitle);

        add(m_jPanelTitle, java.awt.BorderLayout.NORTH);

        m_jPanelDown.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jPanel3.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.lightGray), javax.swing.BorderFactory.createEmptyBorder(1, 5, 1, 5)));
        jPanel3.setLayout(new java.awt.BorderLayout());

        m_jHost.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/display.png"))); // NOI18N
        m_jHost.setText("*Hostname");
        jPanel3.add(m_jHost, java.awt.BorderLayout.CENTER);

        m_jPanelDown.add(jPanel3);

        add(m_jPanelDown, java.awt.BorderLayout.SOUTH);

        m_jPanelContainer.setLayout(new java.awt.CardLayout());

        m_jPanelLogin.setLayout(new java.awt.BorderLayout());

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/logo.jpg"))); // NOI18N
        jLabel1.setText("<html><center>Openbravo POS is a point of sale application designed for touch screens.<br>" +
            "Copyright \u00A9 2007-2008 Openbravo, S.L.<br>" +
            "http://www.openbravo.com<br>" +
            "<br>" +
            "This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.<br>" +
            "<br>" +
            "This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.<br>" +
            "<br>" +
            "You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA<br>" +
            "</center>"
        );
        jLabel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 100, 10, 100));
        jLabel1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        m_jPanelLogin.add(jLabel1, java.awt.BorderLayout.CENTER);

        m_jLogonName.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        m_jLogonName.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        jScrollPane1.setPreferredSize(new java.awt.Dimension(510, 118));
        m_jLogonName.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 5, 0, 0));
        jPanel2.setLayout(new java.awt.BorderLayout());

        jPanel8.setLayout(new java.awt.GridLayout(0, 1, 5, 5));

        m_jClose.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/exit.png"))); // NOI18N
        m_jClose.setText(AppLocal.getIntString("Button.Close")); // NOI18N
        m_jClose.setFocusPainted(false);
        m_jClose.setFocusable(false);
        m_jClose.setPreferredSize(new java.awt.Dimension(115, 35));
        m_jClose.setRequestFocusEnabled(false);
        m_jClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jCloseActionPerformed(evt);
            }
        });
        jPanel8.add(m_jClose);

        jPanel2.add(jPanel8, java.awt.BorderLayout.NORTH);

        m_jLogonName.add(jPanel2, java.awt.BorderLayout.EAST);

        jPanel5.add(m_jLogonName);

        m_jPanelLogin.add(jPanel5, java.awt.BorderLayout.SOUTH);

        m_jPanelContainer.add(m_jPanelLogin, "login");

        add(m_jPanelContainer, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    private void m_jCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jCloseActionPerformed

        tryToClose();
        
    }//GEN-LAST:event_m_jCloseActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton m_jClose;
    private javax.swing.JLabel m_jHost;
    private javax.swing.JLabel m_jLblTitle;
    private javax.swing.JPanel m_jLogonName;
    private javax.swing.JPanel m_jPanelContainer;
    private javax.swing.JPanel m_jPanelDown;
    private javax.swing.JPanel m_jPanelLogin;
    private javax.swing.JPanel m_jPanelTitle;
    // End of variables declaration//GEN-END:variables
}