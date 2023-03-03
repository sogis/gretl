<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:myns="ch.so.agi" xmlns:eCH-0132="http://www.ech.ch/xmlns/eCH-0132/3" xmlns:eCH-0129="http://www.ech.ch/xmlns/eCH-0129/6" xmlns:eCH-0058="http://www.ech.ch/xmlns/eCH-0058/5" xmlns:eCH-0010="http://www.ech.ch/xmlns/eCH-0010/6" exclude-result-prefixes="myns eCH-0132 eCH-0129 eCH-0058 eCH-0010" version="3.0"> 
    <xsl:output method="xml" indent="yes"/>
    
    <xsl:key name="myns:lookup-eventType" match="myns:data" use="@key" />
    <xsl:variable name="myns:eventType-lookup">
        <myns:data key="3" value="Neubau" />
        <myns:data key="4" value="Anbau" />
        <myns:data key="5" value="Umbau" />
        <myns:data key="6" value="Teilabbruch" />
        <myns:data key="11" value="Wiederaufnahme" />
        <myns:data key="16" value="Trennung" />
        <myns:data key="17" value="Vereinigung" />
        <myns:data key="18" value="Entlassung" />
        <myns:data key="19" value="Loeschung.Abbruch" />
        <myns:data key="20" value="Loeschung.Vereinigung" />
        <myns:data key="21" value="Loeschung.Schaden" />
        <myns:data key="26" value="Neuaufnahme" />
    </xsl:variable>

    <xsl:key name="myns:lookup-buildingCategoryType" match="myns:data" use="@key" />
    <xsl:variable name="myns:buildingCategoryType-lookup">
        <myns:data key="1010" value="Provisorische Unterkunft" />
        <myns:data key="1020" value="Reine Wohngebäude (Wohnnutzung ausschliesslich)" />
        <myns:data key="1030" value="Wohngebäude mit Nebennutzung" />
        <myns:data key="1040" value="Gebäude mit teilweiser Wohnnutzung" />
        <myns:data key="1060" value="Gebäude ohne Wohnnutzung" />
        <myns:data key="1080" value="Sonderbau" />
    </xsl:variable>


    <xsl:template match="/eCH-0132:delivery">
        <TRANSFER xmlns="http://www.interlis.ch/INTERLIS2.3">
        <HEADERSECTION SENDER="eCH0132_to_xtf" VERSION="2.3">
            <MODELS>
            <MODEL NAME="SO_AGI_SGV_Meldungen_20221109" VERSION="2022-11-09" URI="https://agi.so.ch"/>
            </MODELS>
        </HEADERSECTION>

        <!-- TODO 
        * Versicherungsbeginn: falsch im XML (metaDataName) oder fehlt gänzlich. Sollte mandatory sein.
        * Umgang mit den verschiednene eventType in der Transformation: sind die immer sehr ähnlich? fehlt einfach was? (-> zusätliche if prüfung)
        -->

        <!-- Bemerkungen
        * Pro Meldung (z.B. newInsuranceValue) sind mehrere "buidlingInformationType" möglich. Dort sind dann wieder mehrere Grundstücke möglich. 
          Wie mache ich das grundsätzlich und wie mit XSLT? (-> TID? position()?)
          - pro building und pro Grundstück ein INTERLIS-Objekt?
          - jedoch werden immer alle Eingänge (buildingEntranceInformation) allen INTERLIS-Objekte zugewiesen
          -> Ich nehme nur jeweils das erste Element? Nachfragen bei SGV.
        * Fehlt EGID? Gemäss SGV führen sie diesen nicht.

        * Gemeinde wird nicht geliefert. Dünkt mich. Wir könntes sie mit einem Update updaten (nache dem Import oder beim Transfer in Pub)
        -->

        <DATASECTION>
            <SO_AGI_SGV_Meldungen_20221109.Meldungen BID="SO_AGI_SGV_Meldungen_20221109.Meldungen">

                <xsl:message>Hallo Delivery</xsl:message>

                <xsl:apply-templates select="eCH-0132:newInsuranceValue | eCH-0132:cancellation" /> 

            </SO_AGI_SGV_Meldungen_20221109.Meldungen>

        </DATASECTION>
        </TRANSFER>
    </xsl:template>

    <xsl:template match="eCH-0132:newInsuranceValue | eCH-0132:cancellation">
        <xsl:message>Hallo newInsuranceValue or cancellation</xsl:message>

        <SO_AGI_SGV_Meldungen_20221109.Meldungen.Meldung xmlns="http://www.interlis.ch/INTERLIS2.3" TID="1">
            <xsl:if test="eCH-0132:buildingInformation[1]/eCH-0132:building[1]/eCH-0129:coordinates">
                <Lage xmlns="http://www.interlis.ch/INTERLIS2.3">
                    <COORD xmlns="http://www.interlis.ch/INTERLIS2.3">
                        <C1 xmlns="http://www.interlis.ch/INTERLIS2.3">
                            <xsl:value-of select="eCH-0132:buildingInformation[1]/eCH-0132:building[1]/eCH-0129:coordinates/eCH-0129:east" />
                        </C1>
                        <C2 xmlns="http://www.interlis.ch/INTERLIS2.3">
                            <xsl:value-of select="eCH-0132:buildingInformation[1]/eCH-0132:building[1]/eCH-0129:coordinates/eCH-0129:north" />                        
                        </C2>
                    </COORD>
                </Lage>
            </xsl:if>

            <Grundstuecksnummer xmlns="http://www.interlis.ch/INTERLIS2.3">
                <xsl:value-of select="number(tokenize(eCH-0132:buildingInformation[1]/eCH-0132:realestate[1]/eCH-0129:realestateIdentification/eCH-0129:number, '-')[last()])" />
            </Grundstuecksnummer>

            <EGRID xmlns="http://www.interlis.ch/INTERLIS2.3">
                <xsl:value-of select="eCH-0132:buildingInformation[1]/eCH-0132:realestate[1]/eCH-0129:realestateIdentification/eCH-0129:EGRID" />
            </EGRID>

            <NBIdent xmlns="http://www.interlis.ch/INTERLIS2.3">
                <xsl:value-of select="eCH-0132:buildingInformation[1]/eCH-0132:realestate[1]/eCH-0129:namedMetaData/eCH-0129:metaDataName[text() = 'NBIdent']/following-sibling::eCH-0129:metaDataValue" />
            </NBIdent>

            <Datum_Meldung xmlns="http://www.interlis.ch/INTERLIS2.3">
                <xsl:value-of select="format-date(current-date(),'[Y0001]-[M01]-[D01]')"/>
            </Datum_Meldung>

            <Meldegrund xmlns="http://www.interlis.ch/INTERLIS2.3">
                <xsl:value-of select="key('myns:lookup-eventType', eCH-0132:event, $myns:eventType-lookup)/@value"/>
            </Meldegrund>

            <Baujahr xmlns="http://www.interlis.ch/INTERLIS2.3">
                <xsl:value-of select="eCH-0132:buildingInformation[1]/eCH-0132:building[1]/eCH-0129:dateOfConstruction/eCH-0129:year" />
            </Baujahr>

            <Gebaeudebezeichnung xmlns="http://www.interlis.ch/INTERLIS2.3">
                <xsl:value-of select="key('myns:lookup-eventType', eCH-0132:buildingInformation[1]/eCH-0132:building[1]/eCH-0129:buildingCategory, $myns:buildingCategoryType-lookup)/@value"/>
            </Gebaeudebezeichnung>

            <Gebaeudeadresse xmlns="http://www.interlis.ch/INTERLIS2.3">
                <xsl:value-of select="eCH-0132:buildingInformation[1]/eCH-0132:buildingEntranceInformation[1]/eCH-0132:localisationInformation/eCH-0132:street/eCH-0129:description/eCH-0129:descriptionLong" />
                <xsl:text>&#x20;</xsl:text>
                <xsl:value-of select="eCH-0132:buildingInformation[1]/eCH-0132:buildingEntranceInformation[1]/eCH-0132:buildingEntrance/eCH-0129:buildingEntranceNo" />
                <xsl:text>,&#x20;</xsl:text>
                <xsl:value-of select="eCH-0132:buildingInformation[1]/eCH-0132:buildingEntranceInformation[1]/eCH-0132:localisationInformation/eCH-0132:locality/eCH-0129:swissZipCode" />
                <xsl:text>&#x20;</xsl:text>
                <xsl:value-of select="eCH-0132:buildingInformation[1]/eCH-0132:buildingEntranceInformation[1]/eCH-0132:localisationInformation/eCH-0132:locality/eCH-0129:name/eCH-0129:nameLong" />
            </Gebaeudeadresse>

            <Versicherungsbeginn xmlns="http://www.interlis.ch/INTERLIS2.3">
                <xsl:value-of select="eCH-0132:insuranceValue/eCH-0129:validFrom" />
            </Versicherungsbeginn>

            <Verwalter xmlns="http://www.interlis.ch/INTERLIS2.3">
                <xsl:call-template name="custodianOrPolicyholder">
                    <xsl:with-param name="address" select="eCH-0132:custodian/eCH-0132:mailAddress" />
                </xsl:call-template>
            </Verwalter>

            <Eigentuemer xmlns="http://www.interlis.ch/INTERLIS2.3">
                <xsl:choose>
                    <xsl:when test="eCH-0132:policyholder/eCH-0132:mailAddress">
                        <xsl:for-each select="eCH-0132:policyholder/eCH-0132:mailAddress">
                            <xsl:call-template name="custodianOrPolicyholder">
                                <xsl:with-param name="address" select="." />
                            </xsl:call-template>
                            <xsl:if test="position() != last()">
                                <xsl:text>&#x20;/&#x20;</xsl:text>
                            </xsl:if>
                        </xsl:for-each>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:text>DUMMY</xsl:text>
                    </xsl:otherwise>
                </xsl:choose>
            </Eigentuemer>

            <Baulicher_Mehrwert xmlns="http://www.interlis.ch/INTERLIS2.3">
                <xsl:value-of select="eCH-0132:buildingInformation[1]/eCH-0132:building[1]/eCH-0129:namedMetaData/eCH-0129:metaDataName[text() = 'benefit']/following-sibling::eCH-0129:metaDataValue" />
            </Baulicher_Mehrwert>

            <Status xmlns="http://www.interlis.ch/INTERLIS2.3">
                <xsl:text>neu</xsl:text>
            </Status>

            <MessageId xmlns="http://www.interlis.ch/INTERLIS2.3">
                <xsl:value-of select="/eCH-0132:delivery/eCH-0132:deliveryHeader/eCH-0058:messageId" />
            </MessageId>

            <InsuranceObjectId xmlns="http://www.interlis.ch/INTERLIS2.3">
                <xsl:value-of select="eCH-0132:insuranceObject/eCH-0129:insuranceNumber" />
            </InsuranceObjectId>

        </SO_AGI_SGV_Meldungen_20221109.Meldungen.Meldung>
    </xsl:template>

    <xsl:template name="custodianOrPolicyholder">
        <xsl:param name="address" />
        <xsl:choose>
            <xsl:when test="$address/eCH-0010:organisation">
                <xsl:value-of select="$address/eCH-0010:organisation/eCH-0010:organisationName" />
                <xsl:text>,&#x20;</xsl:text>
            </xsl:when>
            <xsl:when test="$address/eCH-0010:person">
                <xsl:value-of select="$address/eCH-0010:person/eCH-0010:firstName" />
                <xsl:text>&#x20;</xsl:text>
                <xsl:value-of select="$address/eCH-0010:person/eCH-0010:lastName" />
                <xsl:text>,&#x20;</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:text>DUMMY</xsl:text>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:value-of select="$address/eCH-0010:addressInformation/eCH-0010:street" />
        <xsl:text>&#x20;</xsl:text>
        <xsl:value-of select="$address/eCH-0010:addressInformation/eCH-0010:houseNumber" />
        <xsl:text>,&#x20;</xsl:text>
        <xsl:value-of select="$address/eCH-0010:addressInformation/eCH-0010:swissZipCode" />
        <xsl:text>&#x20;</xsl:text>
        <xsl:value-of select="$address/eCH-0010:addressInformation/eCH-0010:town" />
    </xsl:template>

</xsl:stylesheet>