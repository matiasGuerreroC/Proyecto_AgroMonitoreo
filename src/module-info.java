module Proyecto_AgroMonitoreo {
	
	
    requires java.rmi;
    requires java.sql;
	requires com.fasterxml.jackson.annotation;
	requires com.fasterxml.jackson.databind;
	
	
    exports common;
    exports server;
    
}