/**
 * Copyright 2010-2016 Boxfuse GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
using Microsoft.SqlServer.Server;
using System;
using System.Collections;
using System.Collections.Generic;
using System.Data;
using System.Data.SqlClient;
using System.Data.SqlTypes;
using System.IO;
using System.Text;

/*
Provide CLR test procedures and functions.
*/
public class Hello
{
    /*
    Sql procedure, takes a name as argument and output a greeting.
    */
    [SqlProcedure]
    public static void helloFromProc(SqlString name, out SqlString greeting)
    {
        greeting = new SqlString("Hello " + name.Value);
    }

    /*
    Sql function, takes a name as argument and returns a greeting.
    */
    [SqlFunction]
    public static SqlString helloFromFunc(SqlString name)
    {
        return new SqlString("Hello " + name.Value);
    }

    /*
    Table-valued function: content generation. Will prepare N greetings for the given name.
    */
    [SqlFunction(FillRowMethodName="valueTableFill")]
    public static IEnumerable helloFromTableValuedFunction(SqlInt32 count, SqlString name) {
        List<string> lst = new List<string>(count.Value);
        for (int i = 0; i < count.Value; i++) {
            lst.Add("Hello " + name.Value);
        }
        return lst;
    }

    /*
    Table-valued function: translation to SQL structure.
    */
    public static void valueTableFill(object name, out SqlString hello) {
        hello = new SqlString((string) name);
    }

    /*
    SQL trigger. Will automatically output a greeting in the "triggered_greetings" for each entry added in the "names"
    table.
    */
    [SqlTrigger]
    public static void greetingsTrigger() {
        SqlTriggerContext triggContext = SqlContext.TriggerContext;


        if (triggContext.TriggerAction == TriggerAction.Insert)
        {
            using (SqlConnection conn = new SqlConnection("context connection=true"))
            {
                conn.Open();
                SqlCommand sqlComm = new SqlCommand();
                sqlComm.Connection = conn;
                SqlPipe sqlP = SqlContext.Pipe;
                sqlComm.CommandText = "SELECT name from INSERTED";


                SqlParameter greeting = new SqlParameter("@greeting", System.Data.SqlDbType.NVarChar);
                sqlComm.Parameters.Add(greeting);
                greeting.Value = "Hello " + sqlComm.ExecuteScalar().ToString();
                sqlComm.CommandText = "INSERT triggered_greetings (greeting) VALUES (@greeting)";
                sqlP.Send(sqlComm.CommandText);
                sqlP.ExecuteAndSend(sqlComm);
            }
        }
    }
}

/*
User defined aggregate.  See: https://msdn.microsoft.com/en-us/library/ms131051.aspx
*/
[SqlUserDefinedAggregate(Format.UserDefined, MaxByteSize = 8000)]
public class HelloAll : IBinarySerialize    // Required to implement IBinarySerialize to provide user defined
                                            // serialization.
{
    private StringBuilder builder;

    /*
    Aggregate initialization method (pre-defined signature, see MSDN doc).
    */
    public void Init()
    {
        builder = new StringBuilder();
    }

    /*
    Aggregate accumulator method (pre-defined signature, see MSDN doc).
    */
    public void Accumulate(SqlString value)
    {
        if (value.IsNull)
        {
            return;
        }
        add(value.Value);
    }

    /*
    Aggregate merger method (pre-defined signature, see MSDN doc).
    */
    public void Merge(HelloAll other)
    {
        if (other.builder != null) {
            add(other.builder.ToString());
        }
    }

    /*
    Aggregate finalizer method (pre-defined signature, see MSDN doc).
    */
    public SqlString Terminate()
    {
        return new SqlString("Hello " + builder.ToString());
    }

    /*
    Add a string to the buffer with a preceding comma if required. Ignore null and empty values.
    */
    private void add(String str) {
        if (str != null && str.Length > 0) {
            if (builder.Length > 0) {
                builder.Append(", ");
            }
            builder.Append(str);
        }
    }

    /*
    From the IBinarySerialize interface.
    */
    public void Read(BinaryReader reader)
    {
        builder = new StringBuilder(reader.ReadString());
    }

    /*
    From the IBinarySerialize interface.
    */
    public void Write(BinaryWriter writer)
    {
        writer.Write(builder.ToString());
    }
}