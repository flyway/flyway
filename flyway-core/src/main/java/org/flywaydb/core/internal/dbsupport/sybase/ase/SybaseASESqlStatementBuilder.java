/**
 * 
 */
package org.flywaydb.core.internal.dbsupport.sybase.ase;

import org.flywaydb.core.internal.dbsupport.Delimiter;
import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;

/**
 * SqlStatementBuilder supporting Sybase Server-specific delimiter changes.
 * 
 * @author Jason Wong
 *
 */
public class SybaseASESqlStatementBuilder extends SqlStatementBuilder {

	@Override
    protected Delimiter getDefaultDelimiter() {
        return new Delimiter("GO", true);
    }
	
	@Override
    protected String computeAlternateCloseQuote(String openQuote) {
        char specialChar = openQuote.charAt(2);
        switch (specialChar) {
            case '(':
                return ")'";
            default:
                return specialChar + "'";
        }
    }
}
