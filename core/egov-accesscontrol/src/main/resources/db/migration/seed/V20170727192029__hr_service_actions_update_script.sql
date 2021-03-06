---------------Correcting service and action scripts---------------------
update eg_action set parentmodule = (select id from service where code = 'Employee' and tenantid='default')::varchar where parentmodule = (select id from service where code = 'HR EMPLOYEE' and tenantid='default')::varchar;
delete from service where code = 'HR EMPLOYEE' and tenantid='default';

update service set contextroot = 'EIS' where code in ('LEAVE-MGTMT', 'ATTENDANCE', 'EIS Masters', 'Employee Movement') and tenantid='default';
update service set contextroot = 'EIS Masters' where code in ('Employee', 'Grade', 'Category', 'Community', 'Designation', 'Position', 'CalendarHolidays', 'Calendar', 'Nominee') and tenantid='default';
update service set contextroot = 'LEAVE-MGTMT' where code in ('Leave Mapping', 'Leave Opening Balance', 'Leave Application', 'Leave Type') and tenantid='default';
update service set parentmodule = (select id from service where code = 'LEAVE-MGTMT' and tenantid='default')::varchar where code = 'Leave Type' and tenantid='default';
update service set ordernumber = 0 where code = 'Employee' and tenantid='default';
update service set ordernumber = 1 where code = 'Designation' and tenantid='default';
update service set ordernumber = 2 where code = 'Position' and tenantid='default';
update service set ordernumber = 3 where code = 'Nominee' and tenantid='default';
update service set ordernumber = 0 where code = 'EIS Masters' and tenantid='default';
update service set ordernumber = 1 where code = 'ATTENDANCE' and tenantid='default';
update service set ordernumber = 2 where code = 'LEAVE-MGTMT' and tenantid='default';
update service set ordernumber = 3 where code = 'Employee Movement' and tenantid='default';

update eg_action set servicecode = 'Employee' where parentmodule = (select id from service where code = 'Employee' and tenantid='default')::varchar;
update eg_action set servicecode = 'Grade' where parentmodule = (select id from service where code = 'Grade' and tenantid='default')::varchar;
update eg_action set servicecode = 'Grade' where name = 'SearchGrade';
update eg_action set servicecode = 'Category' where parentmodule = (select id from service where code = 'Category' and tenantid='default')::varchar;
update eg_action set servicecode = 'Community' where parentmodule = (select id from service where code = 'Community' and tenantid='default')::varchar;
update eg_action set servicecode = 'Designation' where parentmodule = (select id from service where code = 'Designation' and tenantid='default')::varchar;
update eg_action set servicecode = 'Position' where parentmodule = (select id from service where code = 'Position' and tenantid='default')::varchar;
update eg_action set servicecode = 'CalendarHolidays' where parentmodule = (select id from service where code = 'CalendarHolidays' and tenantid='default')::varchar;
update eg_action set servicecode = 'Calendar' where parentmodule = (select id from service where code = 'Calendar' and tenantid='default')::varchar;
update eg_action set servicecode = 'ATTENDANCE' where parentmodule = (select id from service where code = 'ATTENDANCE' and tenantid='default')::varchar;
update eg_action set servicecode = 'EIS Masters' where parentmodule = (select id from service where code = 'EIS Masters' and tenantid='default')::varchar;
update eg_action set servicecode = 'Leave Mapping' where parentmodule = (select id from service where code = 'Leave Mapping' and tenantid='default')::varchar;
update eg_action set servicecode = 'Leave Opening Balance' where parentmodule = (select id from service where code = 'Leave Opening Balance' and tenantid='default')::varchar;
update eg_action set servicecode = 'Leave Application' where parentmodule = (select id from service where code = 'Leave Application' and tenantid='default')::varchar;
update eg_action set servicecode = 'Leave Type' where parentmodule = (select id from service where code = 'Leave Type' and tenantid='default')::varchar;
update eg_action set servicecode = 'Nominee' where parentmodule = (select id from service where code = 'Nominee' and tenantid='default')::varchar;
update eg_action set servicecode = 'Employee Movement' where parentmodule = (select id from service where code = 'Employee Movement' and tenantid='default')::varchar;
update eg_action set servicecode = 'Nominee' where name in ('NomineeUpdate', 'NomineeSearch', 'NomineeCreate');
update eg_action set servicecode = 'ATTENDANCE' where name in ('AjaxSearchAttendances', 'AjaxCreateAttendances', 'AjaxUpdateAttendances');
update eg_action set servicecode = 'Leave Mapping' where name in ('CreateLeave Mappings', 'Leave MappingUpdate', 'SearchLeave Mapping');
update eg_action set servicecode = 'Position' where name in ('VacantPositions', 'PositionSearch');
update eg_action set servicecode = 'Leave Opening Balance' where name in ('SearchLeave Opening Balance', 'CreateLeave Opening Balances', 'Leave Opening BalanceUpdate');
update eg_action set servicecode = 'Leave Application' where name in ('Create Leave Applications', 'Leave Application Update', 'Search Leave Application');
update eg_action set servicecode = 'Leave Type' where name in ('Create LeaveType', 'Leave Type Update', 'Search Leave Type');
update eg_action set servicecode = 'Designation' where name in ('SearchCategory', 'DesignationCreate');
update eg_action set servicecode = 'Calendar' where name = 'CalendarSearch';
update eg_action set servicecode = 'CalendarHolidays' where name in ('CreateHoliday', 'UpdateHoliday', 'CalendarHolidaysSearch');
update eg_action set servicecode = 'EIS Masters' where name in ('EmployeeTypes', 'EmployeeStatus', 'EmployeeGroup', 'CommonLanguages', 'CommonReligions', 'RecruitmentModes', 'RecruitmentTypes', 'RecruitmentQuotas', 'HRConfigSearch', 'CommonDepartmentsSearch', 'Search Eligible Leaves', 'Attendance', 'Leaveallotments', 'Leaveapplications', 'Leavetypes', 'Holidays', 'Leaveopeningbalances', 'SearchGender', 'SearchRelationship');