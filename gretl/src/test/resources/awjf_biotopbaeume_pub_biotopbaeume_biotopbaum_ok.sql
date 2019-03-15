SELECT
    biotopbaum.geometrie,
    baum_id,
    CASE
        WHEN baumkategorie = 'Seltene_Baumart'
            THEN 'Seltene Baumart'
        WHEN baumkategorie = 'Stehendes_Totholz'
            THEN 'Stehendes Totholz'
        ELSE baumkategorie
    END AS baumkategorie,
    inventur_jahr,
    wirtschaftszone,
    gesuchsnummer,
    waldeigentuemer_code AS waldeigentuemer,
    CASE
        WHEN baumart = 'Laerche'
            THEN 'Lärche'
        WHEN baumart = 'Bergfoehre'
            THEN 'Bergföhre'
        WHEN baumart = 'Schwarzfoehre'
            THEN 'Schwarzföhre'
        WHEN baumart = 'Weymouthsfoehre'
            THEN 'Weymouthsföhre'
        WHEN baumart = 'Waldfoehre'
            THEN 'Waldföhre'
        WHEN baumart = 'Uebrige_Nadelbaeume'
            THEN 'Übrige Nadelbäume'
        WHEN baumart = 'Schneeballblaettriger_Ahorn'
            THEN 'Schneeballblättriger Ahorn'
        WHEN baumart = 'Uebrige_Laubbaeume'
            THEN 'Übrige Laubbäume'
        ELSE baumart 
    END AS baumart, 
    bhd, 
    baumhoehe, 
    CASE
        WHEN merkmal_1 = 'm1_Stammdurchmesser_70'
            THEN 'Stammdurchmesser ≥ 70cm'
        WHEN merkmal_1 = 'm2_Spechtloecher_Bruthoehlen_Wurzelhoehlen'
            THEN 'Bäume mit Spechtlöchern, Bruthöhlen oder Wurzelhöhlen'
        WHEN merkmal_1 = 'm3_Horstbaeume'
            THEN 'Horstbäume'
        WHEN merkmal_1 = 'm4_Sitz_Schlafbaeume_Auerwilds'
            THEN 'Sitz- und Schlafbäume des Auerwilds'
        WHEN merkmal_1 = 'm5_Alte_ehemalige_Weidebaeume'
            THEN 'Alte, ehemalige Weidebäume im Bestandesinnern, besondere Überhälter'
        WHEN merkmal_1 = 'm6_Lebende_Baeume_Efeu_Mistelbewuchs'
            THEN 'Lebende Bäume mit starkem Efeu- oder Mistelbewuchs'
        WHEN merkmal_1 = 'm7_Baeume_mit_markanten_Schaeden'
            THEN 'Bäume mit markanten Schäden'
        WHEN merkmal_1 = 'm8_Baeume_mit_besonderem_Wuchs'
            THEN 'Bäume mit besonderem Wuchs'
        WHEN merkmal_1 = 'm20_Stehendes_Totholz'
            THEN 'Stehendes Totholz'
        WHEN merkmal_1 = 'm21_Seltene_Baumart'
            THEN 'Seltene Baumart'
        WHEN merkmal_1 = 'm22_Gesellschaftsbaum'
            THEN 'Gesellschaftsbaum'
        WHEN merkmal_1 = 'm23_Potenzieller_Biotopbaum'
            THEN 'Potenzieller Biotopbaum'
        ELSE merkmal_1
    END AS merkmal_1,
    beschreibung_merkmal_1,
    CASE
        WHEN merkmal_2 = 'm1_Stammdurchmesser_70'
            THEN 'Stammdurchmesser ≥ 70cm'
        WHEN merkmal_2 = 'm2_Spechtloecher_Bruthoehlen_Wurzelhoehlen'
            THEN 'Bäume mit Spechtlöchern, Bruthöhlen oder Wurzelhöhlen'
        WHEN merkmal_2 = 'm3_Horstbaeume'
            THEN 'Horstbäume'
        WHEN merkmal_2 = 'm4_Sitz_Schlafbaeume_Auerwilds'
            THEN 'Sitz- und Schlafbäume des Auerwilds'
        WHEN merkmal_2 = 'm5_Alte_ehemalige_Weidebaeume'
            THEN 'Alte, ehemalige Weidebäume im Bestandesinnern, besondere Überhälter'
        WHEN merkmal_2 = 'm6_Lebende_Baeume_Efeu_Mistelbewuchs'
            THEN 'Lebende Bäume mit starkem Efeu- oder Mistelbewuchs'
        WHEN merkmal_2 = 'm7_Baeume_mit_markanten_Schaeden'
            THEN 'Bäume mit markanten Schäden'
        WHEN merkmal_2 = 'm8_Baeume_mit_besonderem_Wuchs'
            THEN 'Bäume mit besonderem Wuchs'
        WHEN merkmal_2 = 'm20_Stehendes_Totholz'
            THEN 'Stehendes Totholz'
        WHEN merkmal_2 = 'm21_Seltene_Baumart'
            THEN 'Seltene Baumart'
        WHEN merkmal_2 = 'm22_Gesellschaftsbaum'
            THEN 'Gesellschaftsbaum'
        WHEN merkmal_2 = 'm23_Potenzieller_Biotopbaum'
            THEN 'Potenzieller Biotopbaum'
        ELSE merkmal_2
    END AS merkmal_2,
    beschreibung_merkmal_2,
    CASE
        WHEN merkmal_3 = 'm1_Stammdurchmesser_70'
            THEN 'Stammdurchmesser ≥ 70cm'
        WHEN merkmal_3 = 'm2_Spechtloecher_Bruthoehlen_Wurzelhoehlen'
            THEN 'Bäume mit Spechtlöchern, Bruthöhlen oder Wurzelhöhlen'
        WHEN merkmal_3 = 'm3_Horstbaeume'
            THEN 'Horstbäume'
        WHEN merkmal_3 = 'm4_Sitz_Schlafbaeume_Auerwilds'
            THEN 'Sitz- und Schlafbäume des Auerwilds'
        WHEN merkmal_3 = 'm5_Alte_ehemalige_Weidebaeume'
            THEN 'Alte, ehemalige Weidebäume im Bestandesinnern, besondere Überhälter'
        WHEN merkmal_3 = 'm6_Lebende_Baeume_Efeu_Mistelbewuchs'
            THEN 'Lebende Bäume mit starkem Efeu- oder Mistelbewuchs'
        WHEN merkmal_3 = 'm7_Baeume_mit_markanten_Schaeden'
            THEN 'Bäume mit markanten Schäden'
        WHEN merkmal_3 = 'm8_Baeume_mit_besonderem_Wuchs'
            THEN 'Bäume mit besonderem Wuchs'
        WHEN merkmal_3 = 'm20_Stehendes_Totholz'
            THEN 'Stehendes Totholz'
        WHEN merkmal_3 = 'm21_Seltene_Baumart'
            THEN 'Seltene Baumart'
        WHEN merkmal_3 = 'm22_Gesellschaftsbaum'
            THEN 'Gesellschaftsbaum'
        WHEN merkmal_3 = 'm23_Potenzieller_Biotopbaum'
            THEN 'Potenzieller Biotopbaum'
        ELSE merkmal_3
    END AS merkmal_3,
    beschreibung_merkmal_3,
    massnahmen,
    besonderheiten,
    biotopflaeche AS biotopbaumflaeche,
    bemerkungen,
    CASE
        WHEN tp_inventar = 'TP_aber_nicht_Inventar_nicht_im_TP'
            THEN 'TP aber nicht Inventar, die nicht im TP sind'
        WHEN tp_inventar = 'TP_und_Inventar'
            THEN 'TP und Inventar'
        WHEN tp_inventar = 'Nicht_TP_aber_Inventar'
            THEN 'Nicht TP, aber Inventar inklusive TP im Inventar'
    END AS tp_inventar,
    auszahlung_beitrag,
    auszahlung_beitrag_jahr,
    forstkreis.aname AS forstkreis,
    forstrevier.aname AS forstrevier,
    CASE
        WHEN 
            kanton.geometrie && biotopbaum.geometrie
            AND
            ST_Contains(kanton.geometrie, biotopbaum.geometrie)
                THEN kanton.kantonskuerzel
        WHEN gemeindegrenze_ausserkantonal.kanton = 'Basel-Landschaft'
            THEN 'BL'
        WHEN gemeindegrenze_ausserkantonal.kanton = 'Bern'
            THEN 'BE'
    END AS kanton,
    CASE
        WHEN 
            kanton.geometrie && biotopbaum.geometrie
            AND
            ST_Contains(kanton.geometrie, biotopbaum.geometrie)
                THEN  gemeindegrenze.bfs_gemeindenummer
        ELSE gemeindegrenze_ausserkantonal.bfs_nummer
    END AS gemeindenummer,
    flurname.name AS flurname,
    round(ST_X(biotopbaum.geometrie)::NUMERIC, 0) AS x_koordinate,
    round(ST_Y(biotopbaum.geometrie)::NUMERIC, 0) AS y_koordinate,
    waldgesellschaft.ges_neu AS waldgesellschaft,
    ablage AS foto
FROM
    awjf_biotopbaeume.biotopbaeume_biotopbaum biotopbaum
    LEFT JOIN awjf_forstreviere.forstreviere_forstreviergeometrie AS forstgeometrie
        ON
            forstgeometrie.geometrie && biotopbaum.geometrie
            AND
            ST_Contains(forstgeometrie.geometrie, biotopbaum.geometrie)
    LEFT JOIN awjf_forstreviere.forstreviere_forstrevier AS forstrevier
        ON
            forstgeometrie.forstrevier = forstrevier.t_id
    LEFT JOIN awjf_forstreviere.forstreviere_forstkreis AS forstkreis
        ON
            forstgeometrie.forstkreis = forstkreis.t_id
    LEFT JOIN agi_hoheitsgrenzen_pub.hoheitsgrenzen_kantonsgrenze AS kanton
        ON
            kanton.geometrie && biotopbaum.geometrie
            AND 
            ST_Contains(kanton.geometrie, biotopbaum.geometrie)
    LEFT JOIN agi_hoheitsgrenzen_pub.hoheitsgrenzen_gemeindegrenze AS gemeindegrenze
        ON
            gemeindegrenze.geometrie && biotopbaum.geometrie
            AND
            ST_Contains(gemeindegrenze.geometrie, biotopbaum.geometrie)
    LEFT JOIN agi_swissboundaries3d_pub.swissboundaries3d_hoheitsgebiet AS gemeindegrenze_ausserkantonal
        ON
            gemeindegrenze_ausserkantonal.geometrie && biotopbaum.geometrie
            AND
            ST_Contains(gemeindegrenze_ausserkantonal.geometrie, biotopbaum.geometrie)
            AND 
            gemeindegrenze_ausserkantonal.kanton <> 'Solothurn'
    LEFT JOIN avdpool.flurn AS flurname
        ON
            flurname.wkb_geometry && biotopbaum.geometrie
            AND
            ST_Contains(flurname.wkb_geometry, biotopbaum.geometrie)
            AND
            flurname.archive = 0
    LEFT JOIN  awjf.wap_bst AS waldplan                   -- Waldplan noch nicht vollständig (Stand 17.01.2018)
        ON
            waldplan.wkb_geometry && biotopbaum.geometrie
            AND
            ST_Contains(waldplan.wkb_geometry, biotopbaum.geometrie)
            AND 
            waldplan.archive = 0
    LEFT JOIN awjf.waldge AS waldgesellschaft
        ON
            waldgesellschaft.wkb_geometry && biotopbaum.geometrie
            AND
            ST_Contains(waldgesellschaft.wkb_geometry, biotopbaum.geometrie)
            AND
            waldgesellschaft.archive = 0
    LEFT JOIN
        (
            SELECT 
                TRIM(BOTH ', ' FROM string_agg(ablage, ', ')) AS ablage, 
                biotopbaum
            FROM (
                    SELECT 
                        ablage, 
                        biotopbaum
                    FROM
                        awjf_biotopbaeume.biotopbaeume_foto
                ) AS foto,
                awjf_biotopbaeume.biotopbaeume_biotopbaum AS biotopbaum
            WHERE
                foto.biotopbaum = biotopbaum.t_id
            GROUP BY 
                biotopbaum
        ) AS foto
        ON foto.biotopbaum = biotopbaum.t_id
GROUP BY
    baum_id,
    baumkategorie,
    inventur_jahr,
    wirtschaftszone,
    gesuchsnummer,
    waldeigentuemer_code,
    baumart,
    bhd,
    baumhoehe, 
    merkmal_1,
    beschreibung_merkmal_1,
    merkmal_2,
    beschreibung_merkmal_2,
    merkmal_3,
    beschreibung_merkmal_3,
    massnahmen,
    besonderheiten,
    biotopflaeche,
    bemerkungen,
    tp_inventar,
    auszahlung_beitrag,
    auszahlung_beitrag_jahr,
    biotopbaum.geometrie,
    waldgesellschaft.legende,
    waldplan.wpnr,
    forstkreis.aname,
    forstrevier.aname,
    kanton.geometrie,
    kanton.kantonskuerzel,
    bfs_gemeindenummer,
    gemeindegrenze_ausserkantonal.bfs_nummer,
    gemeindegrenze_ausserkantonal.kanton,
    flurname.name,
    waldgesellschaft.ges_neu,
    foto.ablage
;