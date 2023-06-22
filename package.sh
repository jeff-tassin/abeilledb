#!/bin/sh

# Package for Mac
rm -rf dist
mkdir dist
cp assets/resources/images/abeille.icns dist 
cp target/scala-3.2.2/abeilledb.jar dist 
cd dist

jpackage --input . \
--name AbeilleDb \
--main-jar abeilledb.jar \
--main-class com.jeta.abeille.main.Main \
--type dmg \
--icon "./abeille.icns" \
--app-version "1.2.3" \
--copyright "Copyright 2023 Jeff Tassin" \
--mac-package-name "Abeille Database Client" \
--verbose \
--java-options '--enable-preview'  

cd ..

CREATE OR REPLACE FUNCTION clia.get_certificate_age_num(cert_id INTEGER)
RETURNS INTEGER AS
$$
DECLARE
    result INTEGER;
BEGIN
    select age_num into result from (
         select 0 as "age_num", pc.* from clia.provider_certificates pc
           inner join clia.prvdr_clia p on p.provider_id=pc.provider_id
         where
	       p.provider_id=(select provider_id from clia.provider_certificates where id=cert_id)
           and p.effective_date is not null
           and pc.effective_date > p.effective_date
         union all
         select 1 as "age_num", pc.* from clia.provider_certificates pc
	       inner join clia.prvdr_clia p on p.provider_id=pc.provider_id
         where
   	       p.provider_id=(select provider_id from clia.provider_certificates where id=cert_id)
           and p.effective_date is not null
           and  pc.effective_date = p.effective_date
         union all
         select ROW_NUMBER() OVER ( order by pc.effective_date DESC) + 1 as "age_num", pc.* from clia.provider_certificates pc
           inner join clia.prvdr_clia p on p.provider_id=pc.provider_id
         where
   	       p.provider_id=(select provider_id from clia.provider_certificates where id=cert_id)
           and p.effective_date is not null
           and  pc.effective_date < p.effective_date) certs
      where certs.id = cert_id
      order by effective_date DESC
	  LIMIT 1;
	  return result;
END;
$$
LANGUAGE plpgsql;