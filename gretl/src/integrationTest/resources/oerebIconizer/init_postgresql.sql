GRANT ALL PRIVILEGES ON DATABASE gretl TO ddluser;
CREATE ROLE dmluser WITH LOGIN PASSWORD 'dmluser';
CREATE ROLE readeruser WITH LOGIN PASSWORD 'readeruser';
ALTER DATABASE gretl SET postgis.gdal_enabled_drivers TO 'GTiff PNG JPEG';

CREATE SCHEMA agi_oereb AUTHORIZATION ddluser;
GRANT USAGE ON SCHEMA agi_oereb TO dmluser;

CREATE TABLE agi_oereb.transferstruktur_legendeeintrag (
    t_id bigserial NOT NULL,
    t_seq int8 NULL,
    symbol bytea NULL, 
    legendetext text NULL,
    legendetext_de text NULL,
    legendetext_fr text NULL,
    legendetext_rm text NULL,
    legendetext_it text NULL,
    legendetext_en text NULL,
    artcode varchar(40) NOT NULL, 
    artcodeliste varchar(1023) NOT NULL,
    thema varchar(255) NOT NULL, 
    subthema varchar(60) NULL, 
    weiteresthema varchar(120) NULL, 
    transfrstrkstllngsdnst_legende int8 NULL, 
    CONSTRAINT transferstruktur_legendeeintrag_pkey PRIMARY KEY (t_id)
);
ALTER TABLE agi_oereb.transferstruktur_legendeeintrag OWNER TO ddluser;
GRANT ALL ON TABLE agi_oereb.transferstruktur_legendeeintrag TO ddluser;
GRANT ALL ON TABLE agi_oereb.transferstruktur_legendeeintrag TO dmluser;

INSERT INTO agi_oereb.transferstruktur_legendeeintrag (t_id,t_seq,symbol,legendetext,legendetext_de,legendetext_fr,legendetext_rm,legendetext_it,legendetext_en,artcode,artcodeliste,thema,subthema,weiteresthema,transfrstrkstllngsdnst_legende) VALUES 
(844,0,NULL,NULL,'Fubar',NULL,NULL,NULL,NULL,'N111','https://www.so.ch/fubar.xml','Nutzungsplanung',NULL,NULL,843);

CREATE TABLE agi_oereb.transferstruktur_legendeeintrag_kommunal (
    t_id bigserial NOT NULL,
    t_seq int8 NULL,
    symbol bytea NULL, 
    legendetext text NULL,
    legendetext_de text NULL,
    legendetext_fr text NULL,
    legendetext_rm text NULL,
    legendetext_it text NULL,
    legendetext_en text NULL,
    artcode varchar(40) NOT NULL, 
    artcodeliste varchar(1023) NOT NULL,
    thema varchar(255) NOT NULL, 
    subthema varchar(60) NULL, 
    weiteresthema varchar(120) NULL, 
    transfrstrkstllngsdnst_legende int8 NULL, 
    CONSTRAINT transferstruktur_legendeeintrag_kommunal_pkey PRIMARY KEY (t_id)
);
ALTER TABLE agi_oereb.transferstruktur_legendeeintrag_kommunal OWNER TO ddluser;
GRANT ALL ON TABLE agi_oereb.transferstruktur_legendeeintrag_kommunal TO ddluser;
GRANT ALL ON TABLE agi_oereb.transferstruktur_legendeeintrag_kommunal TO dmluser;

INSERT INTO agi_oereb.transferstruktur_legendeeintrag_kommunal (t_id,t_seq,symbol,legendetext,legendetext_de,legendetext_fr,legendetext_rm,legendetext_it,legendetext_en,artcode,artcodeliste,thema,subthema,weiteresthema,transfrstrkstllngsdnst_legende) VALUES 
(844,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'1111','https://www.so.ch/fubar.xml','Nutzungsplanung',NULL,NULL,843);