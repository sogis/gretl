SELECT
    t_id + 1000 AS t_id,
    ST_AsBinary(geom) AS geom,
    ST_AsBinary(ST_SetSRID(ST_Buffer(geom, 1), 2056)) AS buffer
FROM
    public.locations
;