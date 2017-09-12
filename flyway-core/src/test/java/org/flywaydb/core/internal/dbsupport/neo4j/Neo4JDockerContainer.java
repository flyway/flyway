/**
 * 
 */
package org.flywaydb.core.internal.dbsupport.neo4j;

import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.wait.HostPortWaitStrategy;

/**
 * @author Ricardo Silva (ScuteraTech)
 *
 */
public class Neo4JDockerContainer<SELF extends Neo4JDockerContainer<SELF>> extends JdbcDatabaseContainer<SELF>{

	static final String NAME = "neo4j";
	    static final String IMAGE = "neo4j";
	    public static final Integer NEO4J_PORT = 7687;
	    private String username = "any";
	    private String password = "any";

	    public Neo4JDockerContainer() {
	        this(IMAGE + ":latest");
	    }

	    public Neo4JDockerContainer(final String dockerImageName) {
	        super(dockerImageName);
	        this.waitStrategy = new HostPortWaitStrategy();
	    }
	    @Override
	    protected Integer getLivenessCheckPort() {
	        return getMappedPort(NEO4J_PORT);
	    }

	    @Override
	    protected void configure() {
	        addExposedPort(NEO4J_PORT);
	        addEnv("NEO4J_AUTH", "none");
	        setCommand("neo4j");
	    }

	    @Override
	    public String getDriverClassName() {
	        return "org.flywaydb.core.internal.dbsupport.neo4j.Neo4JMigrationDriver";
	    }

	    @Override
	    public String getJdbcUrl() {
	        return "jdbc:neo4j:bolt://" + getContainerIpAddress() + ":" + getMappedPort(NEO4J_PORT);
	    }

	    @Override
	    public String getUsername() {
	        return username;
	    }

	    @Override
	    public String getPassword() {
	        return password;
	    }

	    @Override
	    public String getTestQueryString() {
	        return "RETURN 1";
	    }

	    public SELF withDatabaseName(final String databaseName) {
	        return self();
	    }

	    public SELF withUsername(final String username) {
	        this.username = username;
	        return self();
	    }

	    public SELF withPassword(final String password) {
	        this.password = password;
	        return self();
	    }

	    @Override
	    protected void waitUntilContainerStarted() {
	        getWaitStrategy().waitUntilReady(this);
	    }
}
