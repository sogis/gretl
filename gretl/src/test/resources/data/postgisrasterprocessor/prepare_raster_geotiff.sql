SELECT
    1::int AS foo, ST_AsGDALRaster((ST_AsRaster(ST_Buffer(ST_Point(2607880,1228287),10),150, 150)), 'GTiff', ARRAY[''], 2056) AS raster
;
