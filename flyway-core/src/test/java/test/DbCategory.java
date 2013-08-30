package test;


public class DbCategory {
    public interface DB {}
    public interface InstallableDB extends DB {}
    public interface EmbeddedDB extends DB {}
    public interface CommercialDB extends InstallableDB {}
    public interface OpenSourceDB extends InstallableDB {}

    // specific DBs
    public interface DB2 extends CommercialDB {}
    public interface Derby extends EmbeddedDB {}
    public interface H2 extends EmbeddedDB {}
    public interface HSQL extends EmbeddedDB {}
    public interface MySQL extends OpenSourceDB {}
    public interface Oracle extends CommercialDB {}
    public interface PostgreSQL extends OpenSourceDB {}
    public interface SQLServer extends CommercialDB {}

}
