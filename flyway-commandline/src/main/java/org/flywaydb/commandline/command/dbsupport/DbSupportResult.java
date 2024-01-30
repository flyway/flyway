package org.flywaydb.commandline.command.dbsupport;

import java.util.List;
import org.flywaydb.core.api.output.OperationResult;
import org.flywaydb.core.extensibility.Tier;

public record DbSupportResult(String version,String command,Tier edition,List<DbInfoResult> dbInfoResults) implements OperationResult { }