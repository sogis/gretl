SELECT
    t_id,
    ST_AsBinary(geom) AS geom
FROM
    public.locations
;