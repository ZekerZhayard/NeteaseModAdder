
:: Author  = ZekerZhayard
:: Version = 1.0

@CHCP 936 > NUL
@ECHO OFF
TITLE һ��ɾ��ģ�� by ZekerZhayard
CD /D "%~dp0"

FOR /F "tokens=2,*" %%A IN (' REG QUERY "HKCU\Software\Netease\MCLauncher" /v "DownloadPath" ') DO SET "DP=%%B"
FOR /D %%A IN ("%DP%\cache\game\V_*") DO RD /S /Q "%%A\mods\" 2> NUL

ECHO.
ECHO.------------------
ECHO. ɾ������ģ�����
ECHO.------------------
ECHO.
ECHO. ��������˳�������...
ECHO.
PAUSE > NUL
EXIT