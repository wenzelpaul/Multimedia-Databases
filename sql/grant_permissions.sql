begin 
dbms_java.grant_permission('SYSTEM','SYS:java.net.SocketPermission','*','connect,resolve'); 
end;
/

begin 
dbms_java.grant_permission( 'SYSTEM', 'SYS:java.lang.RuntimePermission', 'accessDeclaredMembers', '' ); 
end;
/

begin 
dbms_java.grant_permission( 'SYSTEM', 'SYS:java.lang.reflect.ReflectPermission', 'suppressAccessChecks', '' ); 
end;
/

EXIT;
/
