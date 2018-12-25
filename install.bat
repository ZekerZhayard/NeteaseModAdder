
:: Author  = ZekerZhayard
:: Version = 1.0

@CHCP 936 > NUL
@ECHO OFF
TITLE 一键安装模组 by ZekerZhayard
CD /D "%~dp0"

ECHO.
ECHO.==============================================
ECHO.        我的世界中国版一键安装模组V1.0
ECHO.                             -by ZekerZhayard
ECHO.----------------------------------------------
ECHO. 本工具用于在我的世界中国版中一键永久安装模组
ECHO. 如果有杀毒软件报毒，纯属误报，请勾选允许运行
ECHO. 使用本软件前请必须退出我的世界中国版任何游戏
ECHO.==============================================
ECHO.
ECHO. 请按任意键开始运行本程序...
ECHO.
PAUSE > NUL

REG QUERY "HKCU\Software\Netease\MCLauncher" /v "InstallLocation" > ".\ext\InstallLocation.txt" 2> NUL || (
    ECHO.
    ECHO.--------------------------------
    ECHO. 你还没有安装网易我的世界中国版
    ECHO. 请先前往 mc.163.com 下载并安装
    ECHO.--------------------------------
    GOTO iexit
)

REM 获取「MCLauncher」和「MCLDownload」文件夹路径
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

REM 需要修改jre
ECHO.
ECHO.---------------------
ECHO. 检测到你没有修改jre
ECHO. 程序开始自动修改jre
ECHO.---------------------
ECHO.

REM 强制中止网易jre目录下的所有程序
FOR /F "tokens=*" %%A IN (' WMIC PROCESS GET ExecutablePath^,ProcessId ^| FIND "%DP%\ext\%JRE%\" ') DO FOR /F "tokens=2" %%B IN ("%%~fsA") DO TASKKILL /PID %%B /T

RD /S /Q "%DP%\ext\%JRE%\" 2> NUL
MD "%DP%\ext\%JRE%\" 2> NUL

ECHO.
ECHO.-------------
ECHO. 正在下载jre
ECHO.-------------
ECHO.
".\ext\wget.exe" -t0 -c -T2 -P"%DP%\ext\%JRE%" "https://x19.gdl.netease.com/%JRE%.7z" || (
    TITLE 一键安装模组 by ZekerZhayard
    ECHO.
    ECHO.--------------------
    ECHO. 下载过程中出现异常
    ECHO. 请检查网络是否通畅
    ECHO. 或压缩包内文件缺失
    ECHO.--------------------
    GOTO iexit
)

TITLE 一键安装模组 by ZekerZhayard
ECHO.
ECHO.-----------------------------
ECHO. 下载 jre 成功，开始解压 jre
ECHO.-----------------------------
ECHO.
"%IL%\ext\7z\7z.exe" x "%DP%\ext\%JRE%\%JRE%.7z" -o"%DP%\ext\%JRE%\" "jre8\*" -r || (
    ECHO.
    ECHO.--------------------
    ECHO. 解压过程中出现异常
    ECHO.--------------------
    GOTO iexit
)
ECHO.
ECHO.---------------
ECHO. 解压 jre 成功
ECHO.---------------
ECHO.

DEL /Q "%DP%\ext\%JRE%\%JRE%.7z"
XCOPY "%DP%\ext\%JRE%\jre8\*" "%DP%\ext\%JRE%\jre7\" /S /E /C /I /Q /Y > NUL

REM 复制 ModAdder 及各个版本下的模组
:icopy
SETLOCAL ENABLEDELAYEDEXPANSION
FOR /D %%A IN ("%DP%\cache\game\V_*") DO (
    SET "Version=%%~nA"
    SET "Version=!Version:V_=!"
    SET "Version=!Version:_=.!"
    IF "!Version!" NEQ "1.12.2" (
        XCOPY ".\ext\76002695265125376@4@18.jar" "%%A\mods\" /I /Q /H /R /K /Y > NUL || (
            ECHO.
            ECHO. 复制关键文件出现异常
            ECHO. 确保你是解压了整个文件夹后再运行本程序
            GOTO iexit
        )
    )
    XCOPY ".\模组文件夹\!Version!\*" "%%A\mods\" /C /I /Q /Y > NUL
    FOR %%B IN ("%%A\mods\*.jar") DO (
        MOVE /Y "%%B" "%%~dpnB.zip" > NUL
    )
)
ECHO.
ECHO. 一键安装模组完成

:iexit
ECHO.
ECHO. 按任意键退出本程序...
ECHO.
PAUSE > NUL
EXIT
