-- NORD POS is a fork of Openbravo POS.
-- Copyright (C) 2009-2016 Nord Trading Ltd. 
-- <http://sourceforge.net/p/nordpos/>
-- This file is part of NORD POS.
-- NORD POS is free software: you can redistribute it and/or modify it under the
-- terms of the GNU General Public License as published by the Free Software
-- Foundation, either version 3 of the License, or (at your option) any later
-- version.
-- NORD POS is distributed in the hope that it will be useful, but WITHOUT ANY
-- WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
-- A PARTICULAR PURPOSE. See the GNU General Public License for more details.
-- You should have received a copy of the GNU General Public License along with
-- NORD POS. If not, see <http://www.gnu.org/licenses/>.

-- Database upgrade script for MYSQL
-- NORD POS v3.0.6CE -> add MULTICOMPONENTS RECEIPES

-- final script

ALTER TABLE PRODUCTS  ADD COLUMN ISCOMPLEX BIT DEFAULT b'0' NOT NULL;
CREATE INDEX ISCOMPLEX_INX_1 ON PRODUCTS(ISCOMPLEX);

CREATE TABLE RECIPES (
	ID                VARCHAR(255) NOT NULL COMMENT 'ID рецепта',
	PRODUCT_ID        VARCHAR(255) NOT NULL COMMENT 'ID комплексного продукта',
	INGREDIENT_ID     VARCHAR(255) NOT NULL COMMENT 'ID ингредиента',
	INGREDIENT_WEIGHT DOUBLE           NULL COMMENT 'Кол-во ингредиента',
	PRIMARY KEY (ID),        
        CONSTRAINT PRODUCT_ID_FK_1    FOREIGN KEY (PRODUCT_ID)    REFERENCES PRODUCTS(ID),
        CONSTRAINT INGREDIENT_ID_FK_1 FOREIGN KEY (INGREDIENT_ID) REFERENCES PRODUCTS(ID)
)
COMMENT='тестовая таблица'
ENGINE=InnoDB
DEFAULT CHARSET=utf8
;