package org.flywaydb.core.internal.resolver.tokenizer;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.resource.LoadableResource;

import java.io.StreamTokenizer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public final class SqlParser {
    /**
     * Array of all special characters which can be used in SQL
     */
    private static final char[] SQL_CHARS = new char[]{
        '\'',
        '"',
        ':',
        '_',
        '(',
        ')',
        ';',
        ',',
        '.'
    };

    /**
     * Takes a loadable resource and parses it as SQL, it will remove all unnecessary whitespace
     * as well as uppercase all SQL keywords.
     *
     * It will result in a SQL string that will be totally similar for two exact same SQL files.
     * Which means that a developer can change the casing or spacing without changing the output of this function
     *
     * @param loadableResource The loadable resource
     * @return The parsed SQL string
     */
    public static String parse( LoadableResource loadableResource ) {
        // Load file inside the tokenizer
        StreamTokenizer tokenizer = new StreamTokenizer( loadableResource.read() );
        List<String>    tokens    = new LinkedList<>();

        // Configure the tokenizer for SQL
        initWordChars( tokenizer );

        tokenizer.commentChar( '-' );
        tokenizer.slashSlashComments( false );
        tokenizer.eolIsSignificant( false );
        tokenizer.slashStarComments( false );

        // Start the tokenizing
        try {
            int currentToken = tokenizer.nextToken();

            while ( currentToken != StreamTokenizer.TT_EOF ) {
                if ( tokenizer.ttype == StreamTokenizer.TT_NUMBER ) {
                    tokens.add( String.valueOf( tokenizer.nval ) );
                } else if ( tokenizer.ttype == StreamTokenizer.TT_WORD ) {
                    tokens.add( upperCaseSqlKeywords( tokenizer.sval ) );
                }

                currentToken = tokenizer.nextToken();
            }
        } catch ( Exception e ) {
            throw new FlywayException( "Unable to tokenize SQL file " + loadableResource.getFilename(), e );
        }

        return String.join( " ", tokens );
    }

    /**
     * Takes a token and turns any SQL keyword inside into a upper cased version
     *
     * @param token The token
     * @return The token with correct casing
     */
    private static String upperCaseSqlKeywords( String token ) {
        // We need to not split on ' or " so it wont detect false positive such as
        // " values('not' " which would insert 'not' inside the database
        // In this case the return of the function would be " VALUES('not', "
        for ( String s : token.split( "[^a-zA-Z0-9_'\"]+" ) ) {
            boolean isSqlKeyword = Arrays.stream( SqlKeywords.SQL_KEYWORDS )
                                         .anyMatch( keyword -> keyword.equalsIgnoreCase( s.toUpperCase( Locale.ENGLISH ) ) );

            if ( isSqlKeyword ) {
                token = token.replace( s, s.toUpperCase( Locale.ENGLISH ) );
            }
        }

        return token;
    }

    /**
     * Initializes the StreamTokenizer to detect SQL correctly
     *
     * @param tokenizer The stream tokenizer
     */
    private static void initWordChars( StreamTokenizer tokenizer ) {
        for ( char sqlChar : SQL_CHARS ) {
            tokenizer.wordChars( sqlChar, sqlChar );
        }
    }
}
