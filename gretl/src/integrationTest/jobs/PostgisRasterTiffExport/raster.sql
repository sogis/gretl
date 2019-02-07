SELECT
    '1'::int AS foo, ST_AsTiff(ST_AsRaster(ST_Buffer(ST_Point(1,5),10),150,150)) AS raster
;
