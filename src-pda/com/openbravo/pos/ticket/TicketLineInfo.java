//    Openbravo POS is a point of sales application designed for touch screens.
//    Copyright (C) 2007 Openbravo, S.L.
//    http://sourceforge.net/projects/openbravopos
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

package com.openbravo.pos.ticket;

import com.openbravo.pos.pda.util.FormatUtils;
import java.io.Serializable;
import java.util.Properties;

/**
 *
 * @author jaroslawwozniak
 */
public class TicketLineInfo implements Serializable {

    private static final long serialVersionUID = 6608012948284450199L;
    private String m_sTicket;
    private int m_iLine;
    private TaxInfo tax;
    private double multiply;
    private double price;
    private String productid;
    private Properties attributes;
    private ProductInfo product;
    private double value;

    public TicketLineInfo() {
    }

    public TicketLineInfo(ProductInfo product, double price, TaxInfo tax) {
        this.product = product;
        productid = product.getId();
        this.multiply = 1.0;
        this.price = price;
        this.tax = tax;
        attributes = new Properties();

        m_sTicket = null;
        m_iLine = -1;
        setProperties();
    }

    public void setProperties() {
        attributes.setProperty("product.name", product.getName());
    }

    public Properties getAttributes() {
        return attributes;
    }

    public void setAttributes(Properties attributes) {
        this.attributes = attributes;
    }

    public int getM_iLine() {
        return m_iLine;
    }

    public void setM_iLine(int m_iLine) {
        this.m_iLine = m_iLine;
    }

    public String getM_sTicket() {
        return m_sTicket;
    }

    public void setM_sTicket(String m_sTicket) {
        this.m_sTicket = m_sTicket;
    }

    public double getMultiply() {
        return multiply;
    }

    public void setMultiply(double multiply) {
        this.multiply = multiply;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getProductid() {
        return productid;
    }

    public void setProductid(String productid) {
        this.productid = productid;
    }

    public TaxInfo getTax() {
        return tax;
    }

    public void setTax(TaxInfo tax) {
        this.tax = tax;
    }

    public void setTicket(String ticket, int size) {
        m_sTicket = ticket;
        m_iLine = size;
    }

    public double getValue() {
        return price * multiply;
    }

    public String printPrice() {
        return FormatUtils.formatCurrency(price);
    }

    public String printMultiply() {
        return FormatUtils.formatDouble(multiply);
    }
}