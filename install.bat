
:: Author  = ZekerZhayard
:: Version = 1.0

@CHCP 936 > NUL
@ECHO OFF
TITLE һ����װģ�� by ZekerZhayard
CD /D "%~dp0"

ECHO.
ECHO.==============================================
ECHO.        �ҵ������й���һ����װģ��V1.0
ECHO.                             -by ZekerZhayard
ECHO.----------------------------------------------
ECHO. �������������ҵ������й�����һ�����ð�װģ��
ECHO. �����ɱ����������������󱨣��빴ѡ��������
ECHO. ʹ�ñ����ǰ������˳��ҵ������й����κ���Ϸ
ECHO.==============================================
ECHO.
ECHO. �밴�������ʼ���б�����...
ECHO.
PAUSE > NUL

REG QUERY "HKCU\Software\Netease\MCLauncher" /v "InstallLocation" > ".\ext\InstallLocation.txt" 2> NUL || (
    ECHO.
    ECHO.--------------------------------
    ECHO. �㻹û�а�װ�����ҵ������й���
    ECHO. ����ǰ�� mc.163.com ���ز���װ
    ECHO.--------------------------------
    GOTO iexit
)

REM ��ȡ��MCLauncher���͡�MCLDownload���ļ���·��
FOR /F "tokens=2,* usebackq" %%A IN (".\ext\InstallLocation.txt") DO SET "IL=%%B"
FOR /F "tokens=2,*" %%A IN (' REG QUERY "HKCU\Software\Netease\MCLauncher" /v "DownloadPath" ') DO SET "DP=%%B"
DEL ".\ext\InstallLocation.txt"

IF "%PROCESSOR_ARCHITECTURE%"=="x86" (
    SET "JRE=jre-v32"
) ELSE (
    SET "JRE=jre-v64-170307"
)
WMIC DATAFILE WHERE "Name='%DP:\=\\%\\ext\\%JRE%\\jre7\\bin\\javaw.exe'" GET Version /VALUE | FIND "Version=8.0" > NUL && ^
WMIC DATAFILE WHERE "Name='%DP:\=\\%\\ext\\%JRE%\\jre8\\bin\\javaw.exe'" GET Version /VALUE | FIND "Version=8.0" > NUL && ^
GOTO icopy

REM ��Ҫ�޸�jre
ECHO.
ECHO.---------------------
ECHO. ��⵽��û���޸�jre
ECHO. ����ʼ�Զ��޸�jre
ECHO.---------------------
ECHO.

REM ǿ����ֹ����jreĿ¼�µ����г���
FOR /F "tokens=*" %%A IN (' WMIC PROCESS GET ExecutablePath^,ProcessId ^| FIND "%DP%\ext\%JRE%\" ') DO FOR /F "tokens=2" %%B IN ("%%~fsA") DO TASKKILL /PID %%B /T

RD /S /Q "%DP%\ext\%JRE%\" 2> NUL
MD "%DP%\ext\%JRE%\" 2> NUL

ECHO.
ECHO.-------------
ECHO. ��������jre
ECHO.-------------
ECHO.
".\ext\wget.exe" -t0 -c -T2 -P"%DP%\ext\%JRE%" "https://x19.gdl.netease.com/%JRE%.7z" || (
    TITLE һ����װģ�� by ZekerZhayard
    ECHO.
    ECHO.--------------------
    ECHO. ���ع����г����쳣
    ECHO. ���������Ƿ�ͨ��
    ECHO. ��ѹ�������ļ�ȱʧ
    ECHO.--------------------
    GOTO iexit
)

TITLE һ����װģ�� by ZekerZhayard
ECHO.
ECHO.-----------------------------
ECHO. ���� jre �ɹ�����ʼ��ѹ jre
ECHO.-----------------------------
ECHO.
"%IL%\ext\7z\7z.exe" x "%DP%\ext\%JRE%\%JRE%.7z" -o"%DP%\ext\%JRE%\" "jre8\*" -r || (
    ECHO.
    ECHO.--------------------
    ECHO. ��ѹ�����г����쳣
    ECHO.--------------------
    GOTO iexit
)
ECHO.
ECHO.---------------
ECHO. ��ѹ jre �ɹ�
ECHO.---------------
ECHO.

DEL /Q "%DP%\ext\%JRE%\%JRE%.7z"
XCOPY "%DP%\ext\%JRE%\jre8\*" "%DP%\ext\%JRE%\jre7\" /S /E /C /I /Q /Y > NUL

REM ���� ModAdder �������汾�µ�ģ��
:icopy
SETLOCAL ENABLEDELAYEDEXPANSION
FOR /D %%A IN ("%DP%\cache\game\V_*") DO (
    SET "Version=%%~nA"
    SET "Version=!Version:V_=!"
    SET "Version=!Version:_=.!"
    IF "!Version!" NEQ "1.12.2" (
        XCOPY ".\ext\76002695265125376@4@18.jar" "%%A\mods\" /I /Q /H /R /K /Y > NUL || (
            ECHO.
            ECHO. ���ƹؼ��ļ������쳣
            ECHO. ȷ�����ǽ�ѹ�������ļ��к������б�����
            GOTO iexit
        )
    )
    XCOPY ".\ģ���ļ���\!Version!\*" "%%A\mods\" /C /I /Q /Y > NUL
    FOR %%B IN ("%%A\mods\*.jar") DO (
        MOVE /Y "%%B" "%%~dpnB.zip" > NUL
    )
)
ECHO.
ECHO. һ����װģ�����

:iexit
ECHO.
ECHO. ��������˳�������...
ECHO.
PAUSE > NUL
EXIT
