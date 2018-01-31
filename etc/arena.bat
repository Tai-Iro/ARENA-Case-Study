@echo off

REM   Copyright (c) 2004 TUM.  All rights reserved.

if "%OS%"=="Windows_NT" @setlocal

rem %~dp0 is expanded pathname of the current script under NT
set DEFAULT_ARENA_HOME=%~dp0..

if "%ARENA_HOME%"=="" set ARENA_HOME=%DEFAULT_ARENA_HOME%
set DEFAULT_ARENA_HOME=

rem Slurp the command line arguments. This loop allows for an unlimited number
rem of arguments (up to the command line limit, anyway).
set ARENA_CMD_LINE_ARGS=%1
if ""%1""=="""" goto defaultArg

shift

:setupArgs
if ""%1""=="""" goto doneStart
set ARENA_CMD_LINE_ARGS=%ARENA_CMD_LINE_ARGS% %1
shift
goto setupArgs


:defaultArg
set ARENA_CMD_LINE_ARGS="%ARENA_HOME%\conf\arena.properties


rem This label provides a place for the argument list loop to break out 
rem and for NT handling to skip to.
:doneStart

rem find ARENA_HOME if it does not exist due to either an invalid value passed
rem by the user or the %0 problem on Windows 9x
if exist "%ARENA_HOME%" goto checkJava

:noESHome
echo ARENA_HOME is set incorrectly or es could not be located. Please set ARENA_HOME.
goto end

:checkJava
set LOCALCLASSPATH=%CLASSPATH%
for %%i in ("%ARENA_HOME%\lib\*.jar") do call "%ARENA_HOME%\bin\lcp.bat" %%i

if "%JAVA_HOME%" == "" goto noJavaHome
if not exist "%JAVA_HOME%\bin\java.exe" goto noJavaHome
set _JAVACMD=%JAVA_HOME%\bin\java.exe
if exist "%JAVA_HOME%\lib\tools.jar" call "%ARENA_HOME%\bin\lcp.bat" "%JAVA_HOME%\lib\tools.jar"
if exist "%JAVA_HOME%\lib\classes.zip" call "%ARENA_HOME%\bin\lcp.bat" "%JAVA_HOME%\lib\classes.zip"
goto runES

:noJavaHome
set _JAVACMD=java.exe
echo.
echo Warning: JAVA_HOME environment variable is not set.
echo   If starting of the ARENA fails because 
echo   sun.* classes could not be found you will need 
echo   to set the JAVA_HOME environment variable
echo   to the installation directory of java.
echo.


:runES
"%_JAVACMD%" -classpath "%LOCALCLASSPATH%"  %ARENA_OPTS% org.globalse.arena.server.StartArena %ARENA_CMD_LINE_ARGS%
goto end

:end
set LOCALCLASSPATH=
set _JAVACMD=
set ARENA_CMD_LINE_ARGS=

if "%OS%"=="Windows_NT" @endlocal