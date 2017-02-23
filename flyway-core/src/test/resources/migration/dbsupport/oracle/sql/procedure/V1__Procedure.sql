--
-- Copyright 2010-2017 Boxfuse GmbH
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--         http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

create or replace
procedure id_for_sub_sector (
      p_sub_sector_name in sub_sectors.name%type,
      p_sub_sector_id out sub_sectors.id%type) as
   begin
      select id /*+ full(dprod) parallel(dprod, 8) full(uprod) parallel(uprod, 8) */
        into p_sub_sector_id
        from sub_sectors
        where name = p_sub_sector_name;
   exception when NO_DATA_FOUND then
      insert into sub_sectors (id, name)
      values (sub_sectors_seq.nextval, p_sub_sector_name) returning id into p_sub_sector_id;
   end id_for_sub_sector;
/

set define off;

create or replace
procedure id_for_sector (
      p_sector_name in sectors.name%type,
      p_sector_id out sectors.id%type) as
   begin
      select id
        into p_sector_id
        from sectors
        where name = p_sector_name;
   exception when NO_DATA_FOUND then
      insert into sectors (id, name)
      values (sectors_seq.nextval, p_sector_name) returning id into p_sector_id;
   end id_for_sector;
/

create or replace
procedure id_for_rating (
      p_scheme_id in firm_rating_schemes.id%type,
      p_firm_rating_name in firm_ratings.name%type,
      p_rating_id out firm_ratings.id%type) as
   begin
      select id
        into p_rating_id
        from firm_ratings
        where name = p_firm_rating_name
        and scheme_id = p_scheme_id;
   exception when NO_DATA_FOUND then
      insert into firm_ratings (id, scheme_id, name, rating_order, credit_grade_id)
      values (firm_ratings_seq.nextval, p_scheme_id, p_firm_rating_name, 0, 1) returning id into p_rating_id;
   end id_for_rating;
/

create or replace
procedure id_for_rating_scheme (
      p_scheme_name in firm_rating_schemes.name%type,
      p_firm_id in firms.id%type,
      p_scheme_id out firm_ratings.id%type) as
   begin
      select id
        into p_scheme_id
        from firm_rating_schemes
        where name = p_scheme_name
        and firm_id = p_firm_id;
   exception when NO_DATA_FOUND then
      insert into firm_rating_schemes (id, firm_id, name)
      values (firm_rating_schemes_seq.nextval, p_firm_id, p_scheme_name) returning id into p_scheme_id;
   end id_for_rating_scheme;
/

create or replace
procedure id_for_risk_param_type (
      p_type_name in rparam_types.name%type,
      p_type_scope in rparam_types.scope%type,
      p_type_id out rparam_types.id%type) as
   begin
      select id
        into p_type_id
        from rparam_types
        where name = p_type_name
        and scope = p_type_scope;
   exception when NO_DATA_FOUND then
      insert into rparam_types (id, name, family_id, scope)
      values (rparam_types_seq.nextval, p_type_name, 0, p_type_scope) returning id into p_type_id;
   end id_for_risk_param_type;
   /

create or replace
procedure id_for_portfolio (
      p_pf_name in portfolios.pf_name%type,
      p_data_file_id in portfolios.data_file_id%type,
      p_pf_id out portfolios.id%type) as
   begin
      select id
        into p_pf_id
        from portfolios
        where pf_name = p_pf_name
        and data_file_id = p_data_file_id;
   exception when NO_DATA_FOUND then
      insert into portfolios (id, pf_name, data_file_id, sub_sector_id)
      values (portfolios_seq.nextval, p_pf_name, p_data_file_id, 0) returning id into p_pf_id;
   end id_for_portfolio;
   /

create or replace
procedure id_for_country (
      p_country_name in countries.name%type,
      p_country_id out countries.id%type) as
   begin
      select id
        into p_country_id
        from countries
        where name = p_country_name;
   exception when NO_DATA_FOUND then
      insert into countries (id, code, name)
      values (countries_seq.nextval, p_country_name, p_country_name) returning id into p_country_id;
   end id_for_country;
   /

create or replace
procedure id_for_firm (
      p_firm_name in firms.firm_name%type,
      p_firm_id out firms.id%type) as
   begin
      select id
        into p_firm_id
        from firms
        where firm_name = p_firm_name;
   exception when no_data_found then
      insert into firms (id, firm_name)
      values (firms_seq.nextval, p_firm_name) returning id into p_firm_id;
   end id_for_firm;
   /


create or replace
procedure id_for_sub_sector_and_sector (
      p_sub_sector_name in sub_sectors.name%type,
      p_sector_id in sectors.id%type,
      p_sub_sector_id out sub_sectors.id%type) as
   begin
      select id
        into p_sub_sector_id
        from sub_sectors
        where name = p_sub_sector_name
        and sector_id = p_sector_id;
   exception when NO_DATA_FOUND then
      insert into sub_sectors (id, name, sector_id)
      values (sub_sectors_seq.nextval, p_sub_sector_name, p_sector_id) returning id into p_sub_sector_id;
   end id_for_sub_sector_and_sector;
/



CREATE OR REPLACE procedure add_portfolio (
      p_name in portfolios.pf_name%type,
      p_asset_class  in PORTFOLIOS.ASSET_CLASS%type,
      p_data_file_id in firm_data_files.id%type,
      p_country_name in countries.name%type,
      p_sectorName in sectors.name%type,
      p_subsectorname in sub_sectors.name%type,
      p_firm_name in firms.firm_name%type,
      p_firm_rating_scheme in firm_rating_schemes.name%type,
      p_pf_id out portfolios.id%type) as
   v_sector_id sectors.id%type;
   v_firm_id firms.id%type;
   v_sub_sector_id sectors.id%type;
   v_country_id countries.id%type;
   v_scheme_id firm_rating_schemes.id%type;
   begin
     id_for_firm (p_firm_name, v_firm_id);
     id_for_rating_scheme (p_firm_rating_scheme, v_firm_id, v_scheme_id);
     if p_country_name is not null then
         id_for_country (p_country_name, v_country_id);
     end if;
     if p_sectorName is not null then
         id_for_sector (p_sectorName, v_sector_id);
     end if;
     if p_subSectorName is not null then
        id_for_sub_sector_and_sector (p_subSectorName, v_sector_id, v_sub_sector_id);
     end if;
    select id
        into p_pf_id
        from portfolios
        where pf_name = p_name
        and data_file_id = p_data_file_id
        and country_id = v_country_id
        and scheme_id = v_scheme_id
        and sub_sector_id = v_sub_sector_id;
   exception when no_data_found then
     insert into portfolios(id, pf_name, asset_class,  data_file_id, country_id, sub_sector_id, scheme_id)
             values (portfolios_seq.nextval, p_name, p_asset_class,  p_data_file_id, v_country_id, v_sub_sector_id, v_scheme_id) returning id into p_pf_id;
end add_portfolio;
/


CREATE OR REPLACE PROCEDURE update_portfolio(
    p_name in portfolios.pf_name%type,
    p_product_type in PORTFOLIOS.BUS_UNIT%type,
    p_currency  in PORTFOLIOS.currency%type,
    p_initial_balance  in PORTFOLIOS.INITIAL_BAL%type) as
    --
    v_pf_id portfolios.id%type;
    --
    cursor c1 is
    select id
    from portfolios
    where pf_name = p_name
    for update of BUS_UNIT, CURRENCY, INITIAL_BAL;
    --
begin
  open c1;
  fetch c1 into v_pf_id;

  if c1%notfound then raise no_data_found;
  else
    update  portfolios
    set
        BUS_UNIT = p_product_type,
        currency = p_currency  ,
        INITIAL_BAL = p_initial_balance
     where current of c1;
  end if;
  -- dbms_output.put_line('PROCEDURE update_portfolio : portfolio updated : ' || p_name  );
exception
    when no_data_found then
        dbms_output.put_line('PROCEDURE update_portfolio : no portfolio found: ' || p_name  );
        raise no_data_found;
    WHEN OTHERS THEN
       -- Consider logging the error and then re-raise
        dbms_output.put_line('PROCEDURE update_portfolio : error in update to portfolio: ' || p_name  );
       RAISE;
END update_portfolio;
/

create or replace
procedure add_rparam_value (
      p_rparam_id in risk_parameters.id%type,
      p_row_order in rparam_dim_values.dim_order%type,
      p_col_order in rparam_dim_values.dim_order%type,
      p_value in rparam_values.value%type) as
   v_row_ref_id rparam_values.id%type;
   v_col_ref_id rparam_values.id%type;
 begin

    select id
        into v_row_ref_id
        from rparam_dim_values
        where param_id = p_rparam_id
        and dim_axis = 0
        and dim_order = p_row_order;
    select id
        into v_col_ref_id
        from rparam_dim_values
        where param_id = p_rparam_id
        and dim_axis = 1
        and dim_order = p_col_order;

	insert into rparam_values(id, row_ref_id, col_ref_id, value) values (rparam_values_seq.nextval, v_row_ref_id, v_col_ref_id, p_value);

end add_rparam_value;
/

create or replace
procedure add_risk_parameter (
      p_scenario_id in scenarios.id%type,
      p_name in risk_parameters.name%type,
      p_rparam_type_name in rparam_types.name%type,
      p_rparam_type_scope in rparam_types.scope%type,
      p_country_name in countries.name%type,
      p_sector_name in sectors.name%type,
      p_pf_name in portfolios.pf_name%type,
      p_data_file_id in portfolios.data_file_id%type,
      p_rparam_id out portfolios.id%type) as
   v_pf_id sectors.id%type;
   v_err_msg varchar2(250);
   v_rparam_type_id rparam_types.id%type;
   v_country_id countries.id%type;
   v_sector_id sectors.id%type;

   begin
     if p_country_name is not null then
         id_for_country (p_country_name, v_country_id);
     end if;
     if p_sector_name is not null then
         id_for_sector (p_sector_name, v_sector_id);
     end if;
     if p_pf_name is not null then
        id_for_portfolio (p_pf_name, p_data_file_id, v_pf_id);
     end if;
     if p_rparam_type_name is not null then
        id_for_risk_param_type(p_rparam_type_name, p_rparam_type_scope, v_rparam_type_id);
     end if;
     insert into risk_parameters(id, name, type_id, scenario_id, country_id, sector_id, portfolio_id)
             values (risk_parameters_seq.nextval, p_name, v_rparam_type_id,
              p_scenario_id, v_country_id, v_sector_id, v_pf_id) returning id into p_rparam_id;
     exception
       when others then
      v_err_msg := sqlerrm;
       dbms_output.put_line('Could not create risk parameter ' || p_name || ':');
       dbms_output.put_line(v_err_msg);

end add_risk_parameter;
/

create or replace
procedure add_sub_portfolio (
      p_name in firm_subportfolios.name%type,
      p_pf_id in portfolios.id%type,
      p_firm_name in firms.firm_name%type,
      p_approach in firm_subportfolios.approach%type,
      p_maturity_dt in firm_subportfolios.maturity_dt%type,
      p_firm_rating_scheme in firm_rating_schemes.name%type,
      p_firm_rating_name in firm_ratings.name%type,
      p_db in firm_subportfolios.db%type,
      p_ead in firm_subportfolios.ead%type,
      p_rwa in firm_subportfolios.rwa%type,
      p_pd in firm_subportfolios.pd%type,
      p_lgd in firm_subportfolios.lgd%type,
      p_el in firm_subportfolios.el%type,
      p_sub_pf_id out firm_subportfolios.id%type) as
   v_rating_id firm_ratings.id%type;
   v_firm_id firms.id%type;
   v_scheme_id firm_rating_schemes.id%type;
   begin
     id_for_firm (p_firm_name, v_firm_id);
     id_for_rating_scheme (p_firm_rating_scheme, v_firm_id, v_scheme_id);
     id_for_rating (v_scheme_id, p_firm_rating_name, v_rating_id);
     insert into firm_subportfolios(id, portfolio_id, name, approach, firm_rating_id, db, ead, rwa, pd, lgd, el)
             values (firm_subportfolios_seq.nextval, p_pf_id, p_name, p_approach, v_rating_id, p_db, p_ead, p_rwa, p_pd, p_lgd, p_el) returning id into p_sub_pf_id;
end add_sub_portfolio;
/

CREATE OR REPLACE PROCEDURE set_right_value_for_sequence(seq_name in VARCHAR2, table_name in VARCHAR2, column_id in VARCHAR2)
IS
  seq_val NUMBER(6);
  row_count NUMBER(6);
  BEGIN
    EXECUTE IMMEDIATE
    'select ' || seq_name || '.nextval from dual' INTO seq_val;

    EXECUTE IMMEDIATE
    'alter sequence  ' || seq_name || ' increment by -' || seq_val || ' minvalue 0';

    EXECUTE IMMEDIATE
    'select ' || seq_name || '.nextval from dual' INTO seq_val;

    EXECUTE IMMEDIATE
    'select case when max(' || column_id || ') is null then 1 else max(' || column_id || ') end from ' || table_name INTO row_count;

    EXECUTE IMMEDIATE
    'alter sequence ' || seq_name || ' increment by ' || row_count || ' minvalue 0';

    EXECUTE IMMEDIATE
    'select ' || seq_name || '.nextval from dual' INTO seq_val;

    EXECUTE IMMEDIATE
    'alter sequence ' || seq_name || ' increment by 1 minvalue 1';
  END;
/

CREATE OR REPLACE PROCEDURE dummy_proc(seq_name in VARCHAR2, table_name in VARCHAR2, column_id in VARCHAR2)
IS
  seq_val NUMBER(6);
  row_count NUMBER(6);
  BEGIN
    EXECUTE IMMEDIATE
    'select ''abc'' from dual';
  END;
/

CALL dummy_proc('SEQ_ATR', 'TOTCATTRIB', 'ATTRIB_ID');