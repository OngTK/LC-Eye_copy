use lc_eye;

-- -----------------------------------------------
-- [1] 멤버 및 회사 관련
-- -----------------------------------------------
select * from company;
select * from member;

-- -----------------------------------------------
-- [2] 기본 정보
-- flow, process_info
-- -----------------------------------------------
select * from unitgroup;
select * from units;
select * from flow;
select * from process_info;

select u.ugno, u.uno, u.unit, g.ugname from units u join unitgroup g on u.ugno = g.ugno; 

select pcno from process_info where pcuuid = "0023d685-38fb-410f-a5d9-657e66070432";
select * from flow where fuuid="29190509-6cc8-47e1-bc99-5ff994e39b74";
select unit from units where uno = 40111;

-- -----------------------------------------------
-- [3] 프로젝트 관련 정보
-- project
-- -----------------------------------------------
select * from project;
select * from project_resultfile;

select prfname from project_resultfile where pjno=300004 order by createdate desc limit 1;


insert into project_resultfile(pjno, prfname, createdate,updatedate) values (300004, "10002_300004_result_20251118_1614", now(), now());