/*
 * Licensed to Neo4j under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Neo4j licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.neo4j.examples;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.io.fs.FileUtils;
import org.neo4j.logging.Log;
import org.neo4j.logging.LogProvider;
import org.neo4j.logging.Logger;

public class EmbeddedNeo4jWithCustomLogging
{
    private static final File databaseDirectory = new File( "target/neo4j-store" );
    private static DatabaseManagementService managementService;

    private static class MyCustomLogProvider implements LogProvider
    {
        public MyCustomLogProvider( Object output )
        {
        }

        @Override
        public Log getLog( Class loggingClass )
        {
            return new MyCustomLog();
        }

        @Override
        public Log getLog( String context )
        {
            return new MyCustomLog();
        }

        private static class MyCustomLog implements Log
        {
            @Override
            public boolean isDebugEnabled()
            {
                return false;
            }

            @Nonnull
            @Override
            public Logger debugLogger()
            {
                return new MyCustomLogger();
            }

            @Override
            public void debug( @Nonnull String message )
            {

            }

            @Override
            public void debug( @Nonnull String message, @Nonnull Throwable throwable )
            {

            }

            @Override
            public void debug( @Nonnull String format, @Nullable Object... arguments )
            {

            }

            @Nonnull
            @Override
            public Logger infoLogger()
            {
                return new MyCustomLogger();
            }

            @Override
            public void info( @Nonnull String message )
            {

            }

            @Override
            public void info( @Nonnull String message, @Nonnull Throwable throwable )
            {

            }

            @Override
            public void info( @Nonnull String format, @Nullable Object... arguments )
            {

            }

            @Nonnull
            @Override
            public Logger warnLogger()
            {
                return new MyCustomLogger();
            }

            @Override
            public void warn( @Nonnull String message )
            {

            }

            @Override
            public void warn( @Nonnull String message, @Nonnull Throwable throwable )
            {

            }

            @Override
            public void warn( @Nonnull String format, @Nullable Object... arguments )
            {

            }

            @Nonnull
            @Override
            public Logger errorLogger()
            {
                return new MyCustomLogger();
            }

            @Override
            public void error( @Nonnull String message )
            {

            }

            @Override
            public void error( @Nonnull String message, @Nonnull Throwable throwable )
            {

            }

            @Override
            public void error( @Nonnull String format, @Nullable Object... arguments )
            {

            }

            @Override
            public void bulk( @Nonnull Consumer<Log> consumer )
            {

            }

            private static class MyCustomLogger implements Logger
            {
                @Override
                public void log( @Nonnull String message )
                {

                }

                @Override
                public void log( @Nonnull String message, @Nonnull Throwable throwable )
                {

                }

                @Override
                public void log( @Nonnull String format, @Nullable Object... arguments )
                {

                }

                @Override
                public void bulk( @Nonnull Consumer<Logger> consumer )
                {

                }
            }
        }
    }

    public static void main( final String[] args ) throws IOException
    {
        FileUtils.deleteRecursively( databaseDirectory );

        Object output = new Object();

        // tag::startDbWithLogProvider[]
        LogProvider logProvider = new MyCustomLogProvider( output );
        managementService = new DatabaseManagementServiceBuilder( databaseDirectory ).setUserLogProvider( logProvider ).build();
        // end::startDbWithLogProvider[]

        shutdown();
    }

    private static void shutdown()
    {
        managementService.shutdown();
    }
}
