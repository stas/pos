--    Openbravo POS is a point of sales application designed for touch screens.
--    Copyright (C) 2007-2009 Openbravo, S.L.
--    http://sourceforge.net/projects/openbravopos
--
--    This file is part of Openbravo POS.
--
--    Openbravo POS is free software: you can redistribute it and/or modify
--    it under the terms of the GNU General Public License as published by
--    the Free Software Foundation, either version 3 of the License, or
--    (at your option) any later version.
--
--    Openbravo POS is distributed in the hope that it will be useful,
--    but WITHOUT ANY WARRANTY; without even the implied warranty of
--    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
--    GNU General Public License for more details.
--
--    You should have received a copy of the GNU General Public License
--    along with Openbravo POS.  If not, see <http://www.gnu.org/licenses/>.

-- Database upgrade script for MYSQL
-- v2.10 - v2.20

CREATE TABLE TAXCUSTCATEGORIES (
    ID VARCHAR(255) NOT NULL,
    NAME VARCHAR(255) NOT NULL,
    PRIMARY KEY (ID)
);
CREATE UNIQUE INDEX TAXCUSTCAT_NAME_INX ON TAXCUSTCATEGORIES(NAME);

CREATE TABLE TAXCATEGORIES (
    ID VARCHAR(255) NOT NULL,
    NAME VARCHAR(255) NOT NULL,
    PRIMARY KEY (ID)
);
CREATE UNIQUE INDEX TAXCAT_NAME_INX ON TAXCATEGORIES(NAME);
INSERT INTO TAXCATEGORIES(ID, NAME) VALUES ('000', 'Tax Exempt');
INSERT INTO TAXCATEGORIES(ID, NAME) VALUES ('001', 'Tax Standard');
INSERT INTO TAXCATEGORIES (ID, NAME) SELECT ID, NAME FROM TAXES;

ALTER TABLE TAXES ADD COLUMN CATEGORY VARCHAR(255);
ALTER TABLE TAXES ADD COLUMN CUSTCATEGORY VARCHAR(255);
ALTER TABLE TAXES ADD COLUMN PARENTID VARCHAR(255);
ALTER TABLE TAXES ADD COLUMN RATECASCADE BOOLEAN;
ALTER TABLE TAXES ADD COLUMN RATEORDER INTEGER;
ALTER TABLE TAXES ADD CONSTRAINT TAXES_CAT_FK FOREIGN KEY (CATEGORY) REFERENCES TAXCATEGORIES(ID);
ALTER TABLE TAXES ADD CONSTRAINT TAXES_CUSTCAT_FK FOREIGN KEY (CUSTCATEGORY) REFERENCES TAXCUSTCATEGORIES(ID);
ALTER TABLE TAXES ADD CONSTRAINT TAXES_TAXES_FK FOREIGN KEY (PARENTID) REFERENCES TAXES(ID);
UPDATE TAXES SET CATEGORY = ID, RATECASCADE = FALSE;
ALTER TABLE TAXES MODIFY COLUMN CATEGORY VARCHAR(255) NOT NULL;
ALTER TABLE TAXES MODIFY COLUMN RATECASCADE BOOLEAN NOT NULL;

ALTER TABLE PRODUCTS ADD COLUMN TAXCAT VARCHAR(255);
ALTER TABLE PRODUCTS ADD CONSTRAINT PRODUCTS_TAXCAT_FK FOREIGN KEY (TAXCAT) REFERENCES TAXCATEGORIES(ID);
UPDATE PRODUCTS SET TAXCAT = TAX;
ALTER TABLE PRODUCTS MODIFY COLUMN TAXCAT VARCHAR(255) NULL;
ALTER TABLE PRODUCTS DROP FOREIGN KEY PRODUCTS_FK_2;
ALTER TABLE PRODUCTS DROP COLUMN TAX;

ALTER TABLE CUSTOMERS ADD COLUMN SEARCHKEY VARCHAR(255);
UPDATE CUSTOMERS SET SEARCHKEY = ID;
ALTER TABLE CUSTOMERS MODIFY COLUMN SEARCHKEY VARCHAR(255) NOT NULL;
CREATE UNIQUE INDEX CUSTOMERS_SKEY_INX ON CUSTOMERS(SEARCHKEY);

ALTER TABLE CUSTOMERS ADD COLUMN ADDRESS2 VARCHAR(255);
ALTER TABLE CUSTOMERS ADD COLUMN POSTAL VARCHAR(255);
ALTER TABLE CUSTOMERS ADD COLUMN CITY VARCHAR(255);
ALTER TABLE CUSTOMERS ADD COLUMN REGION VARCHAR(255);
ALTER TABLE CUSTOMERS ADD COLUMN COUNTRY VARCHAR(255);
ALTER TABLE CUSTOMERS ADD COLUMN FIRSTNAME VARCHAR(255);
ALTER TABLE CUSTOMERS ADD COLUMN LASTNAME VARCHAR(255);
ALTER TABLE CUSTOMERS ADD COLUMN EMAIL VARCHAR(255);
ALTER TABLE CUSTOMERS ADD COLUMN PHONE VARCHAR(255);
ALTER TABLE CUSTOMERS ADD COLUMN PHONE2 VARCHAR(255);
ALTER TABLE CUSTOMERS ADD COLUMN FAX VARCHAR(255);
ALTER TABLE CUSTOMERS ADD COLUMN TAXCATEGORY VARCHAR(255);
ALTER TABLE CUSTOMERS ADD CONSTRAINT CUSTOMERS_TAXCAT FOREIGN KEY (TAXCATEGORY) REFERENCES TAXCUSTCATEGORIES(ID);

ALTER TABLE CLOSEDCASH ADD COLUMN HOSTSEQUENCE INTEGER;  
UPDATE CLOSEDCASH SET HOSTSEQUENCE = 0;
ALTER TABLE CLOSEDCASH MODIFY COLUMN HOSTSEQUENCE INTEGER NOT NULL;
CREATE INDEX CLOSEDCASH_SEQINX ON CLOSEDCASH(HOST, HOSTSEQUENCE);

ALTER TABLE RECEIPTS ADD COLUMN ATTRIBUTES MEDIUMBLOB;

ALTER TABLE TICKETLINES DROP COLUMN NAME;
ALTER TABLE TICKETLINES DROP COLUMN ISCOM;

CREATE TABLE TAXLINES (
    ID VARCHAR(255) NOT NULL,
    RECEIPT VARCHAR(255) NOT NULL,
    TAXID VARCHAR(255) NOT NULL, 
    BASE DOUBLE NOT NULL, 
    AMOUNT DOUBLE NOT NULL,
    PRIMARY KEY (ID),
    CONSTRAINT TAXLINES_TAX FOREIGN KEY (TAXID) REFERENCES TAXES(ID)
);

UPDATE PEOPLE SET CARD = NULL WHERE CARD = '';

-- v2.20 - v2.30beta

INSERT INTO RESOURCES(ID, NAME, RESTYPE, CONTENT) VALUES('30', 'Printer.PartialCash', 0, $FILE{/com/openbravo/pos/templates/Printer.PartialCash.xml});

-- Charset (UTF-8 Unicode), collation (utf8_general_ci), engine (InnoDB) modification
ALTER TABLE APPLICATIONS ENGINE=InnoDB;
ALTER TABLE CATEGORIES ENGINE=InnoDB;
ALTER TABLE CLOSEDCASH ENGINE=InnoDB;
ALTER TABLE CUSTOMERS ENGINE=InnoDB;
ALTER TABLE FLOORS ENGINE=InnoDB;
ALTER TABLE LOCATIONS ENGINE=InnoDB;
ALTER TABLE PAYMENTS ENGINE=InnoDB;
ALTER TABLE PEOPLE ENGINE=InnoDB;
ALTER TABLE PLACES ENGINE=InnoDB;
ALTER TABLE PRODUCTS ENGINE=InnoDB;
ALTER TABLE PRODUCTS_CAT ENGINE=InnoDB;
ALTER TABLE PRODUCTS_COM ENGINE=InnoDB;
ALTER TABLE RECEIPTS ENGINE=InnoDB;
ALTER TABLE RESERVATIONS ENGINE=InnoDB;
ALTER TABLE RESERVATION_CUSTOMERS ENGINE=InnoDB;
ALTER TABLE RESOURCES ENGINE=InnoDB;
ALTER TABLE ROLES ENGINE=InnoDB;
ALTER TABLE SHAREDTICKETS ENGINE=InnoDB;
ALTER TABLE STOCKCURRENT ENGINE=InnoDB;
ALTER TABLE STOCKDIARY ENGINE=InnoDB;
ALTER TABLE TAXCATEGORIES ENGINE=InnoDB;
ALTER TABLE TAXCUSTCATEGORIES ENGINE=InnoDB;
ALTER TABLE TAXES ENGINE=InnoDB;
ALTER TABLE TAXLINES ENGINE=InnoDB;
ALTER TABLE THIRDPARTIES ENGINE=InnoDB;
ALTER TABLE TICKETLINES ENGINE=InnoDB;
ALTER TABLE TICKETS ENGINE=InnoDB;
ALTER TABLE TICKETSNUM ENGINE=InnoDB;

ALTER TABLE APPLICATIONS CONVERT TO CHARACTER SET UTF8;
ALTER TABLE CATEGORIES CONVERT TO CHARACTER SET UTF8;
ALTER TABLE CLOSEDCASH CONVERT TO CHARACTER SET UTF8;
ALTER TABLE CUSTOMERS CONVERT TO CHARACTER SET UTF8;
ALTER TABLE FLOORS CONVERT TO CHARACTER SET UTF8;
ALTER TABLE LOCATIONS CONVERT TO CHARACTER SET UTF8;
ALTER TABLE PAYMENTS CONVERT TO CHARACTER SET UTF8;
ALTER TABLE PEOPLE CONVERT TO CHARACTER SET UTF8;
ALTER TABLE PLACES CONVERT TO CHARACTER SET UTF8;
ALTER TABLE PRODUCTS CONVERT TO CHARACTER SET UTF8;
ALTER TABLE PRODUCTS_CAT CONVERT TO CHARACTER SET UTF8;
ALTER TABLE PRODUCTS_COM CONVERT TO CHARACTER SET UTF8;
ALTER TABLE RECEIPTS CONVERT TO CHARACTER SET UTF8;
ALTER TABLE RESERVATIONS CONVERT TO CHARACTER SET UTF8;
ALTER TABLE RESERVATION_CUSTOMERS CONVERT TO CHARACTER SET UTF8;
ALTER TABLE RESOURCES CONVERT TO CHARACTER SET UTF8;
ALTER TABLE ROLES CONVERT TO CHARACTER SET UTF8;
ALTER TABLE SHAREDTICKETS CONVERT TO CHARACTER SET UTF8;
ALTER TABLE STOCKCURRENT CONVERT TO CHARACTER SET UTF8;
ALTER TABLE STOCKDIARY CONVERT TO CHARACTER SET UTF8;
ALTER TABLE TAXCATEGORIES CONVERT TO CHARACTER SET UTF8;
ALTER TABLE TAXCUSTCATEGORIES CONVERT TO CHARACTER SET UTF8;
ALTER TABLE TAXES CONVERT TO CHARACTER SET UTF8;
ALTER TABLE TAXLINES CONVERT TO CHARACTER SET UTF8;
ALTER TABLE THIRDPARTIES CONVERT TO CHARACTER SET UTF8;
ALTER TABLE TICKETLINES CONVERT TO CHARACTER SET UTF8;
ALTER TABLE TICKETS CONVERT TO CHARACTER SET UTF8;
ALTER TABLE TICKETSNUM CONVERT TO CHARACTER SET UTF8;

CREATE TABLE _PRODUCTS_COM (
    ID VARCHAR(255) NOT NULL,
    PRODUCT VARCHAR(255) NOT NULL,
    PRODUCT2 VARCHAR(255) NOT NULL,
    PRIMARY KEY (ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO _PRODUCTS_COM(ID, PRODUCT, PRODUCT2) SELECT CONCAT(PRODUCT, PRODUCT2), PRODUCT, PRODUCT2 FROM PRODUCTS_COM;

DROP TABLE PRODUCTS_COM;

CREATE TABLE PRODUCTS_COM (
    ID VARCHAR(255) NOT NULL,
    PRODUCT VARCHAR(255) NOT NULL,
    PRODUCT2 VARCHAR(255) NOT NULL,
    PRIMARY KEY (ID),
    CONSTRAINT PRODUCTS_COM_FK_1 FOREIGN KEY (PRODUCT) REFERENCES PRODUCTS(ID),
    CONSTRAINT PRODUCTS_COM_FK_2 FOREIGN KEY (PRODUCT2) REFERENCES PRODUCTS(ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE UNIQUE INDEX PCOM_INX_PROD ON PRODUCTS_COM(PRODUCT, PRODUCT2);

INSERT INTO PRODUCTS_COM(ID, PRODUCT, PRODUCT2) SELECT ID, PRODUCT, PRODUCT2 FROM _PRODUCTS_COM;

DROP TABLE _PRODUCTS_COM;

ALTER TABLE TICKETS ADD COLUMN TICKETTYPE INTEGER DEFAULT 0 NOT NULL;
DROP INDEX TICKETS_TICKETID ON TICKETS;
CREATE INDEX TICKETS_TICKETID ON TICKETS(TICKETTYPE, TICKETID);

CREATE TABLE TICKETSNUM_REFUND (ID INTEGER NOT NULL) ENGINE=InnoDB DEFAULT CHARSET=utf8;
INSERT INTO TICKETSNUM_REFUND VALUES(1);

CREATE TABLE TICKETSNUM_PAYMENT (ID INTEGER NOT NULL) ENGINE=InnoDB DEFAULT CHARSET=utf8;
INSERT INTO TICKETSNUM_PAYMENT VALUES(1);

ALTER TABLE PAYMENTS ADD COLUMN TRANSID VARCHAR(255);
ALTER TABLE PAYMENTS ADD COLUMN RETURNMSG MEDIUMBLOB;

CREATE TABLE ATTRIBUTE (
    ID VARCHAR(255) NOT NULL,
    NAME VARCHAR(255) NOT NULL,
    PRIMARY KEY (ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE ATTRIBUTEVALUE (
    ID VARCHAR(255) NOT NULL,
    ATTRIBUTE_ID VARCHAR(255) NOT NULL,
    VALUE VARCHAR(255),
    PRIMARY KEY (ID),
    CONSTRAINT ATTVAL_ATT FOREIGN KEY (ATTRIBUTE_ID) REFERENCES ATTRIBUTE(ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE ATTRIBUTESET (
    ID VARCHAR(255) NOT NULL,
    NAME VARCHAR(255) NOT NULL,
    PRIMARY KEY (ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE ATTRIBUTEUSE (
    ID VARCHAR(255) NOT NULL,
    ATTRIBUTESET_ID VARCHAR(255) NOT NULL,
    ATTRIBUTE_ID VARCHAR(255) NOT NULL,
    LINENO INTEGER,
    PRIMARY KEY (ID),
    CONSTRAINT ATTUSE_SET FOREIGN KEY (ATTRIBUTESET_ID) REFERENCES ATTRIBUTESET(ID),
    CONSTRAINT ATTUSE_ATT FOREIGN KEY (ATTRIBUTE_ID) REFERENCES ATTRIBUTE(ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE UNIQUE INDEX ATTUSE_LINE ON ATTRIBUTEUSE(ATTRIBUTESET_ID, LINENO);

CREATE TABLE ATTRIBUTESETINSTANCE (
    ID VARCHAR(255) NOT NULL,
    ATTRIBUTESET_ID VARCHAR(255) NOT NULL,
    DESCRIPTION VARCHAR(255),
    PRIMARY KEY (ID),
    CONSTRAINT ATTSETINST_SET FOREIGN KEY (ATTRIBUTESET_ID) REFERENCES ATTRIBUTESET(ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE ATTRIBUTEINSTANCE (
    ID VARCHAR(255) NOT NULL,
    ATTRIBUTESETINSTANCE_ID VARCHAR(255) NOT NULL,
    ATTRIBUTE_ID VARCHAR(255) NOT NULL,
    VALUE VARCHAR(255),
    PRIMARY KEY (ID),
    CONSTRAINT ATTINST_SET FOREIGN KEY (ATTRIBUTESETINSTANCE_ID) REFERENCES ATTRIBUTESETINSTANCE(ID),
    CONSTRAINT ATTINST_ATT FOREIGN KEY (ATTRIBUTE_ID) REFERENCES ATTRIBUTE(ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE PRODUCTS ADD COLUMN ATTRIBUTESET_ID VARCHAR(255);
ALTER TABLE PRODUCTS ADD CONSTRAINT PRODUCTS_ATTRSET_FK FOREIGN KEY (ATTRIBUTESET_ID) REFERENCES ATTRIBUTESET(ID);

ALTER TABLE STOCKDIARY ADD COLUMN ATTRIBUTESETINSTANCE_ID VARCHAR(255);
ALTER TABLE STOCKDIARY ADD CONSTRAINT STOCKDIARY_ATTSETINST FOREIGN KEY (ATTRIBUTESETINSTANCE_ID) REFERENCES ATTRIBUTESETINSTANCE(ID);

CREATE TABLE STOCKLEVEL (
    ID VARCHAR(255) NOT NULL,
    LOCATION VARCHAR(255) NOT NULL,
    PRODUCT VARCHAR(255) NOT NULL,
    STOCKSECURITY DOUBLE,
    STOCKMAXIMUM DOUBLE,
    PRIMARY KEY (ID),
    CONSTRAINT STOCKLEVEL_PRODUCT FOREIGN KEY (PRODUCT) REFERENCES PRODUCTS(ID),
    CONSTRAINT STOCKLEVEL_LOCATION FOREIGN KEY (LOCATION) REFERENCES LOCATIONS(ID)
 )ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO STOCKLEVEL(ID, LOCATION, PRODUCT, STOCKSECURITY, STOCKMAXIMUM) SELECT CONCAT(LOCATION, PRODUCT), LOCATION, PRODUCT, STOCKSECURITY, STOCKMAXIMUM FROM STOCKCURRENT;

CREATE TABLE _STOCKCURRENT (
    LOCATION VARCHAR(255) NOT NULL,
    PRODUCT VARCHAR(255) NOT NULL,
    ATTRIBUTESETINSTANCE_ID VARCHAR(255),
    UNITS DOUBLE NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO _STOCKCURRENT(LOCATION, PRODUCT, UNITS) SELECT LOCATION, PRODUCT, UNITS FROM STOCKCURRENT;

DROP TABLE STOCKCURRENT;

CREATE TABLE STOCKCURRENT (
    LOCATION VARCHAR(255) NOT NULL,
    PRODUCT VARCHAR(255) NOT NULL,
    ATTRIBUTESETINSTANCE_ID VARCHAR(255),
    UNITS DOUBLE NOT NULL,
    CONSTRAINT STOCKCURRENT_FK_1 FOREIGN KEY (PRODUCT) REFERENCES PRODUCTS(ID),
    CONSTRAINT STOCKCURRENT_ATTSETINST FOREIGN KEY (ATTRIBUTESETINSTANCE_ID) REFERENCES ATTRIBUTESETINSTANCE(ID),
    CONSTRAINT STOCKCURRENT_FK_2 FOREIGN KEY (LOCATION) REFERENCES LOCATIONS(ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE UNIQUE INDEX STOCKCURRENT_INX ON STOCKCURRENT(LOCATION, PRODUCT, ATTRIBUTESETINSTANCE_ID);

INSERT INTO STOCKCURRENT(LOCATION, PRODUCT, UNITS) SELECT LOCATION, PRODUCT, UNITS FROM _STOCKCURRENT;

DROP TABLE _STOCKCURRENT;

ALTER TABLE TICKETLINES ADD COLUMN ATTRIBUTESETINSTANCE_ID VARCHAR(255);
ALTER TABLE TICKETLINES ADD CONSTRAINT TICKETLINES_ATTSETINST FOREIGN KEY (ATTRIBUTESETINSTANCE_ID) REFERENCES ATTRIBUTESETINSTANCE(ID);

-- final script

DELETE FROM SHAREDTICKETS;

UPDATE APPLICATIONS SET NAME = $APP_NAME{}, VERSION = $APP_VERSION{} WHERE ID = $APP_ID{};
